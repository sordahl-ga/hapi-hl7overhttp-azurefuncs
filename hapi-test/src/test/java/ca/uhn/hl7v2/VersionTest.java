package ca.uhn.hl7v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {

	@Test public void testGreaterThan() {
		assertTrue(Version.V24.isGreaterThan(Version.V23));
		assertTrue(Version.V231.isGreaterThan(Version.V23));
		assertFalse(Version.V23.isGreaterThan(Version.V23));
		assertFalse(Version.V22.isGreaterThan(Version.V23));
	}
	
	@Test public void testLatestVersion() {
		assertEquals(Version.V281, Version.latestVersion());
	}
	
	@Test public void testVersionOf() {
		for (Version version : Version.values()) {
			assertEquals(version, Version.versionOf(version.getVersion()));
		}
	}
	
	@Test public void testSupportsVersion() {
		for (Version version : Version.values()) {
			assertTrue(Version.supportsVersion(version.getVersion()));
		}
		assertFalse(Version.supportsVersion("3.0"));
	}

    @Test public void testExcept() {
        assertEquals(0, Version.except(Version.values()).length);
        Version[] excepts = Version.except(Version.V231, Version.V25);
        assertEquals(2, Version.values().length - excepts.length);
    }
	
	@Test public void testAvailableVersions() {
		assertTrue(Version.availableVersions().size() > 0);
	}
	
	@Test public void testLowestAvailableVersion() {
		assertNotNull(Version.lowestAvailableVersion());
	}

    @Test public void testModelPackageName() {
        assertEquals("ca.uhn.hl7v2.model.v24.", Version.V24.modelPackageName());
    }
}
