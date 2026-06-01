component {

	function init( string tag = "post-default" ) {
		this.tag = arguments.tag;
		return this;
	}

	function loadMixins() {
		include template="mixins.cfm";
	}

	// dispatches the mixed-in function from inside the CFC, via variables scope
	function callMixinInternally() {
		return variables.cfincludeMixedFn();
	}

}
