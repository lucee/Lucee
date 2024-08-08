package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class AIUtil {

	public static Exception toException(AIEngine engine, String msg, String type, String code) {
		String appendix = "";
		if ("model_not_found".equals(code) || msg.indexOf("models") != -1) {
			try {
				appendix = " Available model names are [" + AIUtil.getModelNamesAsStringList(engine) + "]";
			}
			catch (PageException e) {
			}
		}

		PageException ae = new ApplicationException(msg + appendix, "type:" + type + ";code:" + code);
		ae.setErrorCode(code);
		return ae;
	}

	public static List<String> getModelNames(AIEngine aie) throws PageException {
		List<AIModel> models = aie.getModels();
		List<String> names = new ArrayList<>();
		for (AIModel m: models) {
			names.add(m.getName());
		}
		Collections.sort(names);
		return names;
	}

	public static String getModelNamesAsStringList(AIEngine aie) throws PageException {
		StringBuilder sb = new StringBuilder();
		for (String name: getModelNames(aie)) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(name);
		}
		return sb.toString();
	}

	public static Struct getMetaData(AIEngine aie) throws PageException {

		Struct meta = new StructImpl();

		meta.set(KeyConstants._label, aie.getLabel());
		AIEngineFactory factory = aie.getFactory();
		if (factory != null) meta.set(KeyConstants._name, factory.getName());

		// models
		List<AIModel> models = aie.getModels();
		Query qry = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._label, KeyConstants._description, KeyConstants._custom }, models.size(), "models");
		int row = 0;
		for (AIModel m: models) {
			row++;
			qry.setAt(KeyConstants._name, row, m.getName());
			qry.setAt(KeyConstants._label, row, m.getLabel());
			qry.setAt(KeyConstants._description, row, m.getDescription());
			qry.setAt(KeyConstants._custom, row, m.asStruct());
		}
		meta.set(KeyConstants._models, qry);

		return meta;
	}
}
