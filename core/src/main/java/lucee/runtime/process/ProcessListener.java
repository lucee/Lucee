package lucee.runtime.process;

import lucee.runtime.exp.PageException;

public interface ProcessListener {

	public void listen(String part) throws PageException;

}