package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lucee.commons.io.res.ContentType;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class AIUtil {

	private static final Key CREATED_AT = KeyImpl.init("createdAt");
	private static final Key STATUS_DETAILS = KeyImpl.init("statusDetails");

	public static PageException toException(AIEngine engine, String msg, String type, String code) {
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
		{
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
		}

		// files
		if (aie instanceof AIEngineFile) {
			AIEngineFile aief = (AIEngineFile) aie;
			List<AIFile> files = aief.listFiles();

			// String status, String statusDetails
			Query qry = new QueryImpl(new Key[] { KeyConstants._object, KeyConstants._id, KeyConstants._purpose, KeyConstants._filename, KeyConstants._bytes, CREATED_AT,
					KeyConstants._status, STATUS_DETAILS }, files.size(), "files");
			int row = 0;
			for (AIFile f: files) {
				row++;
				qry.setAt(KeyConstants._object, row, f.getObject());
				qry.setAt(KeyConstants._id, row, f.getId());
				qry.setAt(KeyConstants._purpose, row, f.getPurpose());
				qry.setAt(KeyConstants._filename, row, f.getFilename());
				qry.setAt(KeyConstants._bytes, row, f.getBytes());
				qry.setAt(CREATED_AT, row, f.getCreatedAt());
				qry.setAt(KeyConstants._status, row, f.getStatus());
				qry.setAt(STATUS_DETAILS, row, f.getStatusDetails());
			}
			meta.set(KeyConstants._files, qry);
		}
		return meta;
	}

	private static final String getCharset(ContentType ct) {
		String charset = null;
		if (ct != null) charset = ct.getCharset();
		if (!StringUtil.isEmpty(charset)) return charset;

		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) return pc.getWebCharset().name();
		return "ISO-8859-1";
	}
}
