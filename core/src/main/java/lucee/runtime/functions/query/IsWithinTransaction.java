package lucee.runtime.functions.query;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.transformer.library.function.FunctionLibFunction;

public final class IsWithinTransaction extends BIF {

	private static final long serialVersionUID = 7490842489165167839L;

	// provided by the ORM extension (LDEV-6207), it knows how to look into the native session
	private static final String ORM_TRANSACTION_BIF = "IsWithinORMTransaction";

	public static boolean call(PageContext pc) {
		if (!pc.getDataSourceManager().isAutoCommit()) return true;
		return isWithinNativeORMTransaction(pc);
	}

	/**
	 * A native ORM transaction (for example ormGetSession().beginTransaction()) does not touch the
	 * datasource manager, so we have to ask the ORM. This never creates an ORM session.
	 */
	private static boolean isWithinNativeORMTransaction(PageContext pc) {
		try {
			ApplicationContext ac = pc.getApplicationContext();
			if (ac == null || !ac.isORMEnabled()) return false;

			// no session yet, so there can't be a transaction
			if (pc.getORMSession(false) == null) return false;

			FunctionLibFunction flf = ((ConfigPro) pc.getConfig()).getFLDs().getFunction(ORM_TRANSACTION_BIF);
			if (flf == null) {
				LogUtil.log(pc, Log.LEVEL_DEBUG, "orm", IsWithinTransaction.class.getName(), "function [" + ORM_TRANSACTION_BIF + "] not available, the installed ORM engine can't tell if a native transaction is active");
				return false;
			}
			return Caster.toBooleanValue(flf.getBIF().invoke(pc, new Object[0]), false);
		}
		catch (Exception e) {
			LogUtil.log(pc, "orm", IsWithinTransaction.class.getName(), e, Log.LEVEL_WARN);
			return false;
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "IsWithinTransaction", 0, 0, args.length);
	}

}
