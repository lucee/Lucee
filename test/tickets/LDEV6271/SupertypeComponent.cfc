component accessors="true" {

	property name="controller";
	property name="cachebox";
	property name="logBox";

	function init() {
		variables.controller = new ControllerStub();
		return this;
	}

	function getCache( name = "default" ) {
		return variables.controller.getCache( arguments.name );
	}

	// Mirrors ColdBox's includeUDF() — calls getCache() internally
	function doInternalWork() {
		var cache = getCache( "default" );
		if ( isNull( cache ) ) throw( message="getCache returned null", type="NullCacheError" );
		return cache.getOrSet( "someKey", function() { return "value"; }, 10080 );
	}

}
