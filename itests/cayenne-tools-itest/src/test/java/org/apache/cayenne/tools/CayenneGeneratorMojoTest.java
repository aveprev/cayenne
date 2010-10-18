package org.apache.cayenne.tools;

import org.apache.cayenne.reflect.FieldAccessor;

public class CayenneGeneratorMojoTest extends ClassGenerationPluginIntegrationCase {

    private CayenneGeneratorMojo mojo;

    @Override
    public void setUp() {
        super.setUp();

        mojo = new CayenneGeneratorMojo();
        setProperty("outputPattern", "*.java");
    }

    @Override
    protected void execute() throws Exception {
        mojo.execute();
    }

    @Override
    protected void setProperty(String name, Object value) {
        new FieldAccessor(mojo.getClass(), name, value.getClass()).setValue(mojo, value);
    }
}
