package de.jlo.talend.model;

import org.dom4j.Document;

public class Talendjob implements Comparable<Talendjob> {
	
	private String id = null;
	private String projectName = null;
	private String jobName = null;
	private String version = null;
	private int majorVersion = 0;
	private int minorVersion = 0;
	private String pathWithoutExtension = null;
	private Document itemDoc = null;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
		int pos = version.indexOf('.');
		if (pos == -1) {
			throw new IllegalArgumentException("Talendjob has an invalid version: " + version);
		} else {
			majorVersion = Integer.parseInt(version.substring(0, pos));
			minorVersion = Integer.parseInt(version.substring(pos + 1));
		}
	}
	
	public String getPathWithoutExtension() {
		return pathWithoutExtension;
	}
	
	public void setPath(String path) {
		if (path.endsWith(".properties")) {
			this.pathWithoutExtension = path.replace(".properties", "");
		} else if (path.endsWith(".item")) {
 			this.pathWithoutExtension = path.replace(".item", "");
		} else if (path.endsWith(".screenshot")) {
 			this.pathWithoutExtension = path.replace(".screenshot", "");
		} else {
			this.pathWithoutExtension = path;
		}
	}

	@Override
	public int hashCode() {
		return (jobName + ":" + version).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Talendjob) {
			return jobName.equals(((Talendjob) o).jobName) && version.equals(((Talendjob) o).version);
		}
		return false;
	}

	@Override
	public int compareTo(Talendjob job) {
		int pos = job.getVersion().indexOf('.');
		if (pos == -1) {
			throw new IllegalArgumentException("Talendjob: " + job.jobName + " has an invalid version: " + job.version);
		} else {
			int major = Integer.parseInt(job.getVersion().substring(0, pos));
			int minor = Integer.parseInt(job.getVersion().substring(pos + 1));
			if (majorVersion > major) {
				return -1;
			} else if (majorVersion < major) {
				return 1;
			} else {
				if (minorVersion > minor) {
					return -1;
				} else if (minorVersion < minor) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return jobName + ":" + version;
	}
	public Document getItemDoc() {
		return itemDoc;
	}
	public void setItemDoc(Document itemDoc) {
		this.itemDoc = itemDoc;
	}
	
}
