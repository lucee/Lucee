package lucee.commons.io.log;

import java.io.IOException;
import java.nio.charset.Charset;

import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;

public interface LogEngine {
	
	public Log getConsoleLog(boolean errorStream, String name, int level);
	
	public Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout,RetireListener listener, boolean async) throws IOException;
		
}
