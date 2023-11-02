package lucee.commons.lang.compiler;

import javax.tools.Diagnostic.Kind;

public class JavaCompilerException extends Exception {

	private static final long serialVersionUID = 7791408833450791923L;

	private long lineNumber;
	private long columnNumber;
	private Kind kind;

	public JavaCompilerException(String message, long lineNumber, long columnNumber, Kind kind) {
		super(message);
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.kind = kind;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	public long getColumnNumber() {
		return columnNumber;
	}

	public Kind getKind() {
		return kind;
	}
}
