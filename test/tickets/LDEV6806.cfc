component extends="org.lucee.cfml.test.LuceeTestCase" labels="array,cast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6806 native Java arrays cast without copying", function() {

			it( title="arrayLen/arrayIsEmpty/index/loop over Object[] and primitives", body=function() {
				var cases = [
					{ label: "Object[]",  arr: javaCast( "String[]", [ "a", "b", "c" ] ), expectJson: '["a","b","c"]', first: "a", loop: "a,b,c" },
					{ label: "boolean[]", arr: javaCast( "boolean[]", [ true, false, true ] ), expectJson: "[true,false,true]", first: true, loop: "true,false,true" },
					{ label: "byte[]",    arr: javaCast( "byte[]", [ 1, 2, 3 ] ), expectJson: "[1,2,3]", first: 1, loop: 6 },
					{ label: "char[]",    arr: javaCast( "char[]", [ "x", "y", "z" ] ), expectJson: '["x","y","z"]', first: "x", loop: "x,y,z" },
					{ label: "short[]",   arr: javaCast( "short[]", [ 1, 2, 3 ] ), expectJson: "[1,2,3]", first: 1, loop: 6 },
					{ label: "int[]",     arr: javaCast( "int[]", [ 10, 20, 30 ] ), expectJson: "[10,20,30]", first: 10, loop: 60 },
					{ label: "long[]",    arr: javaCast( "long[]", [ 10, 20, 30 ] ), expectJson: "[10,20,30]", first: 10, loop: 60 },
					{ label: "float[]",   arr: javaCast( "float[]", [ 1.5, 2.5, 3.5 ] ), expectJson: "[1.5,2.5,3.5]", first: 1.5, loop: 7.5 },
					{ label: "double[]",  arr: javaCast( "double[]", [ 1.5, 2.5, 3.5 ] ), expectJson: "[1.5,2.5,3.5]", first: 1.5, loop: 7.5 }
				];

				for ( var c in cases ) {
					expect( arrayLen( c.arr ) ).toBe( 3, c.label & " arrayLen" );
					expect( arrayIsEmpty( c.arr ) ).toBeFalse( c.label & " arrayIsEmpty" );
					expect( _get( c.arr, 1 ) ).toBe( c.first, c.label & " get[1]" );
					expect( _sumOrJoin( c ) ).toBe( c.loop, c.label & " loop" );
					expect( _eachCount( c.arr ) ).toBe( 3, c.label & " each" );
					expect( serializeJSON( _asArray( c.arr ) ) ).toBe( c.expectJson, c.label & " serializeJSON" );
				}
			});

			it( title="empty native arrays", body=function() {
				expect( arrayLen( javaCast( "String[]", [] ) ) ).toBe( 0 );
				expect( arrayIsEmpty( javaCast( "String[]", [] ) ) ).toBeTrue();
				expect( arrayLen( javaCast( "int[]", [] ) ) ).toBe( 0 );
				expect( arrayIsEmpty( javaCast( "int[]", [] ) ) ).toBeTrue();
				expect( serializeJSON( _asArray( javaCast( "int[]", [] ) ) ) ).toBe( "[]" );
			});

			it( title="mutating the CFML array does not change the original Java array", body=function() {
				var ReflectArray = createObject( "java", "java.lang.reflect.Array" );

				var obj = javaCast( "String[]", [ "a", "b", "c" ] );
				var mutatedObj = _mutate( obj, "z" );
				expect( arrayLen( mutatedObj ) ).toBe( 4 );
				expect( mutatedObj[ 1 ] ).toBe( "z" );
				expect( ReflectArray.getLength( obj ) ).toBe( 3 );
				expect( ReflectArray.get( obj, 0 ) ).toBe( "a" );

				var ints = javaCast( "int[]", [ 1, 2, 3 ] );
				var mutatedInts = _mutateNumeric( ints, 99 );
				expect( arrayLen( mutatedInts ) ).toBe( 4 );
				expect( mutatedInts[ 1 ] ).toBe( 99 );
				expect( ReflectArray.getLength( ints ) ).toBe( 3 );
				expect( ReflectArray.getInt( ints, 0 ) ).toBe( 1 );
			});

			it( title="append grows the CFML array after copy-on-write", body=function() {
				var arr = _asArray( javaCast( "int[]", [ 1, 2 ] ) );
				arrayAppend( arr, 3 );
				arrayAppend( arr, 4 );
				expect( arrayLen( arr ) ).toBe( 4 );
				expect( arrayToList( arr ) ).toBe( "1,2,3,4" );
			});

			it( title="duplicate returns an independent copy", body=function() {
				var src = _asArray( javaCast( "String[]", [ "a", "b" ] ) );
				var dup = duplicate( src );
				arrayAppend( dup, "c" );
				dup[ 1 ] = "z";
				expect( arrayLen( src ) ).toBe( 2 );
				expect( src[ 1 ] ).toBe( "a" );
				expect( arrayLen( dup ) ).toBe( 3 );
				expect( dup[ 1 ] ).toBe( "z" );
			});

			it( title="arrayLen on a large Object[] remains correct across many calls", body=function() {
				// Sanity only: 50k elements, 100 arrayLen calls. No wall-clock assert.
				var n = 50000;
				var tmp = [];
				arrayResize( tmp, n );
				for ( var i = 1; i <= n; i++ ) tmp[ i ] = i;
				var javaArr = javaCast( "Integer[]", tmp );
				var total = 0;
				for ( var r = 1; r <= 100; r++ ) {
					total += arrayLen( javaArr );
				}
				expect( total ).toBe( n * 100 );
			});

		} );
	}

	private any function _asArray( required any javaArr ) {
		// Force the array cast path used by BIFs / typed arguments, and keep the resulting Array.
		return _identity( javaArr );
	}

	private array function _identity( required array arr ) {
		return arr;
	}

	private any function _get( required any javaArr, required numeric idx ) {
		return _asArray( javaArr )[ idx ];
	}

	private array function _mutate( required any javaArr, required any newFirst ) {
		var arr = _asArray( javaArr );
		arr[ 1 ] = newFirst;
		arrayAppend( arr, "extra" );
		return arr;
	}

	private array function _mutateNumeric( required any javaArr, required numeric newFirst ) {
		var arr = _asArray( javaArr );
		arr[ 1 ] = newFirst;
		arrayAppend( arr, 1000 );
		return arr;
	}

	private numeric function _eachCount( required any javaArr ) {
		var n = 0;
		arrayEach( _asArray( javaArr ), function( v ) { n++; } );
		return n;
	}

	private any function _sumOrJoin( required struct c ) {
		if ( isSimpleValue( c.loop ) && !isNumeric( c.loop ) ) {
			var out = [];
			for ( var v in _asArray( c.arr ) ) arrayAppend( out, v );
			return arrayToList( out );
		}
		var s = 0;
		for ( var v in _asArray( c.arr ) ) s += v;
		return s;
	}

}
