component implements="IFactory" {

	// same type as the interface
	public Animal function make() {
		return new Animal();
	}

}
