component implements="test2" {
	public numeric function create() {
		return 1;
	}
	public boolean function exists( required string id ) {
		return arguments.id == "lucee";
	}
	public void function delete() {
	}
}
