component accessors="true" {

	property name="gprop" default="g-default";

	function chain() {
		return "grand";
	}

	// dispatched on a child instance via inheritance — `this` must resolve to the child,
	// `this.tag` must read the child's per-instance field, not the base's
	function whoAmI() {
		return this.tag;
	}

	// reads `this` and writes to it — should mutate the calling child, not the base
	function tagMe( required string newTag ) {
		this.tag = arguments.newTag;
	}

}
