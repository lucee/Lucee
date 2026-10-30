component implements="IDogFactory" {

	// a wider type than the interface
	public Animal function make() {
		return new Animal();
	}

}
