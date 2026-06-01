// Pseudo-constructor body throws — new() must halt with the same exception, no partial instance returned.
component {

	throw( type="LooseTest", message="body-threw-on-purpose" );

	function getValue() { return "never-reached"; }

}
