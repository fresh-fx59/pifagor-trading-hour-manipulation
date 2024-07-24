import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConcurrentQueueTest {
    @Test
    public void linkedQueueTest() {
        //given
        LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
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
        senderThread.start();
        receiverThread.start();

        //then
        assertThat(messages).isEmpty();
    }

    @Test
    public void linkedQueueThreadPoolTest() {
        //given
        LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
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

        //then
        assertThat(messages).isEmpty();
    }
}
