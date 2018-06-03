package de.jlo.talend.model;

import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.jlo.talend.tweak.model.TalendModel;
import de.jlo.talend.tweak.model.Talendjob;
import de.jlo.talend.tweak.model.sql.JobDatabaseTableCollector;
import de.jlo.talend.tweak.model.tasks.TaskFixTRunJob;
import de.jlo.talend.tweak.model.tasks.TaskSearchJobByComponentAttribute;

public class PlayTalendModel {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger LOG = Logger.getRootLogger();
		LOG.setLevel(Level.INFO);
		playCollectTablesInAllJobs();
	}

	public static void playFixTrunJob() {
		TalendModel model = new TalendModel();
		try {
			model.readProject("/Users/jan/Desktop/test");
			TaskFixTRunJob rt = new TaskFixTRunJob(model);
			rt.execute();
			System.out.println(rt.getSummary());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void playSearchCompAttr() {
		TalendModel model = new TalendModel();
		try {
			model.readProject("/Data/projects/gvl/git/talend_631_project_beat17/BEAT17");
			TaskSearchJobByComponentAttribute rt = new TaskSearchJobByComponentAttribute(model);
			rt.search("core", "tpostgresqloutput", "table", null, null);
			System.out.println(rt.getSummary());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void playCollectTablesInAllJobs() {
		TalendModel model = new TalendModel();
		try {
			model.readProject("/Data/projects/gvl/git/talend_631_project_beat17/BEAT17");
			JobDatabaseTableCollector c = new JobDatabaseTableCollector(model);
			c.setPreferSQLParser(false);
			c.execute();
			Map<String, List<String>> inputTables = c.getInputTables();
			for (Map.Entry<String, List<String>> entry : inputTables.entrySet()) {
				System.out.println(entry.getKey());
				for (String t : entry.getValue()) {
					System.out.println("   " + t);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void playCollectTablesInOneJob() {
		Logger LOG = Logger.getRootLogger();
		LOG.setLevel(Level.DEBUG);
		TalendModel model = new TalendModel();
		try {
			model.readProject("/Data/projects/gvl/git/talend_631_project_beat17/BEAT17");
			JobDatabaseTableCollector c = new JobDatabaseTableCollector(model);
			c.setPreferSQLParser(false);
			Talendjob j = model.getJobByVersion("navi_riupdate_validate_one_file", null);
			c.findTables(j);
			Map<String, Map<String, List<String>>> inputTablesPerComp = c.getInputTablesPerComponent();
			for (Map.Entry<String, Map<String, List<String>>> entryJob : inputTablesPerComp.entrySet()) {
				String jobName = entryJob.getKey();
				System.out.println(jobName);
				Map<String, List<String>> compTableMap = entryJob.getValue();
				for (Map.Entry<String, List<String>> entryComp : compTableMap.entrySet()) {
					String component = entryComp.getKey();
					System.out.println("\t" + component);
					List<String> tables = entryComp.getValue();
					for (String table : tables) {
						System.out.println("\t\t" + table);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
