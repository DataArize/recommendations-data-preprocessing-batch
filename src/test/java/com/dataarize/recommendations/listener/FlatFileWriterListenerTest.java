package com.dataarize.recommendations.listener;

import com.dataarize.recommendations.model.DataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlatFileWriterListenerTest {

    @Mock
    private Chunk<DataSet> chunk;

    @InjectMocks
    private FlatFileWriterListener flatFileWriterListener;

    @Test
    void givenNonEmptyChunk_whenBeforeWriteCalled_thenLogInfo() {
        when(chunk.isEmpty()).thenReturn(false);
        flatFileWriterListener.beforeWrite(chunk);
    }

    @Test
    void givenEmptyChunk_whenBeforeWriteCalled_thenLogWarn() {
        when(chunk.isEmpty()).thenReturn(true);
        flatFileWriterListener.beforeWrite(chunk);
    }

    @Test
    void givenNonEmptyOutputFile_whenAfterWriteCalled_thenLogSuccess() throws IOException {
        ReflectionTestUtils.setField(flatFileWriterListener, "outputPath", "/Users/amith/PycharmProjects/recommendations/src/test/resources/dummy.txt");
        flatFileWriterListener.afterWrite(chunk);
    }

    @Test
    void givenNonEmptyOutputFile_whenAfterWriteCalled_thenLogWarn() throws IOException {
        ReflectionTestUtils.setField(flatFileWriterListener, "outputPath", "/Users/amith/PycharmProjects/recommendations/src/test/resources/mock.txt");
        flatFileWriterListener.afterWrite(chunk);
    }

    @Test
    void givenError_whenOnWriteErrorCalled_thenLogError() throws IOException {
        flatFileWriterListener.onWriteError(new RuntimeException(), chunk);
    }



}