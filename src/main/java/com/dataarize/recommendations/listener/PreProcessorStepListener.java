package com.dataarize.recommendations.listener;

import com.dataarize.recommendations.constants.ExceptionMessages;
import com.dataarize.recommendations.constants.HelperConstants;
import com.dataarize.recommendations.exceptions.FileDoesNotExistsException;
import com.dataarize.recommendations.exceptions.MissingFilePathException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * {@link StepExecutionListener} implementation used for pre-processing steps in a Spring Batch job.
 * This listener validates the input file before processing by checking if the file exists in Google Cloud Storage (GCS)
 * and if it is not empty. The listener logs the relevant information and throws custom exceptions if the file is missing or empty.
 *
 * <p>Key actions performed by this listener:
 * <ul>
 *     <li>Logs the start of the file reading process.</li>
 *     <li>Checks if the input file exists in GCS and is not empty.</li>
 *     <li>Throws custom exceptions if the file does not exist or is empty.</li>
 *     <li>Logs the successful validation of the file before processing.</li>
 *     <li>Logs the completion of the file reading process after the step.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class PreProcessorStepListener implements StepExecutionListener {

    /**
     * The file path parameter extracted from the job parameters.
     * This path is used to locate the input file in Google Cloud Storage.
     */
    private String filePathParameter;

    /**
     * The {@link Storage} service used to interact with Google Cloud Storage.
     * This instance is used to validate the existence and size of the input file.
     */
    private Storage storage;

    /**
     * This method is called before the step execution begins. It validates the input file specified in the job parameters.
     * If the file is missing or empty, an exception is thrown.
     *
     * @param stepExecution the {@link StepExecution} that contains the job parameters and execution context.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        filePathParameter = Objects.requireNonNull(stepExecution.getJobParameters().getString(HelperConstants.INPUT_FILE_PATH),
                () -> {
                    log.error("Input Bucket Path cannot be null");
                    throw new MissingFilePathException(ExceptionMessages.MISSING_INPUT_FILE_PATH);
                });
        log.info("Starting to read file: {}", filePathParameter);
        storage = StorageOptions.getDefaultInstance().getService();
        String bucketName = filePathParameter.split("/")[2];
        String blobName = filePathParameter.substring(filePathParameter.indexOf(bucketName) + bucketName.length() + 1);
        Blob blob = storage.get(bucketName, blobName);
        if (blob == null || !blob.exists() || blob.getSize() == 0) {
            log.error("Input file is missing or empty: {}", filePathParameter);
            throw new FileDoesNotExistsException(ExceptionMessages.FILE_DOES_NOT_EXISTS + filePathParameter);
        }
        log.info("File validated and ready for processing: {}", filePathParameter);
    }


    /**
     * This method is called after the step execution finishes. It logs the completion of the file reading process.
     *
     * @param stepExecution the {@link StepExecution} that contains the job parameters and execution context.
     * @return the exit status indicating the completion of the step.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Finished reading file: {}", filePathParameter);
        return ExitStatus.COMPLETED;
    }
}
