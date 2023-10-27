package lucee.runtime.functions.orm;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;

public class EntityLoadByPK {

	public static Object call(PageContext pc, String name, Object oID) throws PageException {
		return call(pc, name, oID, false);
	}

	public static Object call(PageContext pc, String name, Object oID, boolean unique) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		String id;
		if (Decision.isBinary(oID)) id = Caster.toBase64(oID);
		else id = Caster.toString(oID);

		// TODO implement unique check if it even makes sense?
		/*
		 * if (unique && entity != null){ if (entity size > 1) throw new FunctionException(pc,
		 * "EntityLoadByPk", 3, "unique", "result wasn't unique"); }
		 */
		return session.load(pc, name, id);

		// FUTURE call instead load(..,..,OBJECT);
	}

}