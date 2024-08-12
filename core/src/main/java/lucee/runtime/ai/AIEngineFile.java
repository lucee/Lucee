package lucee.runtime.ai;

import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;

public interface AIEngineFile {
	public List<AIFile> listFiles() throws PageException;

	public String uploadFile(Resource jsonl) throws PageException;
}
