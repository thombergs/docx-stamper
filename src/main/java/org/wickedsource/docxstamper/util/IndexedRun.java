package org.wickedsource.docxstamper.util;

import org.docx4j.wml.R;

/**
 * Represents a run (i.e. a text fragment) in a paragraph. The run is indexed relative to the containing paragraph
 * and also relative to the containing document.
 * @param startIndex    the start index of the run relative to the containing paragraph.
 * @param endIndex       the end index of the run relative to the containing paragraph.
 * @param indexInParent  the index of the run relative to the containing document.
 * @param run            the run itself.
 */
public record IndexedRun(int startIndex, int endIndex, int indexInParent, R run) {

    /**
     * Determines whether the specified range of start and end index touches this run.
     * <p>
     * Example:
     * <p>
     * Given this run: [a,b,c,d,e,f,g,h,i,j]
     * <p>
     * And the range [2,5]
     * <p>
     * This method will return true, because the range touches the run at the indices 2, 3, 4 and 5.
     *
     * @param globalStartIndex the global index (meaning the index relative to multiple aggregated runs) at which to start the range.
     * @param globalEndIndex   the global index (meaning the index relative to multiple aggregated runs) at which to end the range.
     * @return true, if the range touches this run, false otherwise.
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
        if (!runText.isEmpty()) {
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
        if (!(o instanceof IndexedRun that)) return false;
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
