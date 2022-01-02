package org.cjoakim.rdf2cosmos;

import org.cjoakim.rdf2cosmos.gremlin.Property;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


public class PropertyTest {

    @Test public void isValidImpl() {
        Property p1 = new Property(null, null);
        assertFalse(p1.isValid());

        Property p2 = new Property("cat", "Miles");
        assert (p2.isValid());

    }
}
