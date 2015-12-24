package org.wickedsource.docxstamper;

import org.apache.poi.xwpf.usermodel.XWPFRun;

public class IndexedRun {

    private final int startIndex;

    private final int endIndex;

    private final XWPFRun run;

    public IndexedRun(int startIndex, int endIndex, XWPFRun run) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.run = run;
    }

    public int getLength() {
        return endIndex - startIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public XWPFRun getRun() {
        return run;
    }

    /**
     * Determines whether the specified range of start and end index touches this run.
     */
    public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
        return (startIndex >= globalStartIndex && startIndex <= globalEndIndex)
                || (endIndex >= globalStartIndex && endIndex <= globalEndIndex)
                || (startIndex <= globalStartIndex && endIndex >= globalEndIndex);

    }

    /**
     * Replaces the substring starting at the given index with the given replacement string.
     *
     * @param globalStartIndex the global index (meaning the index relative to multiple aggregated runs) at which to perform the replacement.
     * @param replacement      the string to replace the substring at the specified global index.
     */
    public void replace(int globalStartIndex, int globalEndIndex, String replacement) {
        int localStartIndex = globalIndexToLocalIndex(globalStartIndex);
        int localEndIndex = globalIndexToLocalIndex(globalEndIndex);
        String text = run.getText(0);
        text = text.substring(0, localStartIndex);
        text += replacement;
        text += run.getText(0).substring(localEndIndex + 1);
        run.setText(text, 0);
    }

    private int globalIndexToLocalIndex(int globalIndex) {
        if (globalIndex < startIndex) {
            return 0;
        } else if (globalIndex > endIndex) {
            return run.getText(0).length() - 1;
        } else {
            return globalIndex - startIndex;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexedRun that = (IndexedRun) o;

        if (endIndex != that.endIndex) return false;
        if (startIndex != that.startIndex) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + endIndex;
        return result;
    }

    @Override
    public String toString() {
        return String.format("[IndexedRun: startIndex=%d; endIndex=%d; text=%s}", startIndex, endIndex, run.getText(0));
    }
}
