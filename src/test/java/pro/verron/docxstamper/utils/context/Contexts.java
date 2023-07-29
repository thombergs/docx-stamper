package pro.verron.docxstamper.utils.context;


import java.util.List;

/**
 * <p>Contexts class.</p>
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public class Contexts {
    private Contexts() {
        throw new RuntimeException("Static utility class should not be instantiated");
    }

    /**
     * <p>empty.</p>
     *
     * @return a {@link java.lang.Object} object
     */
    public static Object empty() {
        record EmptyContext() {
        }
        return new EmptyContext();
    }

    /**
     * <p>name.</p>
     *
     * @param name a {@link java.lang.String} object
     * @return a {@link java.lang.Object} object
     */
    public static Object name(String name) {
        record Name(String name) {
        }
        return new Name(name);
    }

    /**
     * <p>role.</p>
     *
     * @param character       a {@link java.lang.String} object
     * @param danCastellaneta a {@link java.lang.String} object
     * @return a {@link pro.verron.docxstamper.utils.context.Contexts.Role} object
     */
    public static Role role(String character, String danCastellaneta) {
        return new Role(character, danCastellaneta);
    }

    /**
     * <p>roles.</p>
     *
     * @param roles a {@link pro.verron.docxstamper.utils.context.Contexts.Role} object
     * @return a {@link pro.verron.docxstamper.utils.context.Contexts.Characters} object
     */
    public static Characters roles(Role... roles) {
        return new Characters(List.of(roles));
    }

    public record Role(String name, String actor) {
    }

    public record Characters(List<Role> characters) {
    }
}
