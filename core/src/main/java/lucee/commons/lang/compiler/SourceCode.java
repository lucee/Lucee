package lucee.commons.lang.compiler;

import java.io.IOException;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class SourceCode extends SimpleJavaFileObject {
	private final String contents;
	private final String className;
	private final String functionName;

	public SourceCode(String functionName, String className, String contents) {
		super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.contents = contents;
		this.className = className;
		this.functionName = functionName;
	}

	public String getClassName() {
		return className;
	}

	public String getFunctionName() {
		return functionName;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return contents;
	}
}
