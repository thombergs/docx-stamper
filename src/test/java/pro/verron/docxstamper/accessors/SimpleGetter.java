package pro.verron.docxstamper.accessors;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

public class SimpleGetter implements PropertyAccessor {

    private final String fieldName;

    private final Object value;

    public SimpleGetter(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return null;
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) {
        return true;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) {
        if (name.equals(this.fieldName)) {
            return new TypedValue(value);
        } else {
            return null;
        }
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) {
    }
}
