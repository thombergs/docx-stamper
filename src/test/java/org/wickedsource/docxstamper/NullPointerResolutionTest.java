package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.utils.TestDocxStamper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NullPointerResolutionTest {
    @Test
    void nullPointerResolutionTest_testThrowingCase() throws IOException {
        var subContext = new SubContext("Fullish2",
                                        List.of("Fullish3", "Fullish4",
                                                "Fullish5"));
        var context = new NullishContext("Fullish1", subContext, null, null);
        try (var template = getClass().getResourceAsStream(
                "NullPointerResolution.docx")) {
            var stamper = new TestDocxStamper<NullishContext>();
            assertThrows(
                    DocxStamperException.class,
                    () -> stamper.stampAndLoadAndExtract(template, context)
            );
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
}
