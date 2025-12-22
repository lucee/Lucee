component {

	static {
		static.CONSTANT = "static_constant_value";
	}

	public static function staticMethod( required string arg ) {
		return "called with: " & arguments.arg;
	}

}
