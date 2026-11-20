component {

	function init( string scheme, struct args ) {
		request.LDEV3291_init = {
			isStruct: isStruct( arguments.args ),
			upperKey: structKeyExists( arguments.args, "MYARG" ),
			lowerKey: structKeyExists( arguments.args, "myarg" ),
			value: arguments.args.myArg ?: "",
			keys: listSort( structKeyList( arguments.args ), "textnocase" )
		};
		return this;
	}

	function isCaseSensitive() {
		return true;
	}
}
