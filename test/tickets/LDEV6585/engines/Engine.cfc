/**
 * AI engine implemented as a CFC, its session implements AISession directly (not AISessionSupport)
 */
component implementsJava="lucee.runtime.ai.AIEngine" {

	function init( cd, properties, name, _default="", id ) {
		variables.cd = arguments.cd;
		variables.properties = arguments.properties;
		variables.name = arguments.name;
		variables._default = arguments._default;
		variables.id = arguments.id;
		return this;
	}

	function createSession( initialMessage="", history=[], limit, temp, connectTimeout, socketTimeout ) {
		return new "#variables.properties.session#"( engine = this, systemMessage = arguments.initialMessage, history = arguments.history );
	}

	function getId() { return variables.id; }
	function getSocketTimeout() { return -1; }
	function getConnectTimeout() { return -1; }
	function getLabel() { return variables.properties.label; }
	function getModel() { return variables.properties.model; }
	function getModels( defaultValue ) { return []; }
	function getConversationSizeLimit() { return 50; }
	function getTemperature() { return javaCast( "null", "" ); }
	function getDefault() { return variables._default; }
	function getName() { return variables.name; }
	function getClassDefinition() { return variables.cd; }
	function getProperties() { return variables.properties; }

}
