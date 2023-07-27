package org.wickedsource.docxstamper.processor.table;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Represents a table with several columns, a headers line, and several lines of content
 *
 * @author joseph
 * @version $Id: $Id
 */
public class StampTable {
    private final List<String> headers;
    private final List<List<String>> records;

    /**
     * Instantiate an empty table
     */
    public StampTable() {
        this.headers = new ArrayList<>();
        this.records = new ArrayList<>();
    }

    /**
     * Instantiate a table with headers and several lines
     *
     * @param headers the header lines
     * @param records the lines that the table should contains
     */
    public StampTable(
            List<String> headers,
            List<List<String>> records
    ) {
        this.headers = headers;
        this.records = records;
    }

    /**
     * <p>empty.</p>
     *
     * @return a {@link org.wickedsource.docxstamper.processor.table.StampTable} object
     */
    public static StampTable empty() {
        return new StampTable(singletonList("placeholder"), singletonList(singletonList("placeholder")));
    }

    /**
     * <p>headers.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<String> headers() {
        return headers;
    }

    /**
     * <p>records.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<List<String>> records() {
        return records;
    }
}
