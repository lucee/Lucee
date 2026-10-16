/**
 * LDEV-6078: Test component for bytecode line numbers
 * Each statement should have a unique line number
 */
component {

	// Line 8: property declaration
	property name="value" type="numeric" default="0";

	/**
	 * Constructor - Line 12
	 */
	function init( numeric initialValue = 0 ) {
		// Line 15: assignment
		variables.value = arguments.initialValue;
		// Line 17: return
		return this;
	}

	/**
	 * Increment with condition - Line 22
	 */
	function increment( required numeric amount ) {
		// Line 25: local var
		var result = 0;
		// Line 27: if condition
		if ( arguments.amount > 0 ) {
			// Line 29: assignment
			variables.value += arguments.amount;
			// Line 31: assignment
			result = variables.value;
		}
		// Line 34: return
		return result;
	}

	/**
	 * Process with multiple statements - Line 39
	 */
	function process() {
		// Line 42: var declarations
		var a = 1;
		// Line 44: var declarations
		var b = 2;
		// Line 46: var declarations
		var c = 3;
		// Line 48: calculation
		var total = a + b + c;
		// Line 50: return
		return total;
	}

}
