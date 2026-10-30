component {
	function add( a, b, c, d ) {
		var total = 0;
		for ( var k in arguments ) total += arguments[ k ];
		return total;
	}
}
