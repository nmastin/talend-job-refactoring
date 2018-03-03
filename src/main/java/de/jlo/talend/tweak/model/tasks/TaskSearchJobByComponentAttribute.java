package de.jlo.talend.tweak.model.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import de.jlo.talend.tweak.model.TalendModel;
import de.jlo.talend.tweak.model.Talendjob;

public class TaskSearchJobByComponentAttribute {

	private static Logger LOG = Logger.getLogger(TaskSearchJobByComponentAttribute.class);
	private TalendModel model = null;
	private List<SearchResult> currentSearchResult = null;
	private boolean replaceAttributeValue = false;
	
	public TaskSearchJobByComponentAttribute(TalendModel model) {
		this.model = model;
	}
	
	public static class SearchResult {
		
		private Talendjob job;
		private String componentId;
		private String attribute;
		private String attributeValue;
		
		public SearchResult(Talendjob job, String componentId, String attribute, String attributeValue) {
			this.job = job;
			this.componentId = componentId;
			this.attribute = attribute;
			this.attributeValue = attributeValue;
		}

		public Talendjob getJob() {
			return job;
		}

		public String getComponentId() {
			return componentId;
		}

		public String getAttribute() {
			return attribute;
		}

		public String getAttributeValue() {
			return attributeValue;
		}

	}

	public List<SearchResult> search(String jobNamePattern, String componentName, String attribute, String valuePatternStr, String valueReplacement) throws Exception {
		List<Talendjob> jobs = model.getJobs(jobNamePattern);
		Pattern valuePattern = null;
		if (valuePatternStr != null && valuePatternStr.trim().isEmpty() == false) {
			valuePattern = Pattern.compile(valuePatternStr, Pattern.CASE_INSENSITIVE);	
		}
		List<SearchResult> result = new ArrayList<>();
		for (Talendjob job : jobs) {
			job.setItemDoc(model.readItem(job));
			List<SearchResult> sr = findValue(job, componentName, attribute, valuePattern, valueReplacement);
			result.addAll(sr);
			LOG.info(job.getJobName() + ": found " + sr.size() + " components");
			if (replaceAttributeValue && sr.size() > 0) {
				LOG.info(job.getJobName() + ": write changes");
				writeFixedJobs(job);
			}
		}
		currentSearchResult = result;
		return result;
	}
	
	public List<SearchResult> findValue(Talendjob job, String componentName, String attribute, Pattern valuePattern, String valueReplacement) throws Exception {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<Node> list = model.getComponents(job.getItemDoc(), componentName);
		for (Node cn : list) {
			SearchResult r = findValue(job, (Element) cn, attribute, valuePattern, valueReplacement);
			if (r != null) {
				result.add(r);
			}
		}
		return result;
	}
	
	private void writeFixedJobs(Talendjob job) throws Exception {
		model.writeItemFile(job, model.getProjectRootDir());
	}
	
	private String getComponentId(Element comp) {
		List<Element> params = comp.elements();
		String id = null;
		for (Element param : params) {
			String name = param.attributeValue("name");
			String value = param.attributeValue("value");
			if ("UNIQUE_NAME".equals(name)) {
				id = value;
				break;
			}
		}
		return id;
	}
	
	public SearchResult findValue(Talendjob job, Element comp, String attribute, Pattern valuePattern, String valueReplacement) throws Exception {
		SearchResult result = null;
		List<Element> params = comp.elements();
		for (Element param : params) {
			String componentId = getComponentId(comp);
			String name = param.attributeValue("name");
			String value = param.attributeValue("value");
			if (attribute.equalsIgnoreCase(name)) {
				if (valuePattern != null) {
					Matcher m = valuePattern.matcher(value);
					if (m.find()) {
						result = new SearchResult(job, componentId, attribute, value);
						if (replaceAttributeValue) {
							param.addAttribute("value", valueReplacement);
						}
						break;
					}
				} else {
					result = new SearchResult(job, componentId, name, value);
				}
			}
		}
		return result;
	}

	public List<SearchResult> getCurrentSearchResult() {
		return currentSearchResult;
	}
	
	public String getSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Found " + currentSearchResult.size() + " components:\n");
		sb.append("Replaced all found values with the new value=" + replaceAttributeValue + "\n");
		for (SearchResult r : currentSearchResult) {
			sb.append(r.getJob().getJobName());
			sb.append("->");
			sb.append(r.getComponentId());
			sb.append(": ");
			sb.append(r.getAttribute());
			sb.append("=");
			sb.append(r.getAttributeValue());
			sb.append("\n");
		}
		return sb.toString();
	}

	public boolean isReplaceAttributeValue() {
		return replaceAttributeValue;
	}

	public void setReplaceAttributeValue(boolean replaceAttributeValue) {
		this.replaceAttributeValue = replaceAttributeValue;
	}

}
