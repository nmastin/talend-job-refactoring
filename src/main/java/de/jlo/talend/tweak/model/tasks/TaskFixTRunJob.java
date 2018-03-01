package de.jlo.talend.tweak.model.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import de.jlo.talend.tweak.model.TalendModel;
import de.jlo.talend.tweak.model.Talendjob;

public class TaskFixTRunJob {

	private static Logger LOG = Logger.getLogger(TaskFixTRunJob.class);
	private String projectRootPath = null;
	private TalendModel model = null;
	private int countRepairedJobs = 0;
	private int countComponents = 0;
	private int countAffectedComponents = 0;
	private int countFixedComponents = 0;
	private int countMissingJobs = 0;
	private List<Talendjob> listFixedTalendJobs = new ArrayList<Talendjob>();
	private List<Talendjob> listTalendJobsRefMissingJobs = new ArrayList<Talendjob>();
	private String outputDir = null;
	private boolean simulate = false; 
	
	public static void main(String[] args) {
		TaskFixTRunJob task = new TaskFixTRunJob();
		if (args != null && args.length > 0) {
			task.projectRootPath = args[0];
			if (args.length > 1) {
				task.outputDir = args[1];
			} else {
				task.outputDir = task.projectRootPath;
			}
			if (task.projectRootPath == null || task.projectRootPath.trim().isEmpty()) {
				System.err.println("projectRoorPath cannot be null or empty");
				System.exit(-1);
			} else {
				try {
					task.initialize();
					task.execute();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		} else {
			System.err.println("projectRootPath not given");
			System.exit(-1);
		}
	}
	
	public TaskFixTRunJob() {}
	
	public TaskFixTRunJob(TalendModel model) {
		this.model = model;
		projectRootPath = model.getProjectRootDir();
	}

	public void initialize() throws Exception {
		LOG.debug("Initialize model...");
		model = new TalendModel();
		model.readProject(projectRootPath);
	}
	
	public void execute() throws Exception {
		if (outputDir == null || outputDir.trim().isEmpty()) {
			outputDir = projectRootPath;
		}
		List<Talendjob> list = model.getAllJobs();
		for (Talendjob job : list) {
			if (checkAndRepair(job)) {
				listFixedTalendJobs.add(job);
				countRepairedJobs++;
			}
		}
		LOG.info(getSummary());
	}
	
	private void writeFixedJobs(Talendjob job) throws Exception {
		model.writeItemFile(job, outputDir);
	}
	
	private boolean checkAndRepair(Talendjob job) throws Exception {
		job.setItemDoc(model.readItem(job));
		List<Element> listTRunJobs = model.getComponents(job.getItemDoc(), "tRunJob");
		String message = "Check job: " + job;
		if (listTRunJobs != null && listTRunJobs.isEmpty() == false) {
			message = message + ": " + listTRunJobs.size() + " tRunJob components";
		}
		LOG.info(message);
		boolean jobFixed = false;
		for (Element el : listTRunJobs) {
			countComponents++;
			if (checkAndRepairOneTRunJob(job, el)) {
				if (simulate == false) {
					writeFixedJobs(job);
				}
				jobFixed = true;
			}
		}
		return jobFixed;
	}
	
	private boolean checkAndRepairOneTRunJob(Talendjob job, Element tRunJob) throws Exception {
    	@SuppressWarnings("unchecked")
		List<Element> params = tRunJob.elements();
		String referencedJobName = null;
		String referencedJobVersion = null;
		String referencedJobId = null;
		String compUniqeName = null;
		Element processId = null;
		for (Element param : params) {
			String name = param.attributeValue("name");
			String value = param.attributeValue("value");
			if (name.equals("PROCESS")) {
				referencedJobName = value;
			} else if (name.equals("PROCESS:PROCESS_TYPE_PROCESS")) {
				referencedJobId = value;
				processId = param;
			} else if (name.equals("PROCESS:PROCESS_TYPE_VERSION")) {
				referencedJobVersion = value;
			} else if (name.equals("UNIQUE_NAME")) {
				compUniqeName = value;
			}
		}
		LOG.debug("Check tRunJob component: " + compUniqeName + " referencing job: " + referencedJobName + ":" + referencedJobVersion);
		if (referencedJobId == null || referencedJobId.trim().isEmpty()) {
			countAffectedComponents++;
			Talendjob referencedJob = model.getJobByVersion(referencedJobName, referencedJobVersion);
			if (referencedJob == null) {
				LOG.error("Missing referenced job in job: " + job + " component: " + compUniqeName + " referenced job: " + referencedJobName + ":" + referencedJobVersion);
				if (listTalendJobsRefMissingJobs.contains(job) == false) {
					listTalendJobsRefMissingJobs.add(job);
				}
				countMissingJobs++;
			} else {
				LOG.info("Fix reference in job: " + job + " component: " + compUniqeName + " referenced job: " + referencedJobName + ":" + referencedJobVersion + " current job version: " + referencedJob.getVersion() + " id: " + referencedJob.getId());
				processId.addAttribute("value", referencedJob.getId());
				countFixedComponents++;
				return true;
			}
		}
		return false;
	}

	public int getCountRepairedJobs() {
		return countRepairedJobs;
	}

	public int getCountComponents() {
		return countComponents;
	}

	public int getCountAffectedComponents() {
		return countAffectedComponents;
	}

	public int getCountFixedComponents() {
		return countFixedComponents;
	}

	public int getCountMissingJobs() {
		return countMissingJobs;
	}
	
	public String getSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Checked project: " + projectRootPath + "\n");
		sb.append("* Count repaired jobs: " + countRepairedJobs + "\n");
		sb.append("* Count affected components: " + countAffectedComponents + "\n");
		sb.append("* Count components with missing references: " + countMissingJobs + "\n");
		sb.append("## List jobs sucessfully changed: ");
		if (simulate == false) {
			sb.append("written to output folder: " + getOutputDir() + "\n");
		} else {
			sb.append("\n");
		}
		Collections.sort(listFixedTalendJobs);
		for (Talendjob job : listFixedTalendJobs) {
			sb.append(job);
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("## List jobs with missing referenced jobs: \n");
		Collections.sort(listTalendJobsRefMissingJobs);
		for (Talendjob job : listTalendJobsRefMissingJobs) {
			sb.append(job);
			sb.append("\n");
		}
		return sb.toString();
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public boolean isSimulate() {
		return simulate;
	}

	public void setSimulate(boolean simulate) {
		this.simulate = simulate;
	}
	
}
