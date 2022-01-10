package org.cjoakim.rdf2cosmos.gremlin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Instances of this class represent either parsed RDF data for a Vertex or Edge
 * in a Gremlin/CosmosDB graph database.  They are used as growing/augmented
 * data structures as the AppRdfStream parses the input RDF file(s).
 * They can be serialized to JSON, and cached either on disk or database.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class GraphNode {

    // Constants:
    public static final String TYPE_VERTEX = "vertex";
    public static final String TYPE_EDGE   = "edge";

    // Instance variables:
    protected String type        = null;
    protected String cacheKey    = null;
    protected String vertexId1   = null;
    protected String vertexId2   = null;
    protected String label       = null;
    protected HashMap<String, Property> properties = new HashMap<String, Property>();
    protected long   createdAt   = 0;
    protected long   updatedAt   = 0;
    protected long   convertedAt = 0;

    public static synchronized String edgeCacheKey(String vertexId1, String vertexId2, String label) {

        return "edge__" + vertexId1 + "__" + vertexId2 + "__" + label;
    }

    public static synchronized String vertexCacheKey(String vertexId1) {

        return "vertex__" + vertexId1;
    }

    public GraphNode() {

        super();
    }

    public GraphNode(String type) {

        super();
        this.type = ("" + type).toLowerCase();
    }

    // Getters and Setters below:

    public String getType() {

        return type;
    }

    public String getVertexId1() {

        return vertexId1;
    }

    public void setVertexId1(String vertexId1) {

        this.vertexId1 = vertexId1;
    }

    public String getVertexId2() {

        return vertexId2;
    }

    public void setVertexId2(String vertexId2) {

        this.vertexId2 = vertexId2;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public HashMap<String, Property> getProperties() {

        return properties;
    }

    public void setData(String json) {

    }

    public void setProperties(HashMap<String, Property> properties) {

        this.properties = properties;
    }

    public void setType(String type) {

        this.type = type;
    }

    public void setCacheKey(String cacheKey) {

        this.cacheKey = cacheKey;
    }

    public long getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(long createdAt) {

        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {

        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {

        this.updatedAt = updatedAt;
    }

    public long getConvertedAt() {

        return convertedAt;
    }

    public void setConvertedAt(long convertedAt) {

        this.convertedAt = convertedAt;
    }

    public boolean isVertex() {

        return this.type.equalsIgnoreCase(TYPE_VERTEX);
    }

    public boolean isEdge() {

        return this.type.equalsIgnoreCase(TYPE_EDGE);
    }

    public boolean isValid() {

        if (isVertex()) {
            if (!isPopulated(getVertexId1())) {
                return false;
            }
            if (!isPopulated(getLabel())) {
                return false;
            }
            return true;
        }
        else {
            if (!isPopulated(getVertexId1())) {
                return false;
            }
            if (!isPopulated(getVertexId2())) {
                return false;
            }
            if (!isPopulated(getLabel())) {
                return false;
            }
            return true;
        }
    }

    private boolean isPopulated(String s) {

        if (s == null) {
            return false;
        }
        if (s.strip().length() < 1) {
            return false;
        }
        if (s.equals("null")) {
            return false;
        }
        return true;
    }

    public String getCacheKey() {

        if (isVertex()) {
            this.cacheKey = GraphNode.vertexCacheKey(vertexId1);
            return this.cacheKey;
        }
        else {
            this.cacheKey = GraphNode.edgeCacheKey(vertexId1, vertexId2, label);
            return this.cacheKey;
        }
    }

    // Property methods

    public void addProperty(Property p) {

        if (p != null) {
            if (p.isValid()) {
                properties.put(p.name, p);
            }
        }
    }

    public void addProperty(String name, String value) {

        Property p = new Property(name, value);

        if (p.isValid()) {
            properties.put(p.name, p);
        }
    }

    public void addProperty(String name, String value, String dataType) {

        Property p = new Property(name, value, dataType);
        if (p.isValid()) {
            properties.put(p.name, p);
        }
    }

    public ArrayList<String> getPropertyNames() {

        ArrayList<String> list = new ArrayList<String>();
        Iterator<String> it = properties.keySet().iterator();

        while( it.hasNext()) {
            list.add(it.next());
        }
        Collections.sort(list);
        return list;
    }

    public Property getProperty(String name) {

        if (properties.containsKey(name)) {
            return properties.get(name);
        }
        else {
            return null;
        }
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

    /**
     * "Groovy" is the informal name of the Gremlin syntax that looks like this:
     *  g.addV('library').property('pk','m26-js').property('id','m26-js')
     *  g.V(['MAINT-cjoakim','MAINT-cjoakim']).addE('maintains').to(g.V(['m26-js','m26-js']))
     */
    public String toGroovy() {

        if (isValid()) {
            if (this.isVertex()) {
                return vertexToGroovy();
            }
            else {
                return edgeToGroovy();
            }
        }
        else {
            return null;
        }
    }

    private String vertexToGroovy() {
        // Sample:
        // g.addV('library').property('pk','m26-js').property('id','m26-js').property('desc','A Node.js library for speed and pace calculations for sports like running and cycling. Age-graded times and heart-rate training-zones are also supported.').property('name','m26-js')

        StringBuffer sb = new StringBuffer();
        sb.append("g.addV('" + scrubValue(getLabel()) + "')");
        appendGroovyProperty(sb, "id", getVertexId1(), null);
        appendGroovyProperty(sb, "pk", getVertexId1(), null);

        ArrayList<String> propNames = this.getPropertyNames();
        int propCount = propNames.size();
        if (propCount > 0) {
            for (int i = 0; i < propCount; i++) {
                String name = propNames.get(i);
                Property p = getProperty(name);
                String value = p.getValue();
                String dType = p.getDataType();
                appendGroovyProperty(sb, name, value, dType);
            }
        }
        return sb.toString();
    }

    private String edgeToGroovy() {

        // Samples:
        // g.V(['MAINT-cjoakim','MAINT-cjoakim']).addE('maintains').to(g.V(['m26-js','m26-js']))

        String id1 = scrubValue(getVertexId1());
        String id2 = scrubValue(getVertexId2());

        StringBuffer sb = new StringBuffer();
        sb.append("g.V(['");
        sb.append(id1);
        sb.append("','");
        sb.append(id1);
        sb.append("']).addE('");
        sb.append(scrubValue(getLabel()));
        sb.append("').to(g.V(['");
        sb.append(id2);
        sb.append("','");
        sb.append(id2);
        sb.append("']))");

        ArrayList<String> propNames = this.getPropertyNames();
        int propCount = propNames.size();
        if (propCount > 0) {
            for (int i = 0; i < propCount; i++) {
                String name = propNames.get(i);
                Property p = getProperty(name);
                String value = p.getValue();
                String dType = p.getDataType();
                appendGroovyProperty(sb, name, value, dType);
            }
        }
        return sb.toString();
    }

    private void appendGroovyProperty(StringBuffer sb, String name, String value, String dataType) {

        String n = scrubValue(name);
        String v = scrubValue(value);

        if (n.length() > 0) {
            if (!n.equals("null")) {
                if (v.length() > 0) {
                    if (!v.equals("null")) {
                        if (isNumericDatatype(dataType)) {
                            sb.append(".property('");
                            sb.append(scrubValue(name));
                            sb.append("',");  // <-- unquoted
                            sb.append(scrubValue(value));
                            sb.append(")");   // <-- unquoted
                        }
                        else {
                            sb.append(".property('");
                            sb.append(scrubValue(name));
                            sb.append("','");
                            sb.append(scrubValue(value));
                            sb.append("')");
                        }
                    }
                }
            }
        }
    }

    private boolean isNumericDatatype(String dType) {

        if (dType == null) {
            return false;
        }
        if (dType.equalsIgnoreCase("int")) {
            return true;
        }
        if (dType.equalsIgnoreCase("float")) {
            return true;
        }
        if (dType.equalsIgnoreCase("double")) {
            return true;
        }
        return false;
    }

    private String scrubValue(String s) {

        if (s == null) {
            return "";
        }
        String s1 = s.replaceAll("'", "");
        String s2 = s1.replaceAll("#", "_");
        return s2.trim();
    }
}
