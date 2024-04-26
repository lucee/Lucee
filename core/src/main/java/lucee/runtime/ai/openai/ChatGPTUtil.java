package lucee.runtime.ai.openai;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

public class ChatGPTUtil {

	public static Exception toException(String msg, String type, String code) {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		PageException ae = eng.getExceptionUtil().createApplicationException(msg, "type:" + type + ";code:" + code);
		ae.setErrorCode(code);
		return ae;
	}

}
