/**
 * AI engine implemented as a CFC, its session implements AISession directly (not AISessionSupport)
 */
component implementsJava="lucee.runtime.ai.AIEngine" {

	function init( cd, properties, name, _default, id ) {
		variables.cd = arguments.cd;
		variables.properties = arguments.properties;
		variables.name = arguments.name;
		variables._default = arguments._default;
		variables.id = arguments.id;
		return asJava( this, "lucee.runtime.ai.AIEngine" );
	}

	function createSession( initialMessage, history, limit, temp, connectTimeout, socketTimeout ) {
		var sess = new "#variables.properties.session#"(
			engine = this,
			systemMessage = arguments.initialMessage ?: "",
			history = arguments.history ?: []
		);
		return asJava( sess, "lucee.runtime.ai.AISession" );
	}

	/**
	 * a CFC returned from a proxied method isn't converted to the declared interface, so wrap it explicitly
	 */
	function asJava( cfc, iface ) {
		return createDynamicProxy( arguments.cfc, [ arguments.iface ] );
	}

	function getId() { return variables.id; }
	function getSocketTimeout() { return -1; }
	function getConnectTimeout() { return -1; }
	function getLabel() { return variables.properties.label; }
	function getModel() { return variables.properties.model; }
	function getModels( defaultValue ) {
		if ( !isNull( arguments.defaultValue ) ) return arguments.defaultValue;
		return createObject( "java", "java.util.ArrayList" ).init();
	}
	function getConversationSizeLimit() { return 50; }
	function getTemperature() { return javaCast( "null", "" ); }
	function getDefault() { return variables._default ?: ""; }
	function getName() { return variables.name; }
	function getClassDefinition() { return variables.cd; }
	function getProperties() { return variables.properties; }

}
