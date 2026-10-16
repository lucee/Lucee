// java.lang.reflect.InvocationHandler for a lucee.runtime.config.ConfigPro stand-in.
// ConfigUtil.getPageSourceExisting() only calls getMappings() when pc=null and the path does not start with /mapping-
component {
	function init( required any mappings ) {
		variables.mappings = arguments.mappings;
		return this;
	}
	function invoke( proxy, method, args ) {
		if ( arguments.method.getName() == "getMappings" ) return variables.mappings;
		throw "unexpected call to ConfigPro.#arguments.method.getName()#()";
	}
}
