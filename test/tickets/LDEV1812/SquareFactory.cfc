component implements="IShapeFactory" {

	// component implementing an interface that extends the declared interface
	public Square function make() {
		return new Square();
	}

}
