component {

	function withHint(
		required string name hint="The user's full name"
	) {
		return name;
	}

	function withDefault(
		string greeting default="Hello"
	) {
		return greeting;
	}

	function withBoth(
		required string message hint="The message to display" default="Welcome"
	) {
		return message;
	}

	/**
	 * This hint comes from a docblock
	 */
	function withDocblock() {
		return "docblock";
	}

	/**
	 * Function with full docblock annotations
	 * @name The name parameter description
	 * @age The age parameter description
	 * @return string A greeting message
	 * @deprecated Use greetV2 instead
	 */
	function withFullDocblock( string name, numeric age ) {
		return "Hello #name#, you are #age#";
	}

	function withHintAttr() hint="This hint comes from an attribute" {
		return "attribute";
	}

	/**
	 * Docblock description
	 */
	function docblockPlusHint() hint="Attribute hint" {
		return "both";
	}

	remote function remoteFunc() returnformat="json" {
		return { "status": "ok" };
	}

	/**
	 * @cb.hint Callback function hint
	 */
	function withClosureDefault( function cb=function(){} ) {
		return cb();
	}

}
