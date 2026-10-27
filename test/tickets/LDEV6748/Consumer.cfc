component {

	function init( string responseFormat=null ) {
		variables.argumentExists = structKeyExists( arguments, "responseFormat" );
		variables.responseFormat = responseFormat;
		return this;
	}

	function hasResponseFormatArgument() {
		return variables.argumentExists;
	}

	function isResponseFormatNull() {
		return isNull( variables.responseFormat );
	}
}
