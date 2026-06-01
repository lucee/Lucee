component {

	function init( string tag = "ctor-default" ) {
		this.tag = arguments.tag;
		include template="mixins.cfm";
		return this;
	}

}
