package com.dataarize.recommendations.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GCSDataReaderListenerTest {

    @InjectMocks
    private GCSDataReaderListener listener;

    @Test
    void givenValidFile_whenBeforeReadCalled_thenLogInfo() {
        listener.beforeRead();
    }

    @Test
    void givenValidFile_whenAfterReadCalled_thenLogInfo() {
        listener.afterRead("MOCK");
    }

    @Test
    void givenInValidFile_whenOnReadErrorCalled_thenLogError() {
        listener.onReadError(new RuntimeException());
    }

}