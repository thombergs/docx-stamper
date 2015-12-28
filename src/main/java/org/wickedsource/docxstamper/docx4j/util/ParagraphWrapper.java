package org.wickedsource.docxstamper.docx4j.util;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.docx4j.IndexedRun;

import java.util.ArrayList;
import java.util.List;

/**
 * A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
 * relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
 * runs a word or a string of words is spread.
 * <p/>
 * This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text spans.
 * Call addRun() to add all runs that should be aggregated. Then, call methods to modify the aggregated text. Finally,
 * call getText() or getRuns() to get the modified text or the list of modified runs.
 */
public class ParagraphWrapper {

    private int currentPosition = 0;

    private List<IndexedRun> runs = new ArrayList<>();

    private P paragraph;

    public ParagraphWrapper(P paragraph) {
        this.paragraph = paragraph;
        for (Object contentElement : paragraph.getContent()) {
            if (contentElement instanceof R && !"".equals(RunUtil.getText((R) contentElement))) {
                this.addRun((R) contentElement);
            }
        }
    }

    /**
     * Adds a run to the aggregation.
     *
     * @param run the run to add.
     */
    private void addRun(R run) {
        int startIndex = currentPosition;
        int endIndex = currentPosition + RunUtil.getText(run).length() - 1;
        runs.add(new IndexedRun(startIndex, endIndex, run));
        currentPosition = endIndex + 1;
    }

    /**
     * Replaces the first occurrence of the given placeholder by the given string.
     *
     * @param placeholder the placeholder to replace.
     * @param replacement the string to replace the placeholder.
     */
    public void replaceFirst(String placeholder, String replacement) {
        String text = getText();
        int matchStartIndex = text.indexOf(placeholder);
        int matchEndIndex = matchStartIndex + placeholder.length() - 1;
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean isFirstRun = true;
        boolean isLastRun = affectedRuns.size() == 1;
        int currentRun = 0;
        for (IndexedRun run : affectedRuns) {

            if (isFirstRun) {
                // put the whole replacement into the first affected run
                run.replace(matchStartIndex, matchEndIndex, replacement);
            } else if (isLastRun) {
                // replace the last part of the match with empty string
                run.replace(run.getStartIndex(), matchEndIndex, "");
            } else {
                // the run is in the middle of the match...we simply remove it
                this.paragraph.getContent().remove(run.getRun());
                this.runs.remove(run);
            }

            currentRun++;
            isFirstRun = false;
            isLastRun = currentRun == affectedRuns.size() - 1;
        }
    }

    private List<IndexedRun> getAffectedRuns(int startIndex, int endIndex) {
        List<IndexedRun> affectedRuns = new ArrayList<>();
        for (IndexedRun run : runs) {
            if (run.isTouchedByRange(startIndex, endIndex)) {
                affectedRuns.add(run);
            }
        }
        return affectedRuns;
    }


    /**
     * Returns the aggregated text over all runs.
     *
     * @return the text of all runs.
     */
    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (IndexedRun run : runs) {
            builder.append(RunUtil.getText(run.getRun()));
        }
        return builder.toString();
    }

    /**
     * Returns the list of runs that are aggregated. Depending on what modifications were done to the aggregated text
     * this list may not return the same runs that were initially added to the aggregator.
     *
     * @return the list of aggregated runs.
     */
    public List<R> getRuns() {
        List<R> resultList = new ArrayList<>();
        for (IndexedRun run : runs) {
            resultList.add(run.getRun());
        }
        return resultList;
    }

}
