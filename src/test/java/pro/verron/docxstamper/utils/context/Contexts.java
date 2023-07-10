package pro.verron.docxstamper.utils.context;


public class Contexts {
    private Contexts() {
        throw new RuntimeException("Static utility class should not be instantiated");
    }

    public static Object empty() {
        record EmptyContext() {
        }
        return new EmptyContext();
    }

    public static Object name(String name) {
        record Name(String name) {
        }
        return new Name(name);
    }
}
