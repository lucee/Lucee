component implements="IFactory" {

	// the type is defined with the full path
	public test.tickets.LDEV1812.Dog function make() {
		return new Dog();
	}

}
