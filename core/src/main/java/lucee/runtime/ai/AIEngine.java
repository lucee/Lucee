package lucee.runtime.ai;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface AIEngine {

	public AIEngine init(Struct properties) throws PageException;

	public Response invoke(Request req) throws PageException;
}
