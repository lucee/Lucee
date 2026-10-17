<cfscript>
	function named4286( a ) {
		return a * 2;
	}
	function apply4286( fn, v ) {
		return fn( v );
	}
	function inner4286() {
		var c = function( v ) { return v + 1; };
		var l = ( v ) => v + 2;
		return c( 1 ) & "," & l( 1 );
	}
	res = [];
	arrayAppend( res, named4286( 2 ) );                                       // 4
	f = function( v ) { return v * 3; };
	arrayAppend( res, f( 2 ) );                                               // 6
	arrayAppend( res, apply4286( function( v ) { return v * 4; }, 2 ) );      // 8
	arrayAppend( res, apply4286( ( v ) => v * 5, 2 ) );                       // 10
	arrayAppend( res, ( function( v ) { return v * 6; } )( 2 ) );            // 12
	arrayAppend( res, inner4286() );                                          // 2,3
	arrayAppend( res, arrayMap( [ 1, 2 ], ( v ) => v * 7 ).toList( "|" ) );  // 7|14
	writeOutput( res.toList( ";" ) );
</cfscript>
