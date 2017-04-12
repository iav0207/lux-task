import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by takoe on 12.04.17.
 */
public class ExternalSystemTestImpl implements ExternalSystem {

    private ConcurrentHashMap.KeySetView<Key, Boolean> keySet = ConcurrentHashMap.newKeySet();

    @Override
    public void process(Key key) {
        if (keySet.contains(key)) throw new RuntimeException();
        keySet.add(key);
        key.process();
        keySet.remove(key);
    }

}
