component extends="org.lucee.cfml.test.LuceeTestCase" labels="array,cast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6806 native Java arrays cast without copying", function() {

			it( title="arrayLen/arrayIsEmpty/index/loop over Object[] and primitives", body=function() {
				_assertReads( "String[]", [ "a", "b", "c" ], "a", "a,b,c", '["a","b","c"]' );
				_assertReads( "boolean[]", [ true, false, true ], true, "true,false,true", "[true,false,true]" );
				_assertReads( "byte[]", [ 1, 2, 3 ], 1, 6, "[1,2,3]" );
				_assertReads( "char[]", [ "x", "y", "z" ], "x", "x,y,z", '["x","y","z"]' );
				_assertReads( "short[]", [ 1, 2, 3 ], 1, 6, "[1,2,3]" );
				_assertReads( "int[]", [ 10, 20, 30 ], 10, 60, "[10,20,30]" );
				_assertReads( "long[]", [ 10, 20, 30 ], 10, 60, "[10,20,30]" );
				_assertReads( "float[]", [ 1.5, 2.5, 3.5 ], 1.5, 7.5, "[1.5,2.5,3.5]" );
				_assertReads( "double[]", [ 1.5, 2.5, 3.5 ], 1.5, 7.5, "[1.5,2.5,3.5]" );
			});

			it( title="empty native arrays", body=function() {
				var emptyObj = _wrap( javaCast( "String[]", [] ) );
				expect( arrayLen( emptyObj ) ).toBe( 0 );
				expect( arrayIsEmpty( emptyObj ) ).toBeTrue();
				expect( serializeJSON( emptyObj ) ).toBe( "[]" );

				var emptyInt = _wrap( javaCast( "int[]", [] ) );
				expect( arrayLen( emptyInt ) ).toBe( 0 );
				expect( arrayIsEmpty( emptyInt ) ).toBeTrue();
				expect( serializeJSON( emptyInt ) ).toBe( "[]" );
			});

			it( title="mutating the CFML array does not change the original Java array", body=function() {
				var ReflectArray = createObject( "java", "java.lang.reflect.Array" );

				var obj = javaCast( "String[]", [ "a", "b", "c" ] );
				var wrappedObj = _wrap( obj );
				wrappedObj[ 1 ] = "z";
				arrayAppend( wrappedObj, "extra" );
				expect( arrayLen( wrappedObj ) ).toBe( 4 );
				expect( wrappedObj[ 1 ] ).toBe( "z" );
				expect( ReflectArray.getLength( obj ) ).toBe( 3 );
				expect( ReflectArray.get( obj, 0 ) ).toBe( "a" );

				var ints = javaCast( "int[]", [ 1, 2, 3 ] );
				var wrappedInts = _wrap( ints );
				wrappedInts[ 1 ] = 99;
				arrayAppend( wrappedInts, 1000 );
				expect( arrayLen( wrappedInts ) ).toBe( 4 );
				expect( wrappedInts[ 1 ] ).toBe( 99 );
				expect( ReflectArray.getLength( ints ) ).toBe( 3 );
				expect( ReflectArray.getInt( ints, 0 ) ).toBe( 1 );
			});

			it( title="append grows the CFML array after copy-on-write", body=function() {
				var arr = _wrap( javaCast( "int[]", [ 1, 2 ] ) );
				arrayAppend( arr, 3 );
				arrayAppend( arr, 4 );
				expect( arrayLen( arr ) ).toBe( 4 );
				expect( arrayToList( arr ) ).toBe( "1,2,3,4" );
			});

			it( title="duplicate returns an independent copy", body=function() {
				var src = _wrap( javaCast( "String[]", [ "a", "b" ] ) );
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

	/**
	 * Keep an explicit Array view from Caster.toArray. Typed CFML `array` arguments
	 * leave the native Java array in the local variable and only cast at BIF call
	 * sites, so mutations need a held Array reference to exercise copy-on-write.
	 */
	private any function _wrap( required any javaArr ) {
		return createObject( "java", "lucee.runtime.op.Caster" ).toArray( javaArr );
	}

	private void function _assertReads( required string javaType, required array values, required any first, required any loopExpect, required string expectJson ) {
		var arr = _wrap( javaCast( javaType, values ) );
		var label = javaType;

		expect( arrayLen( arr ) ).toBe( 3, label & " arrayLen" );
		expect( arrayIsEmpty( arr ) ).toBeFalse( label & " arrayIsEmpty" );
		expect( arr[ 1 ] ).toBe( first, label & " get[1]" );

		if ( isSimpleValue( loopExpect ) && !isNumeric( loopExpect ) ) {
			var out = [];
			for ( var v in arr ) arrayAppend( out, v );
			expect( arrayToList( out ) ).toBe( loopExpect, label & " loop" );
		}
		else {
			var s = 0;
			for ( var v in arr ) s += v;
			expect( s ).toBe( loopExpect, label & " loop" );
		}

		var n = 0;
		arrayEach( arr, function( v ) { n++; } );
		expect( n ).toBe( 3, label & " each" );
		expect( serializeJSON( arr ) ).toBe( expectJson, label & " serializeJSON" );
	}

}
