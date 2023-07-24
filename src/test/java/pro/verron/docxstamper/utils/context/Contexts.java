package pro.verron.docxstamper.utils.context;


import java.util.List;

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

    public static Role role(String character, String danCastellaneta) {
        return new Role(character, danCastellaneta);
    }

    public static Characters roles(Role... roles) {
        return new Characters(List.of(roles));
    }

    public record Role(String name, String actor) {
    }

    public record Characters(List<Role> characters) {
    }
}
