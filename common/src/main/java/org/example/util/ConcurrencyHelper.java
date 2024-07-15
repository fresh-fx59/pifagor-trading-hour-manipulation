package org.example.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrencyHelper {

    public static void interruptThread(String threadName) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals(threadName)) {
                log.info("interrupting thread {}", threadName);
                thread.interrupt();
                break;
            }
        }
    }
    public static void sleepMillis(int sleepTime, String message) {
        try {
            if (message != null && !message.isBlank() && !message.isEmpty())
                log.info(message);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            log.error("failed to sleep", ex);
            Thread.currentThread().interrupt();
        }
    }
}
