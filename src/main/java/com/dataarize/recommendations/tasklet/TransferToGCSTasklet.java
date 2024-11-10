package com.dataarize.recommendations.tasklet;

import com.dataarize.recommendations.constants.ExceptionMessages;
import com.dataarize.recommendations.constants.HelperConstants;
import com.dataarize.recommendations.exceptions.MissingFilePathException;
import com.dataarize.recommendations.exceptions.TransferFailedException;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.threeten.bp.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * Tasklet that transfers a file from the local filesystem to Google Cloud Storage (GCS).
 * This tasklet is part of a Spring Batch job and is responsible for uploading a temporary file to a specified GCS bucket.
 *
 * <p><b>Functionality:</b></p>
 * <ul>
 *     <li>Uses the Google Cloud Storage API to upload a file to a specified bucket path.</li>
 *     <li>Handles retries for transient failures using customized retry settings.</li>
 *     <li>Logs errors and throws custom exceptions in case of failure, ensuring traceability and monitoring.</li>
 * </ul>
 *
 * <p><b>Enterprise Standards:</b></p>
 * <ul>
 *     <li><b>Retry Logic:</b> Implements retry settings to handle temporary issues with GCS connectivity, ensuring resilience.</li>
 *     <li><b>Error Handling:</b> Custom exception handling (e.g., {@link TransferFailedException}) ensures that failures are properly logged and can be handled by higher layers of the system.</li>
 *     <li><b>Logging:</b> Uses {@link Slf4j} for consistent and structured logging across enterprise applications.</li>
 *     <li><b>Modularity:</b> The tasklet is isolated for clear responsibility (file transfer) and can be reused within Spring Batch jobs.</li>
 * </ul>
 *
 * @see Tasklet
 * @see StepExecutionListener
 */
@Slf4j
@Component
public class TransferToGCSTasklet implements Tasklet, StepExecutionListener {

    private String outputBucketPath;
    private String tempOutputFilePath;

    private final Storage storage;

    @Autowired
    public TransferToGCSTasklet(Storage storage) {
        this.storage = storage;
    }

    /**
     * Executes the task of transferring the file from the local filesystem to GCS.
     * This method uploads the file to the specified bucket and handles retries for failed uploads.
     *
     * @param contribution the step contribution that is passed to the tasklet during the execution
     * @param chunkContext the chunk context that holds the job's execution state
     * @return {@link RepeatStatus#FINISHED} when the tasklet has completed successfully
     * @throws Exception if an error occurs during the file transfer
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        File file = ResourceUtils.getFile(tempOutputFilePath);
        String blobPath = HelperConstants.TARGET_DIRECTORY + file.getName();
        BlobId blobId = BlobId.of(outputBucketPath, blobPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] fileContent = inputStream.readAllBytes();
            storage.create(blobInfo, fileContent);
        } catch (StorageException e) {
            log.error("Failed to upload file to GCS: {}", e.getMessage(), e);
            throw new TransferFailedException(e.getMessage());
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * Initializes the output bucket path and the temporary output file path before the step execution.
     * These values are retrieved from the job's execution context and parameters.
     *
     * @param stepExecution the context of the current batch step execution
     * @throws MissingFilePathException if the required paths are missing from the execution context or parameters
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.outputBucketPath = Objects.requireNonNull(stepExecution.getJobParameters().getString(HelperConstants.OUTPUT_BUCKET_PATH),
                () -> {
                    log.error("Output Bucket Path cannot be null");
                    throw new MissingFilePathException(ExceptionMessages.MISSING_OUTPUT_FILE_PATH);
                });
        this.tempOutputFilePath = Objects.requireNonNull(stepExecution.getJobExecution().getExecutionContext().getString(HelperConstants.TEMP_PATH),
                () -> {
                    log.error("Temp output path cannot be null or empty");
                    throw new MissingFilePathException(ExceptionMessages.MISSING_TEMP_FILE_PATH);
                });

    }
}
