/**
 * LDEV-6078: Component extends without init
 * Tests line number bug with extends but no init function
 * Bug: Label maps to component extends line and first function line
 */
component extends="TestComponent" {

	/**
	 * First method (no init) - should be line 10
	 */
	function call() {
		return execute(
			( struct args ) => {
				if ( isClosure( variables.target ) || isCustomFunction( variables.target ) ) {
					return variables.target();
				} else {
					return invoke( variables.target, variables.method );
				}
			},
			"Callable",
			arguments
		);
	}

}
