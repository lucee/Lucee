component {

	function init( a = 0 ) {
		variables.a = arguments.a;
		return this;
	}

	function getA() {
		return variables.a;
	}

	static function create() {
		return new self();
	}

	static function createNamed( a ) {
		return new self( a = arguments.a );
	}

	static function createPositional( a ) {
		return new self( arguments.a );
	}

	function make() {
		return new self();
	}

	function makeNamed( a ) {
		return new self( a = arguments.a );
	}

	function makeViaClosure() {
		var f = function() {
			return new self();
		};
		return f();
	}

	function makeViaEvaluate() {
		return evaluate( "new self()" );
	}

	function makeViaEvaluateArgs() {
		return evaluate( "new self( 7 )" );
	}

	function makeQuoted() {
		return new "self"();
	}

	function makeUpperCase() {
		return new SELF();
	}

}
