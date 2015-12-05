package lucee.runtime.template;

import java.util.List;
import java.util.Arrays;


public abstract class TemplateEngine {
	
	private List<String> extensions;
	
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
	
	public boolean handlesExtension(String extension) {
		return this.extensions.contains(extension);
	}
	
	
	abstract public TemplatePageFactory getPageFactory();
	
}
