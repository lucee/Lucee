package lucee.runtime.mvn;

public class Repository {
	private String id;
	private String name;
	private String url;

	public Repository(String id, String name, String url) {
		this.id = id;
		this.name = name;
		this.url = url.endsWith("/") ? url : (url + "/");
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

	@Override
	public String toString() {
		return url;
	}
}
