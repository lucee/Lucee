component {

	function getMessage() {
		return variables.message ?: "";
	}

	function setMessage( required string message ) {
		variables.message = arguments.message;
	}

	function getCount() {
		return variables.count ?: 0;
	}

	function setCount( required numeric count ) {
		variables.count = arguments.count;
	}

}
