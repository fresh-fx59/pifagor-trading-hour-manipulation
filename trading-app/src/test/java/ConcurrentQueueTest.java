import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConcurrentQueueTest {
    private final int waitingTimeMaxMs = 2000;
    final int sleepTimeMillis = 100;

    @Test
    public void linkedQueueTest() throws InterruptedException {
        //given
        boolean shouldRun = true;
        LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        int cyclesCount = 5;
        Thread senderThread = new Thread(() -> {
            for(int i = 0; i < cyclesCount; i++) {
                try {
                    final String message = "message " + i;
                    messages.put(message);
                    log.info(message + " put");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread receiverThread = new Thread(() -> {
            for(int i = 0; i < cyclesCount; i++) {
                try {
                    final String message = messages.take();
                    log.info(message + " was taken");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //when
        senderThread.start();
        receiverThread.start();

        //then
        while (!messages.isEmpty()) {
            Thread.sleep(sleepTimeMillis);
            log.info("waiting for queue to be empty for {} milliseconds...", sleepTimeMillis);
        }

        senderThread.join(waitingTimeMaxMs);
        receiverThread.join(waitingTimeMaxMs);

        assertThat(messages).size().isEqualTo(0);
    }

    @Test
    public void linkedQueueThreadPoolTest() throws InterruptedException {
        //given
        final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        Thread senderThread = new Thread(() -> {
            int cyclesCount = 5;
            for(int i = 0; i < cyclesCount; i++) {
                try {
                    final String message = "message " + i;
                    messages.put(message);
                    log.info(message + " put");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread receiverThread = new Thread(() -> {
            while (true) {
                try {
                    final String message = messages.take();
                    log.info(message + " was taken");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        //when
        executorService.execute(senderThread);
        executorService.execute(receiverThread);

        while (!messages.isEmpty()) {
            Thread.sleep(sleepTimeMillis);
            log.info("waiting for queue to be empty for {} milliseconds...", sleepTimeMillis);
        }

        senderThread.join(waitingTimeMaxMs);
        receiverThread.join(waitingTimeMaxMs);

        //then
        assertThat(messages.size()).isEqualTo(0);
    }
}
