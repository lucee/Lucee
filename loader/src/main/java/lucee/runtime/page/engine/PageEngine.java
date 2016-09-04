package lucee.runtime.page.engine;

import java.util.Arrays;
import java.util.List;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;

/**
 * Template engines are the interface used to convert source files
 * into Page objects.
 * 
 * @author dajester2013
 *
 */
public abstract class PageEngine {
	
	private String label;
	private List<String> extensions;
	private Config config;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public PageEngine setExtensions(String extensionList) {
		return this.setExtensions(extensionList.split(",\\s*"));
	}
	
	public PageEngine setExtensions(String[] extensions) {
		return this.setExtensions(Arrays.asList(extensions));
	}
	
	public PageEngine setExtensions(List<String> extensions) {
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
	
	abstract public int getDialect();
	
	abstract public PageFactory getPageFactory();
	
}
