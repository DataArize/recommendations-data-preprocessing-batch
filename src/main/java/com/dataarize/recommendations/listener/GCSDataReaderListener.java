package com.dataarize.recommendations.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

/**
 * {@link ItemReadListener} implementation that monitors the reading process of items in a batch job.
 * This listener is designed for reading data (in this case, strings) from Google Cloud Storage (GCS) or any
 * similar source. It logs events at different stages of the reading process, including before and after an item is read,
 * as well as any errors encountered during the read operation.
 *
 * <p>This listener is used to track the progress of reading operations and provides logging for diagnostics
 * and monitoring of data pipeline execution.</p>
 *
 * <p>The listener performs the following operations:
 * <ul>
 *     <li>Logs a message before each item is read.</li>
 *     <li>Logs the successful reading of an item along with the line number.</li>
 *     <li>Logs errors if an exception is encountered during the reading process, including the line number and exception details.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class GCSDataReaderListener implements ItemReadListener<String> {

    /**
     * The total number of items that have been read so far.
     * This is used to track the line number during the reading process.
     */
    private long totalItemRead = 0;

    /**
     * Called before each item is read. This method logs the start of the read operation for the next item.
     */
    @Override
    public void beforeRead() {
        log.info("Starting to read next item...");
    }

    /**
     * Called after an item has been successfully read. This method logs the successful reading of an item,
     * including the line number.
     *
     * @param item the item that was read (a {@link String} in this case).
     */
    @Override
    public void afterRead(String item) {
        totalItemRead+=1;
        log.info("Successfully read item, line no: {}", totalItemRead);
    }

    /**
     * Called when an error occurs during the read operation. This method logs the error details, including
     * the line number and the exception message.
     *
     * @param ex the exception that occurred during the read process.
     */
    @Override
    public void onReadError(Exception ex) {
        log.error("Error encountered while reading item, line no: {}, Exception: {}", totalItemRead+1, ex.getMessage(), ex);
    }
}
