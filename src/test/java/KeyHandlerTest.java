import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

/**
 * Created by takoe on 12.04.17.
 */
public class KeyHandlerTest {

    private static final int THREAD_COUNT = 8;

    private static Random random = new Random();

    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(THREAD_COUNT);

    private KeyHandler handler;

    private ExternalSystem externalSystem;

    @BeforeClass
    public void init() {
        externalSystem = new ExternalSystemTestImpl();
        handler = new KeyHandler(externalSystem);
    }

    @BeforeTest
    public void reset() {
        cyclicBarrier.reset();
    }

    @AfterMethod
    public void aBitSleep() throws Exception {
        Thread.sleep(2000);
    }

    @Test(threadPoolSize = THREAD_COUNT, invocationCount = THREAD_COUNT)
    public void shouldProcessSuccessfullyIfRandomKeysArePassed() throws Exception {
        Key key = randomKey();
        cyclicBarrier.await();

        handler.handle(key);
    }

    // tests that ext system implementation really cannot process the equal keys simultaneously
    @Test(
            expectedExceptions = RuntimeException.class,
            threadPoolSize = THREAD_COUNT,
            invocationCount = THREAD_COUNT,
            // at least one calling thread will always succeed
            successPercentage = (THREAD_COUNT - 1) * 100 / THREAD_COUNT
    )
    public void shouldExtSystemThrowExceptionIfEqualKeysArePassed() throws Exception {
        Key key = constantKey();
        cyclicBarrier.await();

        externalSystem.process(key);
    }

    // No exceptions expected here. Just watching successful logs printed to sysOut
    @Test(threadPoolSize = THREAD_COUNT, invocationCount = THREAD_COUNT)
    public void shouldHandlerSendEqualKeysToProcessingSequentially() throws Exception {
        Key key = constantKey();
        cyclicBarrier.await();

        handler.handle(key);
    }

    @Test(threadPoolSize = 2 * THREAD_COUNT, invocationCount = 6 * THREAD_COUNT)
    public void shouldBeOkOnIntermixedKeys() throws Exception {
        Key key = random.nextBoolean() ? randomKey() : constantKey();
        cyclicBarrier.await();

        handler.handle(key);
    }

    @Test
    public void shouldSendKeyToFailManagerIfProcessingFailed() throws Exception {
        KeyHandlerFailuresListener failManager = mock(KeyHandlerFailuresListener.class);
        new KeyHandler(new FailingExternalSystem(), failManager).handle(key("A"));
        Thread.sleep(2000);
        verify(failManager, only()).manage(any(Key.class));
    }

    private static Key randomKey() {
        return key(String.valueOf(random.nextInt()));
    }

    private static Key constantKey() {
        return key("A000000000");
    }

    private static Key key(String s) {
        return new StringKey(s);
    }

}