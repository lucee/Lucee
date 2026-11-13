component {

	private function cachable( x="default" ) {
		return arguments.x;
	}

	function foo() {
		return cachable( "called" );
	}

	function fooNamed() {
		return cachable( x="named" );
	}

}
