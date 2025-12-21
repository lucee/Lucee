/**
 * Component-level docblock
 * @author Test Author
 * @version 1.0
 */
component {

	/** Single line docblock */
	function singleLineDoc() {
		return "single";
	}

	/**
	 * @return string The result
	 */
	function onlyTags() {
		return "tags only";
	}

	/**
	 * Function with multiple params
	 * @a First parameter
	 * @b Second parameter
	 * @c Third parameter
	 */
	function multipleParams( string a, string b, string c ) {
		return a & b & c;
	}

	/** */
	function emptyDocblock() {
		return "empty";
	}

	/**
	 * Contains <html> tags & ampersands and "quotes" too
	 */
	function specialChars() {
		return "special";
	}

}
