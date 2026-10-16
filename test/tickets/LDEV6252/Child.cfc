component extends="Base" accessors="true" {

	property name="name" type="string";
	property name="age" type="numeric";

	function init( string name = "default", numeric age = 0 ) {
		variables.name = arguments.name;
		variables.age = arguments.age;
		return this;
	}

	function getVariablesThis() {
		return variables.this;
	}

	function getVariablesSuper() {
		return variables.super;
	}

	function getVariablesStatic() {
		return variables.static;
	}

	function getPropertyViaVariables( required string prop ) {
		return variables[ arguments.prop ];
	}

	function callSuperBaseMethod() {
		return variables.super.baseMethod();
	}

	function variablesContainsKey( required string key ) {
		return structKeyExists( variables, arguments.key );
	}

	function trySetVariablesThis() {
		variables["this"] = "hacked";
		return variables.this;
	}

	function trySetVariablesSuper() {
		variables["super"] = "hacked";
		return variables.super;
	}

	function setPropertyNull( required string prop ) {
		variables[ arguments.prop ] = nullValue();
	}

	function childMethod() {
		return "from child";
	}

}
