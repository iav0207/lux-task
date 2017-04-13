import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Created by takoe on 12.04.17.
 */
public class KeyHandler {

    public interface FailuresListener {
        void manage(Key keyThatFailedToBeProcessed);
    }

    private ExternalSystem externalSystem;

    private FailuresListener failuresListener;

    private ConcurrentHashMap.KeySetView<Key, Boolean> keySet = ConcurrentHashMap.newKeySet();

    public KeyHandler(ExternalSystem externalSystem) {
        this.externalSystem = requireNonNull(externalSystem);
    }

    public KeyHandler(ExternalSystem externalSystem, FailuresListener failuresListener) {
        this(externalSystem);
        this.failuresListener = failuresListener;
    }

    /**
     * Submits a key to be asynchronously processed at the {@link ExternalSystem}.
     * If there is an equal key currently in processing, an async task awaits
     * until that one finishes, and then sends the given {@link Key} to the {@link ExternalSystem}.
     *
     * @param key a key instance to be sent to the external system.
     */
    public void handle(Key key) {
        CompletableFuture.supplyAsync(() -> submitForProcessing(key));
    }

    /**
     * Submits a key to be processed at the {@link ExternalSystem}.
     * Awaits if key is reserved.
     *
     * Prints a log message if processing succeeded.
     * If the key is failed to be processed, prints a message to log
     * and sends the key to a {@link FailuresListener}, if present, for further actions.
     *
     * @param key a key instance to be sent to the external system.
     * @return {@link CompletionStage}
     */
    private CompletionStage<Key> submitForProcessing(Key key) {
        try {

            synchronized (this) {
                waitIfKeyIsCurrentlyInProcessing(key);
                reserve(key);
                notify();
            }
            sendToExternalSystem(key);
            logSuccess(key);

        } catch (KeyNotProcessedException ex) {
            reportKeyFailed(ex);

        } finally {
            release(key);
            synchronized (this) {
                notifyAll();
            }
        }
        return CompletableFuture.completedFuture(key);
    }

    private void waitIfKeyIsCurrentlyInProcessing(Key key) {
        try {
            while (isReserved(key)) {
                wait();
            }
        } catch (InterruptedException ex) {
            throw keyNotProcessed("Thread interrupted while waiting for the key to release", ex, key);
        }
    }

    private void sendToExternalSystem(Key key) {
        try {
            externalSystem.process(key);
        } catch (RuntimeException ex) {
            throw keyNotProcessed("Exception caught from ext system", ex, key);
        }
    }

    private boolean isReserved(Key key) {
        return keySet.contains(key);
    }

    private void reserve(Key key) {
        keySet.add(key);
    }

    private void release(Key key) {
        keySet.remove(key);
    }

    private CompletionStage<Key> reportKeyFailed(KeyNotProcessedException ex) {
        System.out.println("Key was not processed: " + ex.getKey());
        ex.printStackTrace();
        if (failuresListener != null)
            failuresListener.manage(ex.getKey());
        return CompletableFuture.completedFuture(ex.getKey());
    }

    private static KeyNotProcessedException keyNotProcessed(String msg, Exception cause, Key key) {
        return new KeyNotProcessedException(msg, cause, key);
    }

    private static void logSuccess(Key key) {
        System.out.println("Key processed successfully: " + key);
    }

}
