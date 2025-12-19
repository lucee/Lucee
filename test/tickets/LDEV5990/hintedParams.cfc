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

	function withHintAttr() hint="This hint comes from an attribute" {
		return "attribute";
	}

}
