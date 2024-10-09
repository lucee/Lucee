package lucee.runtime.util.threading;

import lucee.runtime.exp.PageException;

public interface CloserJob {

	public String getLablel();

	public void execute() throws PageException;

}
