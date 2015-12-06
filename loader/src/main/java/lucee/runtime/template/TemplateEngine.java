package lucee.runtime.template;

import java.util.Arrays;
import java.util.List;

import lucee.runtime.config.Config;

public abstract class TemplateEngine {
	
	private String label;
	private List<String> extensions;
	private Config config;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public TemplateEngine setExtensions(String extensionList) {
		return this.setExtensions(extensionList.split(",\\s*"));
	}
	
	public TemplateEngine setExtensions(String[] extensions) {
		return this.setExtensions(Arrays.asList(extensions));
	}
	
	public TemplateEngine setExtensions(List<String> extensions) {
		this.extensions = extensions;
		return this;
	}
	
	public List<String> getExtensions() {
		return this.extensions;
	}
	
	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public boolean handlesExtension(String extension) {
		return this.extensions.contains(extension);
	}
	
	
	abstract public TemplatePageFactory getPageFactory();
	
}
