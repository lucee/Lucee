package lucee.runtime.template;

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
	
	final public int getDialect() {
		int _d = _getDialect();
		switch (_d) {
			case CFMLEngine.DIALECT_BOTH:
			case CFMLEngine.DIALECT_CFML:
			case CFMLEngine.DIALECT_LUCEE:
				return _d;
			
			default:
				return CFMLEngine.DIALECT_LUCEE;
		}
	}
	
	int _getDialect() {
		return config.allowLuceeDialect() ? CFMLEngine.DIALECT_LUCEE : CFMLEngine.DIALECT_CFML;
	}
	
	abstract public TemplatePageFactory getPageFactory();
	
}
