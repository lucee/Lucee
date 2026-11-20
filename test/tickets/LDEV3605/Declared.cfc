component {
	function onMissingMethod( name, args ) {
		return { name: arguments.name, args: arguments.args, keys: structKeyList( arguments ) };
	}
}
