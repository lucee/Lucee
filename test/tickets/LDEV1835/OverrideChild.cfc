component extends="BaseImplements" {

	public string function greet( required string name ) {
		return "Child " & arguments.name;
	}

}
