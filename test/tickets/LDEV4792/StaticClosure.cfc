component {

	static {
		final lambda = () => "lambda";
		final closure = function( string a = "default" ) {
			return "closure:" & arguments.a;
		};
		notFinal = () => "notFinal";
	}

	static function callLambda() {
		return static.lambda();
	}

	static function callClosure() {
		return static.closure( "positional" );
	}

	static function callClosureNamed() {
		return static.closure( a = "named" );
	}

	static function callNotFinal() {
		return static.notFinal();
	}

	static function readLambda() {
		var fn = static.lambda;
		return fn();
	}

}
