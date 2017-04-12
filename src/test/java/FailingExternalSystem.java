/**
 * Created by takoe on 12.04.17.
 */
public class FailingExternalSystem implements ExternalSystem {

    @Override
    public void process(Key key) {
        throw new RuntimeException();
    }

}
