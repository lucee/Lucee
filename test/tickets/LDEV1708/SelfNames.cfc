component {

	// "self" as a function name, a variable, an argument and a property is still fine, only "new self(" is special
	property name="self";

	static function self() {
		return new Other();
	}

	function viaVar() {
		var self = this;
		return self.viaArg( self );
	}

	function viaArg( self ) {
		return self;
	}

	function viaStruct() {
		var self = { a: 1 };
		var other = new Other();
		return self.a;
	}

	function viaClosure() {
		var f = function( self ) {
			return self;
		};
		return f( 42 );
	}

	function viaLocalFunction() {
		var self = function() {
			return new Other();
		};
		return self();
	}

}
