component implements="IFactory" {

	// not an Animal
	public Rock function make() {
		return new Rock();
	}

}
