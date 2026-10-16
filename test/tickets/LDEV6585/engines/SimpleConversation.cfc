component implementsJava="lucee.runtime.ai.Conversation" {

	function init( req, rsp ) {
		variables.req = arguments.req;
		variables.rsp = arguments.rsp;
		return this;
	}

	function getRequest() { return variables.req; }
	function getResponse() { return variables.rsp; }

}
