package com.github.benisraelnir.gripme.service;

import com.github.benisraelnir.gripme.core.reader.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@EnableScheduling
public class FileWatchService {
    private final SimpMessagingTemplate messagingTemplate;
    private final Reader reader;
    private final Map<String, Long> lastModifiedTimes = new HashMap<>();

    @Autowired
    public FileWatchService(SimpMessagingTemplate messagingTemplate, Reader reader) {
        this.messagingTemplate = messagingTemplate;
        this.reader = reader;
    }

    @Scheduled(fixedDelay = 1000)
    public void watchFiles() {
        String currentPath = null; // Default path
        Long lastModified = reader.lastUpdated(currentPath);

        if (lastModified != null) {
            Long previousModified = lastModifiedTimes.get(currentPath);
            if (previousModified == null || !previousModified.equals(lastModified)) {
                lastModifiedTimes.put(currentPath, lastModified);
                messagingTemplate.convertAndSend("/topic/refresh", "refresh");
            }
        }
    }
}
