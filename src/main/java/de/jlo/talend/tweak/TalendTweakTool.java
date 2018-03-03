package de.jlo.talend.tweak;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.jlo.talend.tweak.log.LogPanel;
import de.jlo.talend.tweak.model.TalendModel;

public class TalendTweakTool extends JFrame {

	private static final Logger LOG = Logger.getLogger(TalendTweakTool.class);
	private static final long serialVersionUID = 1L;
	private TalendModel model = null;
	private JTabbedPane tabbedPane = null;
	private JTextField tfProjectPath = null;
	private JButton btnFileChooser = null;
	private JLabel lbNumberJobs = null;
	private PanelTaskFixTRunJob pnTaskFixTRunJob = null;
	private PanelTaskSearchByComponentAttributes pnTaskSearchByComponentAttributes = null;

	public static void main(String[] args) {
		TalendTweakTool tool = new TalendTweakTool();
		tool.initialize();
	}
	
	private void initialize() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		setTitle("Talend Tweak Tool");
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Model config", getConfigPane());
		tabbedPane.add("Log", LogPanel.getInstance());
		tabbedPane.add("Fix tRunJob", getPanelTaskFixTRunJob());
		tabbedPane.add("Search Component Attributes", getPanelTaskSearchByComponentAttributes());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setVisible(true);
		setPreferredSize(new Dimension(600,400));
		pack();
		WindowHelper.locateWindowAtMiddleOfDefaultScreen(this);
	}
	
	private JPanel getConfigPane() {
		JPanel configPane = new JPanel();
		configPane.setLayout(new GridBagLayout());
		{
			JLabel label = new JLabel("Talend project root dir");
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 3;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			configPane.add(label, gbc);
		}
		{
			tfProjectPath = new JTextField();
			tfProjectPath.setToolTipText("Path the the Talend project");
			tfProjectPath.setEditable(false);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(5, 5, 5, 5);
			configPane.add(tfProjectPath, gbc);
		}
		{
			btnFileChooser = new JButton("...");
			btnFileChooser.setToolTipText("Choose Talend project root dir");
			btnFileChooser.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					selectProjectRootDir();
				}
			});
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 2;
			gbc.gridy = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			configPane.add(btnFileChooser, gbc);
		}
		{
			JLabel label = new JLabel("Number Talend jobs: ");
			label.setHorizontalAlignment(JLabel.RIGHT);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.EAST;
			configPane.add(label, gbc);
		}
		{
			lbNumberJobs = new JLabel("0");
			lbNumberJobs.setHorizontalAlignment(JLabel.LEFT);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 2;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			configPane.add(lbNumberJobs, gbc);
		}
		return configPane;
	}
	
	private JPanel getPanelTaskFixTRunJob() {
		pnTaskFixTRunJob = new PanelTaskFixTRunJob();
		return pnTaskFixTRunJob;
	}
	
	private JPanel getPanelTaskSearchByComponentAttributes() {
		pnTaskSearchByComponentAttributes = new PanelTaskSearchByComponentAttributes();
		return pnTaskSearchByComponentAttributes;
	}

	private void selectProjectRootDir() {
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		FileDialog fd = new FileDialog(this, "Choose Talend root project dir (or file talend.project)", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory() || name.equals("talend.project");
			}
			
		});
        fd.setVisible(true);
        String file = fd.getFile();
        if (file != null) {
            File projectDir = new File(fd.getDirectory(),  file);
            if (projectDir != null) {
            	if (projectDir.isFile()) {
            		projectDir = projectDir.getParentFile();
            	}
    			tfProjectPath.setText(projectDir.getAbsolutePath());
    			initializeModel(projectDir.getAbsolutePath());
            }
        }
	}
	
	private void initializeModel(final String projectRootDir) {
		Thread loaderThread = new Thread(new Runnable() {

			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						TalendTweakTool.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					}
				});
				TalendModel _model = new TalendModel();
				try {
					_model.readProject(projectRootDir);
					setModel(_model);
				} catch (Exception e) {
					LOG.error("Load model from: " + projectRootDir + " failed: " + e.getMessage(), e);
				} finally {
					btnFileChooser.setEnabled(true);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							TalendTweakTool.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					});
				}
			}
			
		});
		loaderThread.start();
	}
	
	private void setModel(final TalendModel model) {
		this.model = model;
		lbNumberJobs.setText(String.valueOf(model.getCountJobs()));
		pnTaskFixTRunJob.setModel(model);
		pnTaskSearchByComponentAttributes.setModel(model);
	}

	public TalendModel getModel() {
		return model;
	}
	
}
