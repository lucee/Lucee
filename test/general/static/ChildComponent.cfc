component extends="BaseComponent" {
	static {
		variables.childStaticVar = "child value";
	}

	public static function childStaticMethod() {
		return "child static method";
	}
}
