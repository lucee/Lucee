component {
	// Statement.executeBatch() declares int[], executeLargeBatch() long[]
	function executeBatch() {
		return [ 1, 2, 3 ];
	}
	function executeLargeBatch() {
		return [ 4, 5 ];
	}
}
