/**
 * LDEV-6078: Interface with docblock
 * Tests line number bug with interface keyword after multiline comment
 * Bug: Label L0 maps to both interface line and first function line
 */
interface {

	/**
	 * First method - should be line 11
	 */
	void function execute();

	/**
	 * Second method - should be line 16
	 */
	any function getResult();

}
