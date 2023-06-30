package lucee.commons.lang.compiler;

import lucee.runtime.exp.PageException;

public interface Compiler {
	public boolean supported();

	public byte[] compile(SourceCode sc) throws PageException, JavaCompilerException;
}
