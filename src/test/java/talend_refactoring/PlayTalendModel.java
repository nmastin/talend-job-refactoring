package talend_refactoring;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.jlo.talend.model.TalendModel;
import de.jlo.talend.model.Talendjob;
import de.jlo.talend.model.TaskFixTRunJob;

public class PlayTalendModel {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger LOG = Logger.getRootLogger();
		LOG.setLevel(Level.INFO);
		TalendModel model = new TalendModel();
		try {
			model.readProject("/Data/projects/gvl/talend/workspace_631_tos/BEAT17");
			Talendjob job = model.getLatestJob("navi_calculate_usage_score_one_product");
			System.out.println(job);
			TaskFixTRunJob rt = new TaskFixTRunJob(model);
			rt.setOutputDir("/Data/projects/gvl/talend/workspace_631_fixedjobs/BEAT17");
			rt.execute();
			System.out.println(rt.getSummary());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
