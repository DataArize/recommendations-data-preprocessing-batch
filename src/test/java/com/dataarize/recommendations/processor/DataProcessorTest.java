package com.dataarize.recommendations.processor;

import com.dataarize.recommendations.model.DataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class DataProcessorTest {

    @Mock
    private StepExecution stepExecution;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private ExecutionContext executionContext;
    @InjectMocks
    private DataProcessor dataProcessor;

    @Test
    void giveValidParameters_whenBeforeStepCalled_thenAssignParameter() {
        ReflectionTestUtils.setField(dataProcessor, "outputPath", "MOCK");
        Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        Mockito.when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        Mockito.doNothing().when(executionContext).put(anyString(), anyString());
        dataProcessor.beforeStep(stepExecution);
        Mockito.verify(stepExecution, Mockito.atLeastOnce()).getJobExecution();
        Mockito.verify(jobExecution, Mockito.atLeastOnce()).getExecutionContext();
        Mockito.verify(executionContext, Mockito.atLeastOnce()).put(anyString(), anyString());
    }

    @Test
    void givenValidItem_whenProcessCalled_thenReturnDataSet() throws Exception {
        ReflectionTestUtils.setField(dataProcessor, "movieId", "MOCK");
        DataSet process = dataProcessor.process("MOCK,MOCK,MOCK");
        assertEquals("MOCK", process.getCustomerId());
        assertEquals("MOCK", process.getMovieId());
        assertEquals("MOCK", process.getRatings());
        assertEquals("MOCK", process.getDate());

    }

    @Test
    void givenMovieId_whenProcessCalled_thenReturnDataSet() throws Exception {
        ReflectionTestUtils.setField(dataProcessor, "movieId", "MOCK");
        DataSet process = dataProcessor.process("MOCK:");
        assertNull(process);

    }

}