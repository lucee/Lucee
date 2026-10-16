component extends="Base" {

	function init( string tag = "child-default" ) {
		super.init();
		this.tag = arguments.tag;
		return this;
	}

	// === read patterns ===

	function readSuperMethod() {
		return super.basePublicMethod();
	}

	function readSuperPrivateMethod() {
		return super.basePrivateMethod();
	}

	function readSuperPublicProp() {
		return super.basePublicProp;
	}

	function readSuperBracket( required string key ) {
		return super[ arguments.key ];
	}

	function structKeyExistsOnSuper( required string key ) {
		return structKeyExists( super, arguments.key );
	}

	// === identity / metadata ===

	function isSuperInstanceOf( required string type ) {
		return isInstanceOf( super, arguments.type );
	}

	function metaDataOfSuper() {
		return getMetaData( super );
	}

	// === closure / lambda capture ===

	function superInClosure() {
		// Closure declared inside a method; super referenced inside the closure body.
		// CFML closures capture the enclosing scope including `super`.
		var inner = function() {
			return super.basePublicMethod();
		};
		return inner();
	}

	function superInLambda() {
		// Arrow-syntax lambda. Different bytecode shape from closures; super resolution
		// inside lambdas is a documented gotcha area.
		var inner = () => super.basePublicMethod();
		return inner();
	}

	function superInNestedClosure() {
		var outer = function() {
			var inner = function() {
				return super.basePublicMethod();
			};
			return inner();
		};
		return outer();
	}

	function superCapturedToVariable() {
		var s = super;
		return s.basePublicMethod();
	}

	function returnSuperReference() {
		return super;
	}

	function returnSuperInArray() {
		// LDEV-976 shape: super components as return types inside arrays.
		return [ super, "marker" ];
	}

	// === onMissingMethod fallthrough ===

	function callSuperNonExistentMethod() {
		return super.someMethodThatDoesNotExist();
	}

}
