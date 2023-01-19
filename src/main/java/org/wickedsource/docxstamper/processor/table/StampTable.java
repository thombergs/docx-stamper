package org.wickedsource.docxstamper.processor.table;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class StampTable {
    private final List<String> headers;
    private final List<List<String>> records;

    public StampTable() {
        this.headers = new ArrayList<>();
        this.records = new ArrayList<>();
    }

    public StampTable(
            List<String> headers,
            List<List<String>> records
    ) {
        this.headers = headers;
        this.records = records;
    }

    public List<String> headers() {
        return headers;
    }

    public List<List<String>> records() {
        return records;
    }

    public static StampTable empty() {
        return new StampTable(singletonList("placeholder"), singletonList(singletonList("placeholder")));
    }
}
