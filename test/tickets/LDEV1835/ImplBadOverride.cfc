component implements="IBadOverride" {

	// the override has an additional required argument, which is not compatible with the interface
	public string function f( required string name, required string other ) {
		return arguments.name;
	}

}
