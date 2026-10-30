package lucee.runtime.functions.ai;

import lucee.runtime.type.query.QueryReturnType;
import java.util.HashSet;
import lucee.runtime.op.Caster;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * returns all AI connections available in the current context, the ones defined by the application (this.ai) and the ones defined
 * in the config (LDEV-6621). Secrets (secretKey, custom properties, headers) are never part of the result.
 */
public final class AIGetConnections extends BIF {

	private static final long serialVersionUID = 4087731722431622517L;

	private static final String SOURCE_APPLICATION = "application";
	private static final String SOURCE_SERVER = "server";

	private static final Key[] COLUMNS = new Key[] { KeyConstants._name, KeyConstants._label, KeyConstants._default, KeyConstants._model, KeyConstants._class,
			KeyConstants._bundleName, KeyConstants._bundleVersion, KeyConstants._temperature, KeyConstants._connectionTimeout, KeyConstants._socketTimeout,
			KeyConstants._conversationSizeLimit, KeyConstants._source };

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length > 2) throw new FunctionException(pc, "AIGetConnections", 0, 2, args.length);
		if (args.length == 0) return call(pc);
		return call(pc, Caster.toString(args[0]), args.length > 1 ? Caster.toString(args[1]) : null);
	}

	public static Object call(PageContext pc, String returntype, String columnKey) throws PageException {
		return QueryReturnType.convert(call(pc), returntype, columnKey, "AIGetConnections");
	}

	public static Query call(PageContext pc) throws PageException {
		Query qry = new QueryImpl(COLUMNS, 0, "connections");
		Set<String> done = new HashSet<>();

		// connections defined by the application, they win over the ones with the same name in the config
		ApplicationContext ac = pc.getApplicationContext();
		if (ac instanceof ApplicationContextSupport) {
			for (Entry<String, AIEngine> e: ((ApplicationContextSupport) ac).getAIEngines().entrySet()) {
				if (e.getValue() != null && done.add(e.getKey().toLowerCase())) addRow(qry, e.getKey(), e.getValue(), SOURCE_APPLICATION);
			}
		}

		// connections defined in the config
		ConfigPro config = (ConfigPro) pc.getConfig();
		AIEngine aie;
		for (String name: config.getAIEngineNames()) {
			if (!done.add(name.toLowerCase())) continue;
			aie = config.getAIEngine(name);
			if (aie != null) addRow(qry, name, aie, SOURCE_SERVER);
		}
		return qry;
	}

	private static void addRow(Query qry, String key, AIEngine aie, String source) {
		int row = qry.addRow();
		String name = aie.getName();
		qry.setAtEL(KeyConstants._name, row, StringUtil.isEmpty(name, true) ? key : name);
		qry.setAtEL(KeyConstants._label, row, aie.getLabel());
		qry.setAtEL(KeyConstants._default, row, StringUtil.emptyIfNull(aie.getDefault()));
		qry.setAtEL(KeyConstants._model, row, aie.getModel());
		ClassDefinition<? extends AIEngine> cd = aie.getClassDefinition();
		if (cd != null) {
			qry.setAtEL(KeyConstants._class, row, cd.getClassName());
			qry.setAtEL(KeyConstants._bundleName, row, cd.getName());
			qry.setAtEL(KeyConstants._bundleVersion, row, cd.getVersionAsString());
		}
		qry.setAtEL(KeyConstants._temperature, row, aie.getTemperature());
		qry.setAtEL(KeyConstants._connectionTimeout, row, aie.getConnectTimeout());
		qry.setAtEL(KeyConstants._socketTimeout, row, aie.getSocketTimeout());
		qry.setAtEL(KeyConstants._conversationSizeLimit, row, aie.getConversationSizeLimit());
		qry.setAtEL(KeyConstants._source, row, source);
	}
}
