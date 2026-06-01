component extends="Boom" {

	// triggers the inherited boom() — the exception is thrown from the base UDF
	// but `this` resolves to this child instance, so the message includes the child's tag
	function trigger() {
		boom();
	}

}
