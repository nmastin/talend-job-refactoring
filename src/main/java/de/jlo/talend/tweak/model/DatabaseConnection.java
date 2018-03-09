package de.jlo.talend.tweak.model;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;

public class DatabaseConnection {
	
	private String id = null;
	private String name = null;
	private boolean contextMode = false;
	private String contextId = null;
	private String url = null;
	private String port = null;
	private String userName = null;
	private String password = null;
	private String serverName = null;
	private String database = null;
	private String schema = null;
	private Document itemDoc = null;
	
	public DatabaseConnection(Document doc) {
		this.itemDoc = doc;
    	Element root = itemDoc.getRootElement();
    	QName nameId = new QName("id", null);
		id = root.attributeValue(nameId);
		name = root.attributeValue("name");
		contextMode = "true".equals(root.attributeValue("ContextMode"));
		contextId = root.attributeValue("ContextId");
		url = root.attributeValue("URL");
		port = root.attributeValue("Port");
		userName = root.attributeValue("Username");
		password = root.attributeValue("Password");
		serverName = root.attributeValue("ServerName");
		database = root.attributeValue("SID");
		schema = root.attributeValue("UiSchema");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isContextMode() {
		return contextMode;
	}

	public String getContextId() {
		return contextId;
	}

	public String getUrl() {
		return url;
	}

	public String getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getServerName() {
		return serverName;
	}

	public String getDatabase() {
		return database;
	}

	public String getSchema() {
		return schema;
	}
	
}
