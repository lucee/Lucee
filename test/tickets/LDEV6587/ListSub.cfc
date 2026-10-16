component {
	// List.subList() declares List; a CFML array is already a java.util.List
	function subList( fromIndex, toIndex ) {
		return [ "b", "c" ];
	}
}
