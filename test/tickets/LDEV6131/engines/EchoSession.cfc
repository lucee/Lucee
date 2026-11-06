/**
 * answers "[systemMessage] message", so a lost system message shows in the answer
 */
component implementsJava="lucee.runtime.ai.AISession" {

	function init( engine, systemMessage, history ) {
		variables.engine = arguments.engine;
		variables.systemMessage = arguments.systemMessage;
		variables.history = [];
		loop array=arguments.history item="local.c" {
			arrayAppend( variables.history, c );
		}
		variables.id = createUUID();
		return this;
	}

	function inquiry( message, listener ) {
		var rsp = new SimpleResponse( answer = "[#variables.systemMessage#] #arguments.message#" );
		arrayAppend( variables.history, new SimpleConversation( req = new SimpleRequest( question = arguments.message ), rsp = rsp ) );
		return rsp;
	}

	function getSystemMessage() { return variables.systemMessage; }
	function getHistory() { return variables.history; }
	function getId() { return variables.id; }
	function getEngine() { return variables.engine; }
	function getConversationSizeLimit() { return 50; }
	function getTemperature() { return javaCast( "null", "" ); }
	function getSocketTimeout() { return -1; }
	function getConnectTimeout() { return -1; }
	function release() {}

}
