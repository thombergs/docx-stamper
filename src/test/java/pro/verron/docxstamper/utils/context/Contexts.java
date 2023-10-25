package pro.verron.docxstamper.utils.context;

import org.wickedsource.docxstamper.replace.typeresolver.image.Image;

import java.util.*;

/**
 * <p>Contexts class.</p>
 *
 * @author joseph
 * @version $Id: $Id
 * @since 1.6.5
 */
public class Contexts {
    private Contexts() {
        throw new RuntimeException(
                "Static utility class should not be instantiated");
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
            public String name(){
                return name;
            }

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

    public static HashMap<String, Object> subDocPartContext() {
        var context = new HashMap<String, Object>();
        var subDocParts = new ArrayList<Map<String, Object>>();

        var firstPart = new HashMap<String, Object>();
        firstPart.put("name", "first doc part");
        subDocParts.add(firstPart);

        var secondPart = new HashMap<String, Object>();
        secondPart.put("name", "second doc part");
        subDocParts.add(secondPart);

        context.put("subDocParts", subDocParts);
        return context;
    }

    public static SchoolContext schoolContext() {
        List<Grade> grades = new ArrayList<>();
        for (int grade1 = 0; grade1 < 3; grade1++) {
            var classes = new ArrayList<AClass>();
            for (int classroom1 = 0; classroom1 < 3; classroom1++) {
                var students = new ArrayList<Student>();
                for (int i = 0; i < 5; i++) {
                    students.add(new Student(i, "Bruce·No" + i, 1 + i));
                }
                classes.add(new AClass(classroom1, students));
            }
            grades.add(new Grade(grade1, classes));
        }
        return new SchoolContext("South Park Primary School", grades);
    }

    public static HashMap<String, Object> tableContext() {
        var context = new HashMap<String, Object>();

        var firstTable = new ArrayList<TableValue>();
        firstTable.add(new TableValue("firstTable value1"));
        firstTable.add(new TableValue("firstTable value2"));

        var secondTable = new ArrayList<TableValue>();
        secondTable.add(new TableValue("repeatDocPart value1"));
        secondTable.add(new TableValue("repeatDocPart value2"));
        secondTable.add(new TableValue("repeatDocPart value3"));

        List<TableValue> thirdTable = new ArrayList<>();
        thirdTable.add(new TableValue("secondTable value1"));
        thirdTable.add(new TableValue("secondTable value2"));
        thirdTable.add(new TableValue("secondTable value3"));
        thirdTable.add(new TableValue("secondTable value4"));

        context.put("firstTable", firstTable);
        context.put("secondTable", secondTable);
        context.put("thirdTable", thirdTable);
        return context;
    }

    public static Map<String, Object> coupleContext() {
        Map<String, Object> context = new HashMap<>();

        Name name1 = new Name("Homer");
        Name name2 = new Name("Marge");

        List<Name> repeatValues = new ArrayList<>();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);
        return context;
    }

    public static DateContext nowContext() {
        var now = new Date();
        var context = new DateContext(now);
        return context;
    }

    public static HashMap<String, Object> mapAndReflectiveContext() {
        var listProp = new ArrayList<Container>();
        listProp.add(new Container("first value"));
        listProp.add(new Container("second value"));

        var context = new HashMap<String, Object>();
        context.put("FLAT_STRING", "Flat string has been resolved");
        context.put("OBJECT_LIST_PROP", listProp);
        return context;
    }

    public static NullishContext nullishContext() {
        return new NullishContext(
                "Fullish1",
                new SubContext(
                        "Fullish2",
                        List.of(
                                "Fullish3",
                                "Fullish4",
                                "Fullish5"
                        )
                ),
                null,
                null
        );
    }

    public record Role(String name, String actor) {
    }

    public record Characters(List<Role> characters) {
    }

    public record DateContext(Date date) {
    }

    public static class SpacyContext {
        private final String expressionWithLeadingAndTrailingSpace = " Expression ";
        private final String expressionWithLeadingSpace = " Expression";
        private final String expressionWithTrailingSpace = "Expression ";
        private final String expressionWithoutSpaces = "Expression";

        public String getExpressionWithLeadingAndTrailingSpace() {
            return " Expression ";
        }

        public String getExpressionWithLeadingSpace() {
            return " Expression";
        }

        public String getExpressionWithTrailingSpace() {
            return "Expression ";
        }

        public String getExpressionWithoutSpaces() {
            return "Expression";
        }
    }

    public record ImageContext(Image monalisa) {
    }

    static class Container {
        public String value;

        public Container(String value) {
            this.value = value;
        }
    }

    public static final class NullishContext {
        private String fullish_value;
        private SubContext fullish;
        private String nullish_value;
        private SubContext nullish;

        public NullishContext() {
        }

        public NullishContext(
                String fullish_value,
                SubContext fullish,
                String nullish_value,
                SubContext nullish
        ) {
            this.fullish_value = fullish_value;
            this.fullish = fullish;
            this.nullish_value = nullish_value;
            this.nullish = nullish;
        }

        public String getFullish_value() {
            return fullish_value;
        }

        public void setFullish_value(String fullish_value) {
            this.fullish_value = fullish_value;
        }

        public SubContext getFullish() {
            return fullish;
        }

        public void setFullish(SubContext fullish) {
            this.fullish = fullish;
        }

        public String getNullish_value() {
            return nullish_value;
        }

        public void setNullish_value(String nullish_value) {
            this.nullish_value = nullish_value;
        }

        public SubContext getNullish() {
            return nullish;
        }

        public void setNullish(SubContext nullish) {
            this.nullish = nullish;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NullishContext) obj;
            return Objects.equals(this.fullish_value, that.fullish_value) &&
                   Objects.equals(this.fullish, that.fullish) &&
                   Objects.equals(this.nullish_value, that.nullish_value) &&
                   Objects.equals(this.nullish, that.nullish);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fullish_value, fullish, nullish_value, nullish);
        }

        @Override
        public String toString() {
            return "NullishContext[" +
                   "fullish_value=" + fullish_value + ", " +
                   "fullish=" + fullish + ", " +
                   "nullish_value=" + nullish_value + ", " +
                   "nullish=" + nullish + ']';
        }

    }

    public static final class SubContext {
        private String value;
        private List<String> li;

        public SubContext() {
        }

        public SubContext(
                String value,
                List<String> li
        ) {
            this.value = value;
            this.li = li;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public List<String> getLi() {
            return li;
        }

        public void setLi(List<String> li) {
            this.li = li;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SubContext) obj;
            return Objects.equals(this.value, that.value) &&
                   Objects.equals(this.li, that.li);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, li);
        }

        @Override
        public String toString() {
            return "SubContext[" +
                   "value=" + value + ", " +
                   "li=" + li + ']';
        }

    }

    record SchoolContext(String schoolName, List<Grade> grades) {}

    record Grade(int number, List<AClass> classes) {}

    public record Show(List<CharacterRecord> characters) {}

    public record CharacterRecord(int index, String indexSuffix, String characterName, String actorName) {}

    public record Character(String name, String actor) {}


    public record AClass(int number, List<Student> students) {}

    record Student(int number, String name, int age) {}

    static class TableValue {
        public String value;

        TableValue(String value) {
            this.value = value;
        }
    }

    public record Name(String name) {}

    public static class EmptyContext {
    }

    public record Context(CustomType name) {}

    public static class CustomType {}
}
