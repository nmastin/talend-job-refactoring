package de.jlo.talend.tweak.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public class DeployDIJob extends Deployer {
	
	private File jobZipFile = null;
	
	public DeployDIJob() {
		setNexusRepository("job-releases");
	}
	
	public void setJobZipFile(String jobZipFilePath) {
		this.jobZipFile = new File(jobZipFilePath);
		String fileName = jobZipFile.getName();
		int pos1 = fileName.lastIndexOf("_");
		artifactId = fileName.substring(0, pos1);
		version = extractVersion(fileName, "zip");
	}

	private String buildDIJobPom() {
		StringBuilder pom = new StringBuilder();
		pom.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
		pom.append("<modelVersion>4.0.0</modelVersion>\n");
		pom.append("<groupId>");
		pom.append(groupId);
		pom.append("</groupId>\n");
		pom.append("<artifactId>");
		pom.append(artifactId);
		pom.append("</artifactId>\n");
		pom.append("<version>");
		pom.append(version);
		pom.append("</version>\n");
		pom.append("<type>zip</type>\n");
		pom.append("</project>");
		return pom.toString();
	}
	
	public void deployToNexus() throws Exception {
		if (httpClient == null) {
			throw new IllegalStateException("Http client not connected");
		}
		HttpPost post = new HttpPost(nexusUrl + restPath);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addTextBody("r", nexusRepository, ContentType.DEFAULT_TEXT);
		builder.addTextBody("hasPom", "true", ContentType.DEFAULT_TEXT);
		builder.addTextBody("e", "zip", ContentType.DEFAULT_TEXT);
		builder.addBinaryBody("file", buildDIJobPom().getBytes("UTF-8"), ContentType.DEFAULT_BINARY, "pom.xml");
		InputStream inputStream = new FileInputStream(jobZipFile);
		builder.addBinaryBody("file", inputStream, ContentType.DEFAULT_BINARY, jobZipFile.getName());
		HttpEntity entity = builder.build();
		post.setEntity(entity);
		String response = httpClient.execute(post, false);
		System.out.println(response);
	}

}
