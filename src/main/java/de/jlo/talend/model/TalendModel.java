package de.jlo.talend.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class TalendModel {
	
	private static final Logger LOG = Logger.getLogger(TalendModel.class);
	private Map<String, Talendjob> mapIdJob = new HashMap<String, Talendjob>();
	private Map<String, List<Talendjob>> mapNameJobs = new HashMap<>();
	private String projectRootDir = null;
	private OutputFormat format = OutputFormat.createPrettyPrint();
	
	/**
	 * Reads all Talend jobs and fills the job map
	 * @param rootDir points to project folder written the in capital letters 
	 * @return number jobs read
	 * @throws Exception
	 */
	public int readProject(String rootDir) throws Exception {
    	LOG.info("Start read jobs from project root: " + rootDir);
    	projectRootDir = rootDir;
		File processFolder = new File(rootDir, "process");
		readPropertiesFiles(processFolder);
    	LOG.info("Finished read " + mapIdJob.size() + " jobs from project root: " + rootDir);
		return mapIdJob.size();
	}

	private void registerJob(Talendjob job) {
		mapIdJob.put(job.getId(), job);
		List<Talendjob> list = mapNameJobs.get(job.getJobName());
		if (list == null) {
			list = new ArrayList<Talendjob>();
			mapNameJobs.put(job.getJobName(), list);
		}
		if (list.contains(job) == false) {
			list.add(job);
		}
	}
	
	public Talendjob getJobByVersion(String jobName, String version) {
		if (version.equals("Latest")) {
			return getLatestJob(jobName);
		} else {
			List<Talendjob> list = mapNameJobs.get(jobName);
			if (list != null && list.isEmpty() == false) {
				for (Talendjob job : list) {
					if (job.getVersion().equals(version)) {
						return job;
					}
				}
			}
			return null;
		}
	}
	
    public Talendjob getJobById(String id) {
    	return mapIdJob.get(id);
    }
    
    public Talendjob getLatestJob(String jobName) {
		List<Talendjob> list = mapNameJobs.get(jobName);
		if (list != null && list.isEmpty() == false) {
			Collections.sort(list);
			// after sort, the latest is the first element
			return list.get(0);
		} else {
			return null;
		}
	}
    
    public List<Talendjob> getAllJobs() {
    	List<Talendjob> list = new ArrayList<Talendjob>();
    	for (Map.Entry<String, Talendjob> entry : mapIdJob.entrySet()) {
    		list.add(entry.getValue());
    	}
    	return list;
    }
    
    public List<Element> getComponents(Talendjob job, String componentName) throws Exception {
    	Document doc = readItem(job);
    	List<Element> list = doc.selectNodes("/talendfile:ProcessType/node[@componentName='" + componentName + "']");
    	return list;
    }
	
    public List<Element> getComponents(Document doc, String componentName) throws Exception {
    	List<Element> list = doc.selectNodes("/talendfile:ProcessType/node[@componentName='" + componentName + "']");
    	return list;
    }
    
    private void readPropertiesFiles(File root) throws Exception {
        File[] list = root.listFiles();
        if (list == null) {
        	return;
        }
        for (File f : list) {
            if (f.isDirectory()) {
            	readPropertiesFiles(f);
            	LOG.debug("Read jobs in: " + f.getAbsoluteFile());
            } else if (f.getName().endsWith(".properties")) {
            	try {
					Talendjob job = readFromProperties(f);
					registerJob(job);
				} catch (Exception e) {
					LOG.error("Failed to read properties file: " + f.getAbsolutePath(), e);
					throw new Exception("Failed to read properties file: " + f.getAbsolutePath(), e);
				}
            }
        }
    }
    
    public Document readItem(Talendjob job) throws Exception {
    	return readFile(new File(job.getPathWithoutExtension() + ".item"));
    }
    
    private Talendjob readFromProperties(File propertiesFile) throws Exception {
    	Document propDoc = readFile(propertiesFile);
    	Talendjob job = new Talendjob();
    	Element propertyNode = (Element) propDoc.selectSingleNode("/xmi:XMI/TalendProperties:Property");
    	job.setId(propertyNode.attributeValue("id"));
    	job.setJobName(propertyNode.attributeValue("label"));
    	job.setPath(propertiesFile.getAbsolutePath());
    	job.setVersion(propertyNode.attributeValue("version"));
    	if (LOG.isDebugEnabled()) {
        	LOG.debug("Read Talend job properties from file: " + propertiesFile.getAbsolutePath() + ". Id=" + job.getId());
    	}
    	return job;
    }

    private Document readFile(File f) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		reader.close();
    	return DocumentHelper.parseText(sb.toString());
    }

	public String getProjectRootDir() {
		return projectRootDir;
	}
	
	private String getRelativePath(Talendjob job) {
		String path = job.getPathWithoutExtension();
		path = path.replace(projectRootDir, "");
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}
	
	public String writeItemFile(Talendjob job, String targetRootDir) throws Exception {
		Document itemDoc = job.getItemDoc();
		if (itemDoc == null) {
			throw new Exception("Talend job: " + job + " does not carry a read or changed document.");
		}
		String targetFilePath = null;
		if (targetRootDir == null) {
			targetFilePath = job.getPathWithoutExtension() + ".item";
		} else {
			String relPath = getRelativePath(job);
			if (targetRootDir.endsWith("/") == false) {
				targetRootDir = targetRootDir + "/";
			}
			targetFilePath = targetRootDir + relPath + ".item";
		}
		File targetFile = new File(targetFilePath);
		File targetDir = targetFile.getParentFile();
		targetDir.mkdirs();
		if (targetDir.exists() == false) {
			throw new Exception("Cannot create or use target dir: " + targetDir.getAbsolutePath());
		}
		XMLWriter writer = new XMLWriter(new FileOutputStream(targetFilePath), format);
        writer.write( itemDoc );
        writer.close();
		return targetFilePath;
	}
    
}
