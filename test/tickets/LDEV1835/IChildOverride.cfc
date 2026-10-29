interface extends="IDefault" {

	public default string function greet( required string name ) {
		return "Welcome " & arguments.name;
	}

}
