package lucee.commons.lang.compiler;

import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.PageException;

public interface Compiler {
	public boolean supported();

	public byte[] compile(ConfigPro config, SourceCode sc) throws PageException, JavaCompilerException;
}
