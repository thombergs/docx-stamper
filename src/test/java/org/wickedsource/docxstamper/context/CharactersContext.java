package org.wickedsource.docxstamper.context;

import java.util.ArrayList;
import java.util.List;

public class CharactersContext {

    private final List<Character> characters;

    public CharactersContext() {
        this.characters = new ArrayList<>();
    }

    public CharactersContext(List<Character> characters) {
        this.characters = characters;
    }

    public List<Character> getCharacters() {
        return characters;
    }
}
