component {
	// No modifiers at all - should NOT have access or returnType in AST
	function noModifiers() {}

	// Explicit public only - should have access="public" but NOT returnType
	public function explicitPublic() {}

	// Explicit any return only - should have returnType="any" but NOT access
	any function explicitAnyReturn() {}

	// Both explicit - should have both access="public" and returnType="any"
	public any function explicitBoth() {}

	// Only return type (string) - should have returnType but NOT access
	string function onlyReturnType() {
		return "";
	}

	// Only access (private) - should have access but NOT returnType
	private function onlyAccess() {}
}
