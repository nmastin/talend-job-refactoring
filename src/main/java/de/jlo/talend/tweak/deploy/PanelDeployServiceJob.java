package de.jlo.talend.tweak.deploy;

import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import de.jlo.talend.tweak.TalendTweakTool;

public class PanelDeployServiceJob extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(PanelDeployServiceJob.class);
	private TalendTweakTool mainFrame = null;
	private DeployServiceJob deployer = new DeployServiceJob();
	private JTextField tfFile = null;
	private JTextField tfNexusURL = null;
	private JTextField tfNexusRepo = null;
	private JTextField tfNexusGroupId = null;
	private JTextField tfNexusUser = null;
	private JPasswordField tfNexusPassword = null;
	private JButton btnFileChooser = null;
	private JButton btnDeploy = null;

	public PanelDeployServiceJob(TalendTweakTool mainFrame) throws Exception {
		this.mainFrame = mainFrame;
		initialize();
	}
	
	private void initialize() throws Exception {
		setLayout(new GridBagLayout());

		{
			JLabel label = new JLabel("Nexus URL: ");
			label.setHorizontalAlignment(JLabel.LEFT);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			this.add(label, gbc);
		}
		{
			tfNexusURL = new JTextField();
			tfNexusURL.setToolTipText("Path to the Service OSGi bundle");
			tfNexusURL.setEditable(false);
			tfNexusURL.setText(deployer.getNexusUrl());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 0;
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(tfNexusURL, gbc);
		}
		{
			JLabel label = new JLabel("Nexus Job Repo and groupId: ");
			label.setHorizontalAlignment(JLabel.LEFT);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 1;
			gbc.gridx = 0;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			this.add(label, gbc);
		}
		{
			tfNexusRepo = new JTextField();
			tfNexusRepo.setToolTipText("Nexus repository");
			tfNexusRepo.setEditable(false);
			tfNexusRepo.setText(deployer.getNexusRepository());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 1;
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(tfNexusRepo, gbc);
		}
		{
			tfNexusGroupId = new JTextField();
			tfNexusGroupId.setToolTipText("Group-ID");
			tfNexusGroupId.setEditable(false);
			tfNexusGroupId.setText(deployer.getGroupId());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 1;
			gbc.gridx = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(tfNexusGroupId, gbc);
		}
		{
			JLabel label = new JLabel("Nexus User and password: ");
			label.setHorizontalAlignment(JLabel.LEFT);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 2;
			gbc.gridx = 0;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			this.add(label, gbc);
		}
		{
			tfNexusUser = new JTextField();
			tfNexusUser.setToolTipText("User");
			tfNexusUser.setEditable(false);
			tfNexusUser.setText(deployer.getNexusUser());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 2;
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(tfNexusUser, gbc);
		}
		{
			tfNexusPassword = new JPasswordField();
			tfNexusPassword.setToolTipText("Password");
			tfNexusPassword.setEditable(false);
			tfNexusPassword.setText(deployer.getNexusPasswd());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 2;
			gbc.gridx = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(tfNexusPassword, gbc);
		}
		{
			JLabel label = new JLabel("Choose exported Service OSGi bundle: ");
			label.setHorizontalAlignment(JLabel.LEFT);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 3;
			gbc.gridx = 0;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			this.add(label, gbc);
		}
		{
			tfFile = new JTextField();
			tfFile.setToolTipText("Path to the Talend Service OSGi bundle jar file");
			tfFile.setEditable(false);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 3;
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(tfFile, gbc);
		}
		{
			btnFileChooser = new JButton("...");
			btnFileChooser.setToolTipText("Choose exported OSGI bundle");
			btnFileChooser.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					selectFile();
				}
			});
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 3;
			gbc.gridx = 3;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(btnFileChooser, gbc);
		}
		{
			btnDeploy = new JButton("Deploy Service");
			btnDeploy.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							btnDeploy.setEnabled(false);
							PanelDeployServiceJob.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						}
					});
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							deploy();
						}
					});
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							PanelDeployServiceJob.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							btnDeploy.setEnabled(true);
						}
					});
				}
			});
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 4;
			gbc.gridx = 1;
			gbc.insets = new Insets(5, 5, 5, 5);
			this.add(btnDeploy, gbc);
		}
	}

	private void selectFile() {
		System.setProperty("apple.awt.fileDialogForDirectories", "false");
		FileDialog fd = new FileDialog(mainFrame, "Choose exported Talend Service OSGi bundle (jar file)", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
			
			@Override
			public boolean accept(File file, String name) {
				return name.endsWith(".jar");
			}
			
		});
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        if (file != null) {
        	tfFile.setText(new File(dir, file).getAbsolutePath());
        }
	}
	
	private void deploy() {
		deployer = new DeployServiceJob();
		deployer.setNexusUrl(tfNexusURL.getText());
		deployer.setNexusRepository(tfNexusRepo.getText());
		deployer.setGroupId(tfNexusGroupId.getText());
		deployer.setNexusUser(tfNexusUser.getText());
		deployer.setNexusPasswd(new String(tfNexusPassword.getPassword()));
		deployer.setJobJarFile(tfFile.getText());
		try {
			LOG.info("Deploying bundle job: " + tfFile.getText() + ". Artifact-ID: " + deployer.getArtifactId() + " Version: " + deployer.getVersion());
			deployer.connect();
			deployer.deployBundleToNexus();
			LOG.info("Deploying feature job: " + tfFile.getText() + ". Artifact-ID: " + deployer.getArtifactId() + " Version: " + deployer.getVersion());
			deployer.deployFeatureToNexus();
			LOG.info("Service Job: " + tfFile.getText() + " successfully deployed: Artifact-ID: " + deployer.getArtifactId() + " Version: " + deployer.getVersion());
			deployer.close();
		} catch (Exception e) {
			LOG.error("Deploy Service Job: " + tfFile.getText() + " failed: " + e.getMessage(), e);
		}
	}

}
