package de.jlo.talend.deploy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.jlo.talend.tweak.deploy.DeployServiceJob;

public class TestDeployServiceJob {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSetJobZipFile() {
		DeployServiceJob d = new DeployServiceJob();
		String zipFilePath = "/path/to/my_artifact-1.23.jar";
		d.setJobJarFile(zipFilePath);
		String expectedArtifactid = "my_artifact";
		String expectedVersion = "1.23.0";
		String actualArtifactId = d.getArtifactId();
		String actualVersion = d.getVersion();
		Assert.assertEquals("ArtifactId does not match", expectedArtifactid, actualArtifactId);
		Assert.assertEquals("Version does not match", expectedVersion, actualVersion);
	}
	
//	@Test
//	public void testDeloy() throws Exception {
//		DeployServiceJob d = new DeployServiceJob();
//		String zipFilePath = "/Data/exported_jobs/core_generate_calendar_0.1.zip";
//		d.setJobJarFile(zipFilePath);
//		String expectedArtifactid = "core_generate_calendar";
//		String expectedVersion = "0.1.0";
//		String actualArtifactId = d.getArtifactId();
//		String actualVersion = d.getVersion();
//		Assert.assertEquals("ArtifactId does not match", expectedArtifactid, actualArtifactId);
//		Assert.assertEquals("Version does not match", expectedVersion, actualVersion);
//		d.connect();
//		d.deployToNexus();
//	}

}
