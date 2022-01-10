package org.cjoakim.rdf2cosmos.gremlin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Instances of this class represent a single Property for a Vertex or Edge
 * in a Gremlin/CosmosDB graph database.  These Property objects are added
 * to GraphNode instances.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class Property {

    // Instance variables:
    protected String name;
    protected String value;
    protected String dataType;

    private Property() {

        super();
    }

    public Property(String name, String value) {

        super();
        this.name = name;
        this.value = value;
        this.dataType = "string";
    }

    public Property(String name, String value, String dataType) {

        super();
        this.name = name;
        this.value = value;
        this.dataType = dataType;
    }

    public boolean isValid() {

        if (this.name == null) {
            return false;
        }
        if (this.value == null) {
            return false;
        }
        if (this.dataType == null) {
            return false;
        }
        return true;
    }

    public String getName() {

        return name;
    }

    public String getValue() {

        return value;
    }

    public String getDataType() {

        return dataType;
    }

    // Transformation methods

    public String toJson() {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
            return mapper.writeValueAsString(this);
        }
        catch (Exception e) {
            return null;
        }
    }
}
