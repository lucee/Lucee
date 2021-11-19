package lucee.commons.lang.compiler;

import lucee.runtime.PageSource;

public class JavaFunction {

	public final byte[] byteCode;
	public final SourceCode sourceCode;
	private PageSource parent;
	// private String templateName;
	// private String functionName;

	public JavaFunction(PageSource parent, SourceCode sourceCode, byte[] byteCode) {
		this.parent = parent;
		this.sourceCode = sourceCode;
		this.byteCode = byteCode;
	}

	public String getName() {
		int index = sourceCode.getClassName().lastIndexOf('.');
		if (index == -1) return sourceCode.getClassName();
		return sourceCode.getClassName().substring(index + 1);
	}

	public String getPackage() {
		int index = sourceCode.getClassName().lastIndexOf('.');
		if (index == -1) return "";
		return sourceCode.getClassName().substring(0, index);
	}

	public String getClassName() {
		return sourceCode.getClassName();
	}

	public PageSource getParent() {
		return parent;
	}

	/*
	 * public String getTemplateName() { return templateName; }
	 */

	/*
	 * public void setTemplateName(String templateName) { this.templateName = templateName; }
	 */

	/*
	 * public String getFunctionName() { return functionName; }
	 */

	/*
	 * public void setFunctionName(String functionName) { this.functionName = functionName; }
	 */
}
