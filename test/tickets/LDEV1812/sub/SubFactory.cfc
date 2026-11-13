component implements="test.tickets.LDEV1812.IFactory" {

	// the type is resolved relative to the component declaring it
	public test.tickets.LDEV1812.Dog function make() {
		return new test.tickets.LDEV1812.Dog();
	}

}
