component {

	function init( string responseFormat=null ) {
		variables.responseFormat = responseFormat;
		return this;
	}

	function hasResponseFormat() {
		return structKeyExists( variables, "responseFormat" );
	}

	function isResponseFormatNull() {
		return isNull( variables.responseFormat );
	}
}
