package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

public interface AIResponseListener {

	public void listen(String part) throws PageException;

}