component extends="NestedStaticBase" {

	static {
		static.shared = "outer";

		// nested static constructor: Inner's runs to completion, including its own
		// afterStaticConstructor, inside Outer's
		static.innerInstance = new NestedStaticInner();

		// Outer is still inside its own static constructor. This key exists on
		// NestedStaticBase but not yet on Outer, so it only stays local if the
		// inside-static-constructor flag survived the nested constructor
		static.afterNested = "outer";

		function afterInner() {
			return "outer-after-inner";
		}
	}

	public function instanceFn() {
		return "outer-instance";
	}

}
