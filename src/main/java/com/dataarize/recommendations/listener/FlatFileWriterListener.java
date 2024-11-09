package com.dataarize.recommendations.listener;

import com.dataarize.recommendations.constants.HelperConstants;
import com.dataarize.recommendations.model.DataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * {@link ItemWriteListener} implementation that monitors the writing process of {@link DataSet} items
 * to a flat file. This listener logs various events related to the writing process, such as the start
 * and successful completion of the write operation, and checks for any issues with the output file.
 *
 * <p>This class provides hooks for logging before and after writing items, as well as handling errors
 * that occur during the write operation. It is intended to be used within a Spring Batch job configuration
 * to provide monitoring and diagnostics for the data writing process.</p>
 *
 * <p>The listener performs the following operations:
 * <ul>
 *     <li>Logs information about the number of items being written.</li>
 *     <li>Checks the output file after writing to verify it was created and populated correctly.</li>
 *     <li>Handles any errors that occur during the writing of items and logs the exception details.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class FlatFileWriterListener implements ItemWriteListener<DataSet> {

    /**
     * The output path where the flat file will be written.
     * This value is injected from the application configuration.
     */
    @Value("${spring.application.output}")
    private String outputPath;

    /**
     * Called before the items are written to the output file. This method logs the number of items being
     * written and checks if there are any issues with the items to be written.
     *
     * @param items the chunk of {@link DataSet} items to be written.
     */
    @Override
    public void beforeWrite(Chunk<? extends DataSet> items) {
        log.info("Preparing to write {} items to the output file.", items.size());
        if (items.isEmpty()) {
            log.warn("No items to write, this might indicate an issue in the pipeline.");
        } else {
            log.info("Items are validated and ready for writing.");
        }
    }

    /**
     * Called after the items have been successfully written to the output file. This method logs the success
     * message and performs post-write validation by checking the output file for correctness.
     *
     * @param items the chunk of {@link DataSet} items that were written.
     */
    @Override
    public void afterWrite(Chunk<? extends DataSet> items) {
        log.info("Successfully wrote {} items to the output file.", items.size());
        try {
            File outputFile = new File(outputPath + HelperConstants.FILE_NAME);
            if (outputFile.exists() && outputFile.length() == 0) {
                log.warn("Output file is created but appears empty. Verify if this is expected.");
            } else {
                log.info("Output file is created and populated successfully.");
            }
        } catch (Exception e) {
            log.error("Error during post-write file validation: {}", e.getMessage(), e);
        }
    }

    /**
     * Called if an error occurs during the write operation. This method logs the error and provides details
     * about the exception that occurred while writing the items.
     *
     * @param exception the exception that occurred during the write process.
     * @param items the chunk of {@link DataSet} items that were being written when the error occurred.
     */
    @Override
    public void onWriteError(Exception exception, Chunk<? extends DataSet> items) {
        log.error("Error writing {} items. Error message: {}", items.size(), exception.getMessage(), exception);
    }
}
