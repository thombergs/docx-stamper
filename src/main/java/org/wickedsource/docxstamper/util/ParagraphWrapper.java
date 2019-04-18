package org.wickedsource.docxstamper.util;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.wickedsource.docxstamper.replace.IndexedRun;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void recalculateRuns() {
        currentPosition = 0;
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
     * Replaces the given placeholder String with the replacement object within the paragraph.
     * The replacement object must be a valid DOCX4J Object.
     *
     * @param placeholder the placeholder to be replaced.
     * @param replacement the object to replace the placeholder String.
     */
    public void replace(String placeholder, Object replacement) {
        String text = getText();
        int matchStartIndex = text.indexOf(placeholder);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + placeholder.length() - 1;
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            IndexedRun run = affectedRuns.get(0);

            boolean placeholderSpansCompleteRun = placeholder.length() == RunUtil.getText(run.getRun()).length();
            boolean placeholderAtStartOfRun = matchStartIndex == run.getStartIndex();
            boolean placeholderAtEndOfRun = matchEndIndex == run.getEndIndex();
            boolean placeholderWithinRun = matchStartIndex > run.getStartIndex() && matchEndIndex < run.getEndIndex();

            if (placeholderSpansCompleteRun) {
                this.paragraph.getContent().remove(run.getRun());
                this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                recalculateRuns();
            } else if (placeholderAtStartOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                recalculateRuns();
            } else if (placeholderAtEndOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                this.paragraph.getContent().add(run.getIndexInParent() + 1, replacement);
                recalculateRuns();
            } else if (placeholderWithinRun) {
                String runText = RunUtil.getText(run.getRun());
                int startIndex = runText.indexOf(placeholder);
                int endIndex = startIndex + placeholder.length();
                R run1 = RunUtil.create(runText.substring(0, startIndex), this.paragraph);
                R run2 = RunUtil.create(runText.substring(endIndex), this.paragraph);
                this.paragraph.getContent().add(run.getIndexInParent(), run2);
                this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                this.paragraph.getContent().add(run.getIndexInParent(), run1);
                this.paragraph.getContent().remove(run.getRun());
                recalculateRuns();
            }

        } else {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);

            // remove the placeholder from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (IndexedRun run : affectedRuns) {
                if (run != firstRun && run != lastRun) {
                    this.paragraph.getContent().remove(run.getRun());
                }
            }

            // add replacement run between first and last run
            this.paragraph.getContent().add(firstRun.getIndexInParent() + 1, replacement);

            recalculateRuns();
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
        return getText(this.runs);
    }

    /**
     * Returns each Placeholder and its run.
     *
     * @return the placeholder and its run as linkedHashMap
     */
    public LinkedHashMap<String, RPr> getPlaceholderAndStyle() {
        return getPlaceholderAndStyle(this.runs);
    }

    private String getText(List<IndexedRun> runs) {
        StringBuilder builder = new StringBuilder();
        for (IndexedRun run : runs) {
            builder.append(RunUtil.getText(run.getRun()));
        }
        return builder.toString();
    }

    private LinkedHashMap<String, RPr> getPlaceholderAndStyle(List<IndexedRun> runs) {
        LinkedHashMap<String, RPr> placeholderRpr = new LinkedHashMap<>();
        StringBuilder builder = new StringBuilder();

        for (IndexedRun run : runs) {
            builder.append(RunUtil.getText(run.getRun()));
            Pattern p = Pattern.compile("\\$\\{.*?}");
            Matcher m = p.matcher(builder.toString());
            RPr rPr = new RPr();
            String placeholder = "";
            int index = 0;
            while (m.find(index)) {
                builder = new StringBuilder();
                rPr = run.getRun().getRPr();
                placeholder = m.group();
                index = m.end();
            }
            if (!"".equals(placeholder)) {
                placeholderRpr.put(placeholder+"_"+run.getStartIndex(), rPr);
            }

        }

        return placeholderRpr;
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

    @Override
    public String toString() {
        return getText();
    }

    public P getParagraph() {
        return paragraph;
    }
}
