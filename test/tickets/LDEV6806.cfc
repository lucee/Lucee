component extends="org.lucee.cfml.test.LuceeTestCase" labels="array,cast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6806 native Java arrays cast without copying", function() {

			it( title="arrayLen/arrayIsEmpty/index/loop over Object[] and primitives", body=function() {
				_assertReads( javaCast( "String[]", [ "a", "b", "c" ] ), "Object[]", "a", "a,b,c", '["a","b","c"]', false );
				_assertReads( javaCast( "boolean[]", [ true, false, true ] ), "boolean[]", true, "true,false,true", "[true,false,true]", false );
				_assertReads( javaCast( "byte[]", [ 1, 2, 3 ] ), "byte[]", 1, 6, "", true );
				_assertReads( javaCast( "char[]", [ "x", "y", "z" ] ), "char[]", "x", "x,y,z", '["x","y","z"]', false );
				_assertReads( javaCast( "short[]", [ 1, 2, 3 ] ), "short[]", 1, 6, "[1,2,3]", false );
				_assertReads( javaCast( "int[]", [ 10, 20, 30 ] ), "int[]", 10, 60, "[10,20,30]", false );
				_assertReads( javaCast( "long[]", [ 10, 20, 30 ] ), "long[]", 10, 60, "[10,20,30]", false );
				_assertReads( javaCast( "float[]", [ 1.5, 2.5, 3.5 ] ), "float[]", 1.5, 7.5, "[1.5,2.5,3.5]", false );
				_assertReads( javaCast( "double[]", [ 1.5, 2.5, 3.5 ] ), "double[]", 1.5, 7.5, "[1.5,2.5,3.5]", false );
			});

			it( title="empty native arrays", body=function() {
				_assertEmpty( javaCast( "String[]", [] ) );
				_assertEmpty( javaCast( "int[]", [] ) );
			});

			it( title="mutating the CFML array does not change the original Java array", body=function() {
				var ReflectArray = createObject( "java", "java.lang.reflect.Array" );

				var obj = javaCast( "String[]", [ "a", "b", "c" ] );
				_mutateObject( obj, "z" );
				expect( ReflectArray.getLength( obj ) ).toBe( 3 );
				expect( ReflectArray.get( obj, 0 ) ).toBe( "a" );

				var ints = javaCast( "int[]", [ 1, 2, 3 ] );
				_mutateNumeric( ints, 99 );
				expect( ReflectArray.getLength( ints ) ).toBe( 3 );
				expect( ReflectArray.getInt( ints, 0 ) ).toBe( 1 );
			});

			it( title="append grows the CFML array after copy-on-write", body=function() {
				_assertAppendGrows( javaCast( "int[]", [ 1, 2 ] ) );
			});

			it( title="duplicate returns an independent copy", body=function() {
				_assertDuplicateIndependent( javaCast( "String[]", [ "a", "b" ] ) );
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
	 * All assertions that need the cast Array must run inside a function whose
	 * argument is typed as array, so the view is the local variable. Returning
	 * the view from a UDF can re-materialise a native Object[] via Array.toArray().
	 */
	private void function _assertReads( required array arr, required string label, required any first, required any loopExpect, required string expectJson, required boolean skipJson ) {
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

		if ( !skipJson ) {
			expect( serializeJSON( arr ) ).toBe( expectJson, label & " serializeJSON" );
		}
	}

	private void function _assertEmpty( required array arr ) {
		expect( arrayLen( arr ) ).toBe( 0 );
		expect( arrayIsEmpty( arr ) ).toBeTrue();
		expect( serializeJSON( arr ) ).toBe( "[]" );
	}

	private void function _mutateObject( required array arr, required any newFirst ) {
		arr[ 1 ] = newFirst;
		arrayAppend( arr, "extra" );
		expect( arrayLen( arr ) ).toBe( 4 );
		expect( arr[ 1 ] ).toBe( newFirst );
	}

	private void function _mutateNumeric( required array arr, required numeric newFirst ) {
		arr[ 1 ] = newFirst;
		arrayAppend( arr, 1000 );
		expect( arrayLen( arr ) ).toBe( 4 );
		expect( arr[ 1 ] ).toBe( newFirst );
	}

	private void function _assertAppendGrows( required array arr ) {
		arrayAppend( arr, 3 );
		arrayAppend( arr, 4 );
		expect( arrayLen( arr ) ).toBe( 4 );
		expect( arrayToList( arr ) ).toBe( "1,2,3,4" );
	}

	private void function _assertDuplicateIndependent( required array arr ) {
		var dup = duplicate( arr );
		arrayAppend( dup, "c" );
		dup[ 1 ] = "z";
		expect( arrayLen( arr ) ).toBe( 2 );
		expect( arr[ 1 ] ).toBe( "a" );
		expect( arrayLen( dup ) ).toBe( 3 );
		expect( dup[ 1 ] ).toBe( "z" );
	}

}
