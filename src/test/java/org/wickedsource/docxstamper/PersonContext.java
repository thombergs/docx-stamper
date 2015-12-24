package org.wickedsource.docxstamper;

import java.util.ArrayList;
import java.util.List;

public class PersonContext implements IPersonContext {

    private String name;

    private Integer age;

    private List<Location> locations = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getAge() {
        return age;
    }

    @Override
    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
