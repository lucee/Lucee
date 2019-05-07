package lucee.runtime.spooler;

import java.io.Serializable;

import lucee.runtime.config.Config;

public abstract class SpoolerTaskListener implements Serializable {

	public abstract void listen(Config config, Exception e, boolean before);
}
