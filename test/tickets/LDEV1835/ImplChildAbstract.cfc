component implements="IChildAbstract" {

	public string function required() {
		return "x";
	}

	public string function greet( required string name ) {
		return "Abstract " & arguments.name;
	}

}
