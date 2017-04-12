import static java.util.Objects.requireNonNull;

/**
 * Created by takoe on 12.04.17.
 */
public class StringKey implements Key {

    private static final int loops = 1_000_000;

    private String value;

    public StringKey(String value) {
        this.value = requireNonNull(value);
    }

    @Override
    public void process() {
        String s = value;
        // some dummy operations that take some time
        for (int i = 0; i < loops; i++)
            s = s.substring(1) + s.charAt(0);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;

        StringKey stringKey = (StringKey) that;

        return value.equals(stringKey.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "StringKey{" +
                "value='" + value + '\'' +
                '}';
    }
}
