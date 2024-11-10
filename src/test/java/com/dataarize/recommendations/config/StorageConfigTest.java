package com.dataarize.recommendations.config;

import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StorageConfigTest {

    @InjectMocks
    private StorageConfig storageConfig;

    @Test
    void testStorageBeanCreation() {
        Storage storage = storageConfig.storage();
        assertNotNull(storage);
    }

}