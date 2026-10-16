/**
 * LDEV-6078: Component extends with init
 * Tests line number bug with extends and init function
 * Bug: Label L2 maps to both component extends line and init function line
 */
component extends="TestComponent" {

	/**
	 * Constructor - should be line 10
	 */
	function init( required f ) {
		super.init( arguments.f );
		return this;
	}

	/**
	 * Second method - should be line 18
	 */
	function accept( required t ) {
		return execute(
			( struct args ) => {
				variables.target( args.t );
			},
			"Consumer",
			arguments
		);
	}

}
