component {

	// static counter shared across all instances of this class via LDEV-3335 infrastructure
	static {
		static.counter = 0;
	}

	public static function bump() {
		static.counter++;
		return static.counter;
	}

	public static function read() {
		return static.counter;
	}

	public static function reset() {
		static.counter = 0;
	}

}
