package org.wickedsource.docxstamper.util;

import org.docx4j.wml.R;

public class IndexedRun {

    private final int startIndex;

    private final int endIndex;

    private final int indexInParent;

    private final R run;

    public IndexedRun(int startIndex, int endIndex, int indexInParent, R run) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.indexInParent = indexInParent;
        this.run = run;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getIndexInParent() {
        return indexInParent;
    }

    public R getRun() {
        return run;
    }

    /**
     * Determines whether the specified range of start and end index touches this run.
     */
    public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
        return ((startIndex >= globalStartIndex) && (startIndex <= globalEndIndex))
                || ((endIndex >= globalStartIndex) && (endIndex <= globalEndIndex))
                || ((startIndex <= globalStartIndex) && (endIndex >= globalEndIndex));

    }

    /**
     * Replaces the substring starting at the given index with the given replacement string.
     *
     * @param globalStartIndex the global index (meaning the index relative to multiple aggregated runs) at which to start the replacement.
     * @param globalEndIndex   the global index (meaning the index relative to multiple aggregated runs) at which to end the replacement.
     * @param replacement      the string to replace the substring at the specified global index.
     */
    public void replace(int globalStartIndex, int globalEndIndex, String replacement) {
        int localStartIndex = globalIndexToLocalIndex(globalStartIndex);
        int localEndIndex = globalIndexToLocalIndex(globalEndIndex);
        String text = RunUtil.getText(run);
        text = text.substring(0, localStartIndex);
        text += replacement;
        String runText = RunUtil.getText(run);
        if (runText.length() > 0) {
            text += RunUtil.getText(run).substring(localEndIndex + 1);
        }
        RunUtil.setText(run, text);
    }

    private int globalIndexToLocalIndex(int globalIndex) {
        if (globalIndex < startIndex) {
            return 0;
        } else if (globalIndex > endIndex) {
            return RunUtil.getText(run).length() - 1;
        } else {
            return globalIndex - startIndex;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexedRun)) return false;

        IndexedRun that = (IndexedRun) o;

        if (endIndex != that.endIndex) return false;
        if (indexInParent != that.indexInParent) return false;
        return startIndex == that.startIndex;
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + indexInParent;
        return result;
    }

    @Override
    public String toString() {
        return String.format("[IndexedRun: startIndex=%d; endIndex=%d; indexInParent=%d text=%s}", startIndex, endIndex, indexInParent, RunUtil.getText(run));
    }
}
