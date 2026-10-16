component accessors="true" {

	// Base class for the 2-level super-dispatch fixtures. Defines methods, properties,
	// and an onMissingMethod handler that the Child class will exercise via `super.X`.

	property name="basePublicProp" default="base-public-default";

	function init() {
		this.basePublicProp = "base-public-default";
		variables.baseVariablesProp = "base-variables-default";
		this.baseThisProp = "base-this-default";
		return this;
	}

	function basePublicMethod() {
		return "base-public-method-result";
	}

	private function basePrivateMethod() {
		return "base-private-method-result";
	}

	function whoAmIFromBase() {
		// Dispatched on a Child via super.whoAmIFromBase() — `this` must resolve to the Child,
		// not the Base. Pins the CFC-instance dispatch contract.
		return this.tag;
	}

	function onMissingMethod( required string missingMethodName, required struct missingMethodArguments ) {
		return "base-omm:" & arguments.missingMethodName;
	}

}
