/**
 * Created by takoe on 12.04.17.
 */
class KeyNotProcessedException extends RuntimeException {

    private final Key key;

    KeyNotProcessedException(String message, Throwable cause, Key key) {
        super(message, cause);
        this.key = key;
    }

    Key getKey() {
        return key;
    }

}
