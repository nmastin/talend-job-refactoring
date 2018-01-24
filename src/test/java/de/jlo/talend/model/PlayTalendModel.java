package de.jlo.talend.model;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class PlayTalendModel {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger LOG = Logger.getRootLogger();
		LOG.setLevel(Level.INFO);
		TalendModel model = new TalendModel();
		try {
			model.readProject("/Data/projects/gvl/git/talend_631_project_beat17/BEAT17");
			Talendjob job = model.getLatestJob("navi_calculate_usage_score_one_product");
			System.out.println(job);
//			Talendjob job2 = model.readFromProperties(new File("/Data/projects/gvl/git/talend_631_project_beat17/BEAT17/process/beat17/core/Agreement/CORE_LOAD_STAGING_GDI_ADMINISTRATION_AGREEMENT_one_block_1.2.properties"));
//			System.out.println(job2.getId());
			TaskFixTRunJob rt = new TaskFixTRunJob(model);
			rt.setOutputDir("/Data/projects/gvl/git/talend_631_project_beat17/BEAT17");
			rt.execute();
			System.out.println(rt.getSummary());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
