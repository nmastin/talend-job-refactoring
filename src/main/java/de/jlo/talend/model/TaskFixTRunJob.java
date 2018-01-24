package de.jlo.talend.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class TaskFixTRunJob {

	private static Logger LOG = Logger.getLogger(TaskFixTRunJob.class);
	private String projectRootPath = null;
	private TalendModel model = null;
	private int countAffectedJobs = 0;
	private int countComponents = 0;
	private int countAffectedComponents = 0;
	private int countFixedComponents = 0;
	private int countMissingJobs = 0;
	private List<Talendjob> listFixedTalendJobs = new ArrayList<Talendjob>();
	private String outputDir = null;
	
	public static void main(String[] args) {
		TaskFixTRunJob task = new TaskFixTRunJob();
		if (args != null && args.length > 0) {
			task.projectRootPath = args[0];
			if (args.length > 1) {
				task.outputDir = args[1];
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
		List<Talendjob> list = model.getAllJobs();
		for (Talendjob job : list) {
			if (checkAndRepair(job)) {
				listFixedTalendJobs.add(job);
				countAffectedJobs++;
			}
		}
	}
	
	private void writeFixedJobs(Talendjob job) throws Exception {
		model.writeItemFile(job, outputDir);
	}
	
	private boolean checkAndRepair(Talendjob job) throws Exception {
		LOG.debug("Check job: " + job);
		job.setItemDoc(model.readItem(job));
		List<Element> listTRunJobs = model.getComponents(job.getItemDoc(), "tRunJob");
		boolean jobFixed = false;
		for (Element el : listTRunJobs) {
			countComponents++;
			if (checkAndRepairOneTRunJob(job, el)) {
				writeFixedJobs(job);
				jobFixed = true;
			}
		}
		return jobFixed;
	}
	
	private boolean checkAndRepairOneTRunJob(Talendjob job, Element tRunJob) throws Exception {
		List<Element> params = tRunJob.elements();
		String referencedJobName = null;
		String referencedJobVersion = null;
		String referencedJobId = null;
		String compUniqeName = null;
		Element processId = null;
		for (Element param : params) {
			countComponents++;
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
				countMissingJobs++;
			} else {
				LOG.info("Job reference fixed in job: " + job + " component: " + compUniqeName + " referenced job: " + referencedJobName + ":" + referencedJobVersion);
				processId.addAttribute("value", job.getId());
				countFixedComponents++;
				return true;
			}
		}
		return false;
	}

	public int getCountAffectedJobs() {
		return countAffectedJobs;
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
		sb.append("* Count affected jobs: " + countAffectedJobs + "\n");
		sb.append("* Count affected components: " + countAffectedComponents + "\n");
		sb.append("* Count components with missing references: " + countMissingJobs + "\n");
		sb.append("## List jobs changed:\n");
		for (Talendjob job : listFixedTalendJobs) {
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
	
}
