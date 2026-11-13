component extends="NestedStaticBase" {

	variables.id = createUUID();

	static {
		// inherited static scope: while this static constructor runs the write must stay on
		// Inner, not fall through to NestedStaticBase where the key already exists
		static.shared = "inner";

		function innerFn() {
			return "inner-fn";
		}
	}

	public function getId() {
		return variables.id;
	}

}
