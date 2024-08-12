package lucee.runtime.ai;

import java.util.Date;

public interface AIFile {
	public String getObject();

	public String getId();

	public String getPurpose();

	public String getFilename();

	public long getBytes();

	public Date getCreatedAt();

	public String getStatus();

	public String getStatusDetails();
}