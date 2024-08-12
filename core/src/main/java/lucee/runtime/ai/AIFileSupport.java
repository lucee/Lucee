package lucee.runtime.ai;

import java.util.Date;

public class AIFileSupport implements AIFile {

	private String object;
	private String id;
	private String purpose;
	private String filename;
	private long bytes;
	private Date createdAt;
	private String status;
	private String statusDetails;

	public AIFileSupport(String object, String id, String purpose, String filename, long bytes, Date createdAt, String status, String statusDetails) {
		this.object = object;
		this.id = id;
		this.purpose = purpose;
		this.filename = filename;
		this.bytes = bytes;
		this.createdAt = createdAt;
		this.status = status;
		this.statusDetails = statusDetails;
	}

	@Override
	public String getObject() {
		return object;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getPurpose() {
		return purpose;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public long getBytes() {
		return bytes;
	}

	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public String getStatusDetails() {
		return statusDetails;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "object:" + object + ";id:" + id + ";purpose:" + purpose + ";filename:" + filename + ";bytes:" + bytes + ";createdAt:" + createdAt + ";status:" + status
				+ ";statusDetails:" + statusDetails + ";";
	}

}
