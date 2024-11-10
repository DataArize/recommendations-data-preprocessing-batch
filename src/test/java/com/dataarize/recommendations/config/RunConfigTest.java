package com.dataarize.recommendations.config;

import com.dataarize.recommendations.constants.HelperConstants;
import com.dataarize.recommendations.exceptions.MissingFilePathException;
import com.dataarize.recommendations.listener.FlatFileWriterListener;
import com.dataarize.recommendations.listener.GCSDataReaderListener;
import com.dataarize.recommendations.listener.PreProcessorStepListener;
import com.dataarize.recommendations.model.DataSet;
import com.dataarize.recommendations.processor.DataProcessor;
import com.dataarize.recommendations.tasklet.TransferToGCSTasklet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RunConfigTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource mockResource;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ItemReader<String> gcsItemReader;

    @Mock
    private GCSDataReaderListener readerListener;

    @Mock
    private DataProcessor dataProcessor;

    @Mock
    private ItemWriter<DataSet> flatFileItemWriter;

    @Mock
    private FlatFileWriterListener writerListener;

    @Mock
    private PreProcessorStepListener stepListener;

    @Mock
    private TransferToGCSTasklet tasklet;

    @Mock
    private Step preProcessorStep;

    @Mock
    private Step transferToGCSStep;




    @InjectMocks
    private RunConfig runConfig;


    private FlatFileItemReader<String> itemReader;
    private static final String VALID_FILE_PATH = "file:/path/to/input/file.txt";
    private static final String INVALID_FILE_PATH = "file:/invalid/path/to/file.txt";


    @Test
    void givenValidFilePath_whenGcsItemReaderCalled_thenReaderIsCreated() throws Exception {
        Mockito.when(resourceLoader.getResource(VALID_FILE_PATH)).thenReturn(mockResource);
        itemReader = runConfig.gcsItemReader(resourceLoader, VALID_FILE_PATH);
        assertNotNull(itemReader);
        Mockito.verify(resourceLoader, Mockito.times(1)).getResource(VALID_FILE_PATH);
    }

    @Test
    void givenValidFilePath_whenGcsItemReaderCalled_thenThrowsException() {
        assertThrows(MissingFilePathException.class, () ->
                runConfig.gcsItemReader(resourceLoader, null));
    }

    @Test
    void givenValidOutputPath_whenFlatFileItemWriterCalled_thenWriterIsCreated() {
        FlatFileItemWriter<DataSet> itemWriter = runConfig.flatFileItemWriter(VALID_FILE_PATH);
        assertNotNull(itemWriter);
    }

    @Test
    void givenValidDependencies_whenTransferToGCSStepCalled_thenStepIsCreated() {
        Step step = runConfig.transferToGCSStep(jobRepository, transactionManager, tasklet);
        assertNotNull(step, "Step should not be null");
        assertEquals(HelperConstants.TRANSFER_TO_GCS_STEP, step.getName(), "Step name should match");
    }

    @Test
    void givenValidDependencies_whenPreProcessorStepCalled_thenStepIsCreated() {
        Step step = runConfig.preProcessorStep(
                jobRepository,
                transactionManager,
                gcsItemReader,
                readerListener,
                dataProcessor,
                flatFileItemWriter,
                writerListener,
                stepListener
        );
        assertNotNull(step, "Step should not be null");
        assertEquals(HelperConstants.STEP_NAME, step.getName(), "Step name should match");
    }

    @Test
    void givenValidSteps_whenPreProcessorJobCalled_thenJobIsCreated() {
        Job job = runConfig.preProcessorJob(jobRepository, preProcessorStep, transferToGCSStep);
        assertNotNull(job, "Job should not be null");
        assertEquals(HelperConstants.JOB_NAME, job.getName(), "Job name should match");
    }



}