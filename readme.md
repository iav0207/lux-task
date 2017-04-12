#### Task

In our system we have a class KeyHandler which delegates key processing
to some external system (see code below).
Key is a simple value class with correct equals() and hashCode() defined.
Method handle(Key key) is called from multiple threads (8 threads).
External system can process different keys at the same time just fine,
but if it's called with equal keys at the same time it will break.

Implement method handle() to prevent simultaneous processing of equal keys.
All keys should be processed eventually, even if some of them are equal.

```java
public class KeyHandler {
    private ExternalSystem externalSystem;
                
    public void handle(Key key) {
        externalSystem.process(key);
    }
}
```