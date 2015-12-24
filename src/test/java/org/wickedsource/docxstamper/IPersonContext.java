package org.wickedsource.docxstamper;

import java.util.List;

/**
 * Created by Tom on 20.12.2015.
 */
public interface IPersonContext {
    String getName();

    void setName(String name);

    Integer getAge();

    void setAge(Integer age);

    List<Location> getLocations();

    void setLocations(List<Location> locations);
}
