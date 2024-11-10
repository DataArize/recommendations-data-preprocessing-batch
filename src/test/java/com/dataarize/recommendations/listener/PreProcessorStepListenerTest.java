package com.dataarize.recommendations.listener;

import com.dataarize.recommendations.exceptions.FileDoesNotExistsException;
import com.dataarize.recommendations.exceptions.MissingFilePathException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class PreProcessorStepListenerTest {

    @Mock
    private StepExecution stepExecution;
    @Mock
    private Storage storage;
    @Mock
    private Blob blob;

    @Mock
    private JobParameters jobParameters;

    @InjectMocks
    private PreProcessorStepListener listener;

    @Test
    void givenInValidFilePath_whenBeforeStepCalled_thenThrowException() {
        Mockito.when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        Mockito.when(jobParameters.getString(anyString())).thenReturn(null);
        assertThrows(MissingFilePathException.class, () -> listener.beforeStep(stepExecution));
    }

    @Test
    void givenValidFilePath_whenBeforeStepCalled_thenLogIndo() {
        Mockito.when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        Mockito.when(jobParameters.getString(anyString())).thenReturn("gs://Mock/MOCK");
        ReflectionTestUtils.setField(listener, "storage", storage);
        Mockito.when(storage.get(anyString(), anyString())).thenReturn(blob);
        Mockito.when(blob.exists()).thenReturn(true);
        Mockito.when(blob.getSize()).thenReturn(1l);
        listener.beforeStep(stepExecution);

    }

    @Test
    void giveFileDoesNotExists_whenBeforeStepCalled_thenThrowException() {
        Mockito.when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        Mockito.when(jobParameters.getString(anyString())).thenReturn("gs://Mock/MOCK");
        ReflectionTestUtils.setField(listener, "storage", storage);
        Mockito.when(storage.get(anyString(), anyString())).thenReturn(blob);
        Mockito.when(blob.exists()).thenReturn(false);
        assertThrows(FileDoesNotExistsException.class, () -> listener.beforeStep(stepExecution));
    }

    @Test
    void givenValidFilePath_whenAfterStepCalled_thenLogIndo() {
        ExitStatus exitStatus = listener.afterStep(stepExecution);
        assertEquals(ExitStatus.COMPLETED, exitStatus);

    }

}