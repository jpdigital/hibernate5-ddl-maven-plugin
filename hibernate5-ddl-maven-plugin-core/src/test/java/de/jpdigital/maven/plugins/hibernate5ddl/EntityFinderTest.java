package de.jpdigital.maven.plugins.hibernate5ddl;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityFinderTest {

    private final DefaultLog log = new DefaultLog(new ConsoleLogger());

    @Test
    public void findEntities() throws MojoFailureException {
        assertEquals("Found all entities in 'com.example' packages",
                2, EntityFinder.forPackage(null, log, "com.example", false).findEntities().size());
        assertEquals("Found entities only in 'com.example.p1' packages",
                1, EntityFinder.forPackage(null, log, "com.example.p1", false).findEntities().size());
        assertEquals("Found entities only in 'com.example.p2' packages",
                1, EntityFinder.forPackage(null, log, "com.example.p2", false).findEntities().size());
    }


}