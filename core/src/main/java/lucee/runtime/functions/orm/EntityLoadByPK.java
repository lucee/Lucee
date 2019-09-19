package lucee.runtime.functions.orm;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;

public class EntityLoadByPK {
	public static Object call(PageContext pc, String name, Object oID) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		String id;
		if (Decision.isBinary(oID)) id = Caster.toBase64(oID);
		else id = Caster.toString(oID);
		return session.load(pc, name, id);
		// FUTURE call instead load(..,..,OBJECT);
	}
}