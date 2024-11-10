package com.dataarize.recommendations.tasklet;

import com.dataarize.recommendations.exceptions.MissingFilePathException;
import com.dataarize.recommendations.exceptions.TransferFailedException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class TransferToGCSTaskletTest {


    @Mock
    private StepExecution stepExecution;
    @Mock
    private JobParameters jobParameters;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private ExecutionContext executionContext;
    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private Storage mockStorage;
    @Mock
    private Blob blob;
    @InjectMocks
    private TransferToGCSTasklet tasklet;

    @Test
    void giveValidParameters_whenBeforeStepCalled_thenAssignParameter() {
        Mockito.when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        Mockito.when(jobParameters.getString(anyString())).thenReturn("MOCK");
        Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        Mockito.when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        Mockito.when(executionContext.getString(anyString())).thenReturn("MOCK");
        tasklet.beforeStep(stepExecution);
        assertEquals("MOCK", ReflectionTestUtils.getField(tasklet, "outputBucketPath"));
        assertEquals("MOCK", ReflectionTestUtils.getField(tasklet, "tempOutputFilePath"));
        Mockito.verify(stepExecution, Mockito.atMostOnce()).getJobParameters();
        Mockito.verify(jobParameters, Mockito.atLeastOnce()).getString(anyString());
        Mockito.verify(stepExecution, Mockito.atLeastOnce()).getJobExecution();
        Mockito.verify(jobExecution, Mockito.atLeastOnce()).getExecutionContext();
        Mockito.verify(executionContext, Mockito.atLeastOnce()).getString(anyString());
    }

    @Test
    void giveInValidParameters_whenBeforeStepCalled_thenThrowException() {
        Mockito.when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        Mockito.when(jobParameters.getString(anyString())).thenReturn(null);
        assertThrows(MissingFilePathException.class, () -> tasklet.beforeStep(stepExecution));
        Mockito.verify(stepExecution, Mockito.atMostOnce()).getJobParameters();
        Mockito.verify(jobParameters, Mockito.atLeastOnce()).getString(anyString());
    }

    @Test
    void giveInValidExecutionContext_whenBeforeStepCalled_thenThrowException() {
        Mockito.when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        Mockito.when(jobParameters.getString(anyString())).thenReturn("MOCK");
        Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        Mockito.when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        Mockito.when(executionContext.getString(anyString())).thenReturn(null);
        assertThrows(MissingFilePathException.class, () -> tasklet.beforeStep(stepExecution));
        assertEquals("MOCK", ReflectionTestUtils.getField(tasklet, "outputBucketPath"));
        Mockito.verify(stepExecution, Mockito.atMostOnce()).getJobParameters();
        Mockito.verify(jobParameters, Mockito.atLeastOnce()).getString(anyString());
        Mockito.verify(stepExecution, Mockito.atLeastOnce()).getJobExecution();
        Mockito.verify(jobExecution, Mockito.atLeastOnce()).getExecutionContext();
        Mockito.verify(executionContext, Mockito.atLeastOnce()).getString(anyString());
    }

    @Test
    void givenValidParameters_whenExecuteCalled_thenCompleteSuccessfully() throws Exception {
        URL resource = getClass().getClassLoader().getResource("dummy.txt");
        assertNotNull(resource, "File not found in test resources");
        String filePath = Paths.get(resource.toURI()).toString();
        ReflectionTestUtils.setField(tasklet, "outputBucketPath", "mock");
        ReflectionTestUtils.setField(tasklet, "tempOutputFilePath", filePath);
        Mockito.when(mockStorage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);
        RepeatStatus execute = tasklet.execute(stepContribution, chunkContext);
        Mockito.verify(mockStorage, Mockito.atLeastOnce()).create(any(BlobInfo.class), any(byte[].class));
        assertEquals(RepeatStatus.FINISHED, execute);
    }

    @Test
    void givenValidParameters_whenExecuteCalled_thenThrowException() throws Exception {
        ReflectionTestUtils.setField(tasklet, "outputBucketPath", "mock");
        URL resource = getClass().getClassLoader().getResource("dummy.txt");
        assertNotNull(resource, "File not found in test resources");
        String filePath = Paths.get(resource.toURI()).toString();
        ReflectionTestUtils.setField(tasklet, "tempOutputFilePath", filePath);
        Mockito.when(mockStorage.create(any(BlobInfo.class), any(byte[].class))).thenThrow(new StorageException(1, "MOCK"));
        assertThrows(TransferFailedException.class, () -> tasklet.execute(stepContribution, chunkContext));
        Mockito.verify(mockStorage, Mockito.atLeastOnce()).create(any(BlobInfo.class), any(byte[].class));
    }
}