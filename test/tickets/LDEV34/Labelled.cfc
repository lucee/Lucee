component {

	// label directly followed by the loop (always worked)
	function forNoComment() {
		var n = 0;
		outer:
		for ( var x in [ 1, 2, 3, 4, 5 ] ) {
			for ( var y in [ 1, 2, 3, 4, 5 ] ) {
				n++;
				if ( x * y >= 10 ) break outer;
			}
		}
		return n;
	}

	function forSingleLineComment() {
		var n = 0;
		outer: // comment here
		for ( var x in [ 1, 2, 3, 4, 5 ] ) {
			for ( var y in [ 1, 2, 3, 4, 5 ] ) {
				n++;
				if ( x * y >= 10 ) break outer;
			}
		}
		return n;
	}

	function forMultiLineComment() {
		var n = 0;
		outer: /* comment
		over two lines */
		for ( var x in [ 1, 2, 3, 4, 5 ] ) {
			for ( var y in [ 1, 2, 3, 4, 5 ] ) {
				n++;
				if ( x * y >= 10 ) break outer;
			}
		}
		return n;
	}

	function whileSingleLineComment() {
		var n = 0;
		var x = 0;
		outer: // comment here
		while ( x < 5 ) {
			x++;
			var y = 0;
			while ( y < 5 ) {
				y++;
				n++;
				if ( x * y >= 10 ) break outer;
			}
		}
		return n;
	}

	function doWhileSingleLineComment() {
		var n = 0;
		var x = 0;
		outer: // comment here
		do {
			x++;
			var y = 0;
			do {
				y++;
				n++;
				if ( x * y >= 10 ) break outer;
			} while ( y < 5 );
		} while ( x < 5 );
		return n;
	}

}
