component {

	// Plain CFC with no explicit `extends` — its base is the implicit Component.cfc
	// instance returned by ConfigWebHelper.getBaseComponentInstance. Today that base
	// is a fresh duplicate per child; LDEV-6300 phase 3 collapses it to a shared ref.

	function init() {
		return this;
	}

	function writeSuperFoo( required any value ) {
		super.foo = arguments.value;
	}

	function writeSuperKey( required string key, required any value ) {
		super[ arguments.key ] = arguments.value;
	}

	function readSuperFoo() {
		return super.foo;
	}

	function readSuperKey( required string key ) {
		return super[ arguments.key ];
	}

	function readSuperKeyOrDefault( required string key, required any defaultValue ) {
		try {
			return super[ arguments.key ];
		} catch ( any e ) {
			return arguments.defaultValue;
		}
	}

	function clearSuper() {
		structClear( super );
	}

	function removeSuperKey( required string key ) {
		structDelete( super, arguments.key );
	}

}
