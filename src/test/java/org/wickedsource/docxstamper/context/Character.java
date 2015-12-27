package org.wickedsource.docxstamper.context;

public class Character {

    private final String name;

    private final String actor;

    public Character(String name, String actor) {
        this.name = name;
        this.actor = actor;
    }

    public String getName() {
        return name;
    }

    public String getActor() {
        return actor;
    }
}
