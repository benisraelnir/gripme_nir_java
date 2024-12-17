package com.github.benisraelnir.gripme.service;

import com.github.benisraelnir.gripme.core.reader.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

class FileWatchServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Reader reader;

    private FileWatchService fileWatchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileWatchService = new FileWatchService(messagingTemplate, reader);
    }

    @Test
    void shouldNotifyWhenFileChanges() {
        // Given
        when(reader.lastUpdated(null))
            .thenReturn(1000L)
            .thenReturn(2000L);

        // When
        fileWatchService.watchFiles();
        fileWatchService.watchFiles();

        // Then
        verify(messagingTemplate, times(1))
            .convertAndSend("/topic/refresh", "refresh");
    }

    @Test
    void shouldNotNotifyWhenFileUnchanged() {
        // Given
        when(reader.lastUpdated(null))
            .thenReturn(1000L)
            .thenReturn(1000L);

        // When
        fileWatchService.watchFiles();
        fileWatchService.watchFiles();

        // Then
        verify(messagingTemplate, times(1))
            .convertAndSend("/topic/refresh", "refresh");
    }
}
