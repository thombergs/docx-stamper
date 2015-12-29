package org.wickedsource.docxstamper.docx4j.replace;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.docx4j.util.RunUtil;

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
        recalculateRuns();
    }

    private void recalculateRuns() {
        this.runs.clear();
        int index = 0;
        for (Object contentElement : paragraph.getContent()) {
            if (contentElement instanceof R && !"".equals(RunUtil.getText((R) contentElement))) {
                this.addRun((R) contentElement, index);
            }
            index++;
        }
    }

    /**
     * Adds a run to the aggregation.
     *
     * @param run the run to add.
     */
    private void addRun(R run, int index) {
        int startIndex = currentPosition;
        int endIndex = currentPosition + RunUtil.getText(run).length() - 1;
        runs.add(new IndexedRun(startIndex, endIndex, index, run));
        currentPosition = endIndex + 1;
    }

    /**
     * Removes the specified placeholder from the runs that are touched by it and returns the index at which
     * a replacement for the placeholder may be inserted into the paragraph.
     *
     * @param placeholder the placeholder which is to be removed from the paragraph.
     */
    public int cleanPlaceholder(String placeholder) {
        int replacementIndex = 0;
        String text = getText();
        int matchStartIndex = text.indexOf(placeholder);
        int matchEndIndex = matchStartIndex + placeholder.length() - 1;
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean isFirstRun = true;
        boolean isLastRun = affectedRuns.size() == 1;
        int currentRun = 0;
        for (IndexedRun run : affectedRuns) {

            if (isFirstRun && isLastRun) {
                if (placeholder.length() == RunUtil.getText(run.getRun()).length()) {
                    // the placeholder is the whole run, simply remove the run and return its index
                    this.paragraph.getContent().remove(run.getRun());
                    replacementIndex = run.getIndexInParent();
                    recalculateRuns();
                } else {
                    // cut the run in two parts left and right of the match
                    String runText = RunUtil.getText(run.getRun());
                    R run1 = RunUtil.create(runText.substring(0, matchStartIndex));
                    R run2 = RunUtil.create(runText.substring(matchEndIndex + 1));
                    this.paragraph.getContent().add(run.getIndexInParent(), run2);
                    this.paragraph.getContent().add(run.getIndexInParent(), run1);
                    this.paragraph.getContent().remove(run.getRun());
                    replacementIndex = run.getIndexInParent() + 1;
                    recalculateRuns();
                }
            } else if (isFirstRun) {
                // put the whole replacement into the first affected run
                run.replace(matchStartIndex, matchEndIndex, "");
                replacementIndex = run.getIndexInParent();
            } else if (isLastRun) {
                // replace the last part of the match with empty string
                run.replace(run.getStartIndex(), matchEndIndex, "");
            } else {
                // the run is in the middle of the match...we simply remove it
                this.paragraph.getContent().remove(run.getRun());
                recalculateRuns();
            }

            currentRun++;
            isFirstRun = false;
            isLastRun = currentRun == affectedRuns.size() - 1;
        }
        return replacementIndex;
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
