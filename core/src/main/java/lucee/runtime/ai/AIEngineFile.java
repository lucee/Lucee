package lucee.runtime.ai;

import java.io.InputStream;
import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;

public interface AIEngineFile {

	public static final String PURPOSE_ASSISTANTS = "assistants";
	public static final String PURPOSE_VISION = "vision";
	public static final String PURPOSE_FInE_TUNE = "fine-tune";

	public List<AIFile> listFiles() throws PageException;

	public String uploadFile(Resource jsonl, String purpose) throws PageException;

	public AIFile getFile(String id) throws PageException;

	public InputStream getFileContent(String id) throws PageException;

	public boolean deleteFile(String id) throws PageException;
}
