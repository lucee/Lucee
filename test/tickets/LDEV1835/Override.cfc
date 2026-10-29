component implements="IDefault" {

	public string function required() {
		return "override";
	}

	public string function greet( required string name ) {
		return "Hi " & arguments.name;
	}

}
