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

	// reads class-level metadata while dispatching from a base UDF.
	// Surfaces the bucket-A `top.properties.X` reads (final fields on ComponentProperties):
	// when called via inheritance, every field below must read the calling LEAF's properties
	// (e.g. "Child"), not Grandparent's. Pins phase 2 doesn't skew metadata under shared bases.
	function metaSnapshot() {
		var meta = getMetaData( this );
		return {
			name: meta.name,
			fullname: meta.fullname,
			extends: structKeyExists( meta, "extends" ) ? meta.extends.name : "",
			accessors: structKeyExists( meta, "accessors" ) ? meta.accessors : false,
			persistent: structKeyExists( meta, "persistent" ) ? meta.persistent : false
		};
	}

}
