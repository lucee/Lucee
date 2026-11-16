component {

	public static final string function g() {
		return getFunctionCalledName();
	}

	public final static numeric function sum( numeric a, numeric b ) {
		return arguments.a + arguments.b;
	}

	static final function noAccess() {
		return "noAccess";
	}

	final public static function finalFirst() {
		return "finalFirst";
	}

	public static function onlyStatic() {
		return "onlyStatic";
	}

	public final function instanceFinal() {
		return "instanceFinal";
	}
}
