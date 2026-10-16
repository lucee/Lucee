component {

	variables.cacheName = "";

	function init( required string name ) {
		variables.cacheName = arguments.name;
		return this;
	}

	function getName() {
		return variables.cacheName;
	}

	function getOrSet( required string key, required producer, numeric timeout = 0 ) {
		return arguments.producer();
	}

}
