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
		var rsp = variables.engine.asJava( new SimpleResponse( answer = "[#variables.systemMessage#] #arguments.message#" ), "lucee.runtime.ai.Response" );
		var req = variables.engine.asJava( new SimpleRequest( question = arguments.message ), "lucee.runtime.ai.Request" );
		arrayAppend( variables.history, variables.engine.asJava( new SimpleConversation( req = req, rsp = rsp ), "lucee.runtime.ai.Conversation" ) );
		return rsp;
	}

	function getSystemMessage() { return variables.systemMessage; }
	// a CFML array isn't converted to Conversation[] by the proxy
	function getHistory() { return javaCast( "lucee.runtime.ai.Conversation[]", variables.history ); }
	function getId() { return variables.id; }
	function getEngine() { return variables.engine.asJava( variables.engine, "lucee.runtime.ai.AIEngine" ); }
	function getConversationSizeLimit() { return 50; }
	function getTemperature() { return javaCast( "null", "" ); }
	function getSocketTimeout() { return -1; }
	function getConnectTimeout() { return -1; }
	function release() {}

}
