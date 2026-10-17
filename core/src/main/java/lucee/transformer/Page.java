package lucee.transformer;

import lucee.runtime.config.Config;
import lucee.transformer.util.SourceCode;

public interface Page extends Root {

	public boolean isPage();

	public boolean isInterface();

	public boolean isComponent();

	public boolean isAST();

	public SourceCode getSourceCode();

	public Config getConfig();

	public long getLastModifed();

	public boolean getOutput();

	public boolean returnValue();

	public boolean getSupressWSbeforeArg();
}
