package com.github.benisraelnir.gripme.service;

import com.github.benisraelnir.gripme.core.reader.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class FileWatchService {
    private final SimpMessagingTemplate messagingTemplate;
    private final Reader reader;
    private Long lastModifiedTime;
    private boolean initialized = false;

    @Autowired
    public FileWatchService(SimpMessagingTemplate messagingTemplate, Reader reader) {
        this.messagingTemplate = messagingTemplate;
        this.reader = reader;
    }

    @Scheduled(fixedDelay = 1000)
    public void watchFiles() {
        Long currentModified = reader.lastUpdated(null);
        if (currentModified != null) {
            if (!initialized) {
                lastModifiedTime = currentModified;
                initialized = true;
            } else if (!currentModified.equals(lastModifiedTime)) {
                lastModifiedTime = currentModified;
                messagingTemplate.convertAndSend("/topic/refresh", "refresh");
            }
        }
    }
}
