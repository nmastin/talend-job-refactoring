package de.jlo.talend.model;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.jlo.talend.tweak.model.TalendModel;
import de.jlo.talend.tweak.model.tasks.TaskFixTRunJob;

public class PlayTalendModel {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger LOG = Logger.getRootLogger();
		LOG.setLevel(Level.INFO);
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

}
