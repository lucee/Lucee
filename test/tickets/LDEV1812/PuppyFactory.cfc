component implements="IFactory" {

	// 2 levels below the declared return type
	public Puppy function make() {
		return new Puppy();
	}

}
