component extends="Service" {

	remote string function childPing() {
		return "child";
	}

	public string function childShared() {
		return "childShared";
	}

	private string function childLocal() {
		return "childLocal";
	}

}
