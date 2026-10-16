package lucee.runtime.mvn;

public final class Repository {
	private String id;
	private String name;
	private String url;
	private boolean releasesEnabled = true;
	private boolean snapshotsEnabled = true;

	public Repository(String url) {
		this.url = url.endsWith("/") ? url : (url + "/");
	}

	public Repository(String id, String name, String url) {
		this(url);
		this.id = id;
		this.name = name;
	}

	public Repository(String id, String name, String url, boolean releasesEnabled, boolean snapshotsEnabled) {
		this(id, name, url);
		this.releasesEnabled = releasesEnabled;
		this.snapshotsEnabled = snapshotsEnabled;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public boolean isReleasesEnabled() {
		return releasesEnabled;
	}

	public boolean isSnapshotsEnabled() {
		return snapshotsEnabled;
	}

	@Override
	public String toString() {
		return url;
	}
}
