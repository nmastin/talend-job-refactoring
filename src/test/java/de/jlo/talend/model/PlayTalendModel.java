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
			TaskFixTRunJob rt = new TaskFixTRunJob(model);
			rt.execute();
			System.out.println(rt.getSummary());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
