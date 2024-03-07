package lucee.runtime.config;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;

public interface ConfigWebInner extends ConfigWebPro {
	public void updatePassword(ConfigWebImpl outer, boolean server, String passwordOld, String passwordNew) throws PageException;

	public ConfigServer getConfigServer(ConfigWebImpl outer, String password) throws ExpressionException;

	public boolean hasIndividualSecurityManager(ConfigWebImpl outer);
}
