package de.jlo.talend.tweak.model.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import de.jlo.talend.tweak.model.TalendModel;
import de.jlo.talend.tweak.model.Talendjob;
import de.jlo.talend.tweak.model.tasks.AbstractTask;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TableAndProcedureNameFinder;

public class JobDatabaseTableCollector extends AbstractTask {
	
	private static final Logger LOG = Logger.getLogger(JobDatabaseTableCollector.class);
	private Map<String, List<String>> inputTables = new HashMap<>();
	private Map<String, List<String>> functions = new HashMap<>();
	private Map<String, List<String>> outputTables = new HashMap<>();

	public JobDatabaseTableCollector(TalendModel model) {
		super(model);
	}

	@Override
	public void execute() throws Exception {
		// read context for all jabs
		List<Talendjob> list = getModel().getAllJobs();
		for (Talendjob job : list) {
			try {
				findTables(job);
			} catch (Exception e) {
				LOG.error("Find tables in job: " + job.getJobName() + " failed.", e);
			}
		}
	}
	
	public void findTables(Talendjob job) throws Exception {
		job.setItemDoc(getModel().readItem(job));
		// read context variables
		Map<String, String> context = job.getContext();
		ContextVarResolver cr = new ContextVarResolver();
		for (Map.Entry<String, String> entry : context.entrySet()) {
			cr.addContextVar(entry.getKey(), entry.getValue());
		}
		List<Node> listInputComps = getModel().getComponents(job.getItemDoc(), "Output", "Input");
		for (Node component : listInputComps) {
			findTables(job, cr, (Element) component);
		}
	}
	
	private void addInputTable(Talendjob job, String tableName) {
		List<String> list = inputTables.get(job.getJobName());
		if (list == null) {
			list = new ArrayList<String>();
			inputTables.put(job.getJobName(), list);
		}
		if (list.contains(tableName) == false) {
			list.add(tableName);
		}
	}
	
	private void addOutputTable(Talendjob job, String tableName) {
		List<String> list = outputTables.get(job.getJobName());
		if (list == null) {
			list = new ArrayList<String>();
			outputTables.put(job.getJobName(), list);
		}
		if (list.contains(tableName) == false) {
			list.add(tableName);
		}
	}
	
	private void addFunction(Talendjob job, String name) {
		List<String> list = functions.get(job.getJobName());
		if (list == null) {
			list = new ArrayList<String>();
			functions.put(job.getJobName(), list);
		}
		if (list.contains(name) == false) {
			list.add(name);
		}
	}

	public void findTables(Talendjob job, ContextVarResolver cr, Element component) throws Exception {
		List<Element> params = component.elements();
		String compId = null;
		String componentName = component.attributeValue("componentName");
		if (componentName.contains("Output")) {
			String outputTableName = null;
			String outputTableSchema = null;
			for (Element param : params) {
				String pname = param.attributeValue("name");
				String field = param.attributeValue("field");
				String value = param.attributeValue("value");
				if ("UNIQUE_NAME".equals(pname)) {
					compId = value;
				}
				if ("DBTABLE".equals(field)) {
					outputTableName = value;
				}
				if ("SCHEMA_DB".equals(pname)) {
					outputTableSchema = cr.getVariableValue(value);
				}
			}
			if (outputTableName != null) {
				if (outputTableSchema != null) {
					addOutputTable(job, outputTableSchema + "." + outputTableName);
				} else {
					addOutputTable(job, outputTableName);
				}
			}
		} else {
			String query = null;
			// Input and Row components
			for (Element param : params) {
				String pname = param.attributeValue("name");
				String field = param.attributeValue("field");
				String value = param.attributeValue("value");
				if ("UNIQUE_NAME".equals(pname)) {
					compId = value; 
				}
				if ("MEMO_SQL".equals(field)) {
					query = value;
				}
			}
			if (query != null && query.trim().isEmpty() == false) {
				// now we have read a SQL query
				// replace the context variables
				try {
					cr.replaceContextVars(query);
				} catch (Exception e) {
					throw new Exception("Replace context vars failed for query in component: " + compId, e);
				}
				// replace globalMap variables
				query = SQLCodeUtil.replaceGlobalMapVars(query);
				// now convert to SQL
				String sql = SQLCodeUtil.convertJavaToSqlCode(query);
				// now parse the sql code
				try {
					Statement stmt = CCJSqlParserUtil.parse(sql);
					TableAndProcedureNameFinder finder = new TableAndProcedureNameFinder();
					finder.retrieveTablesAndFunctionSignatures(stmt);
					List<String> listTables = finder.getListTableNames();
					for (String t : listTables) {
						addInputTable(job, t);
					}
					List<String> listFunctions = finder.getListFunctionSignatures();
					for (String f : listFunctions) {
						addFunction(job, f);
					}
				} catch (Exception pe) {
					throw new Exception("Component: " + compId + " fail to parse QUERY: " + query + "\nSQL: " + sql, pe);
				}
			}
		}
	}

	public Map<String, List<String>> getInputTables() {
		return inputTables;
	}

	public Map<String, List<String>> getFunctions() {
		return functions;
	}

	public Map<String, List<String>> getOutputTables() {
		return outputTables;
	}
	
}