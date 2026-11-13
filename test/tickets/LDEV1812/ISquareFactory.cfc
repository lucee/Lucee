component implements="IShapeFactory" {

	// interface extending the declared interface
	public ISquare function make() {
		return new Square();
	}

}
