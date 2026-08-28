component extends="org.lucee.cfml.test.LuceeTestCase" {

	// returns the concrete java class name of a value, e.g. "java.lang.Double".
	// we check the real runtime type here on purpose: isNumeric()/isBoolean()/isDate()
	// only test whether a value *could* be converted, not what it actually is.
	private string function typeOf( any value ) {
		return getMetaData( arguments.value ).getName();
	}

	public void function testConvertTypesReturnsNewQuery() localmode="true" {
		qry = queryNew( "id,name,created,active,price", "integer,varchar,date,boolean,double", [
			[ "1", "Susi", "2026-01-15", "true", "9.5" ],
			[ "2", "Peter", "2026-08-28", "0", "12" ]
		] );

		res = queryConvertTypes( qry );

		// a new query is returned
		assertTrue( isQuery( res ) );

		// original query is untouched: the values are still the exact strings we put in
		assertEquals( "java.lang.String", typeOf( qry.id[ 1 ] ) );
		assertEquals( "java.lang.String", typeOf( qry.created[ 1 ] ) );
		assertEquals( "java.lang.String", typeOf( qry.active[ 1 ] ) );
		assertEquals( "java.lang.String", typeOf( qry.price[ 1 ] ) );

		// converted query has the concrete java types declared by the columns
		assertEquals( "java.lang.Double", typeOf( res.id[ 1 ] ) );
		assertEquals( "java.lang.Double", typeOf( res.id[ 2 ] ) );
		assertEquals( "lucee.runtime.type.dt.DateTimeImpl", typeOf( res.created[ 1 ] ) );
		assertEquals( "java.lang.Boolean", typeOf( res.active[ 1 ] ) );
		assertEquals( "java.lang.Boolean", typeOf( res.active[ 2 ] ) );
		assertEquals( "java.lang.Double", typeOf( res.price[ 1 ] ) );

		// string columns stay strings
		assertEquals( "java.lang.String", typeOf( res.name[ 1 ] ) );

		// values are still correct after conversion
		assertEquals( 1, res.id[ 1 ] );
		assertEquals( true, res.active[ 1 ] );
		assertEquals( false, res.active[ 2 ] );
		assertEquals( 9.5, res.price[ 1 ] );
		assertEquals( "Susi", res.name[ 1 ] );
	}

	public void function testConvertTypesInline() localmode="true" {
		qry = queryNew( "id,created", "integer,date", [
			[ "3", "2026-02-01" ]
		] );

		res = queryConvertTypes( qry, true );

		// inline modifies the given query in place and returns the same query
		assertEquals( "java.lang.Double", typeOf( qry.id[ 1 ] ) );
		assertEquals( "lucee.runtime.type.dt.DateTimeImpl", typeOf( qry.created[ 1 ] ) );
		assertEquals( 3, res.id[ 1 ] );
	}

	public void function testConvertTypesMemberFunction() localmode="true" {
		qry = queryNew( "amount", "double", [ [ "42.25" ] ] );
		res = qry.convertTypes();

		assertEquals( "java.lang.Double", typeOf( res.amount[ 1 ] ) );
		assertEquals( 42.25, res.amount[ 1 ] );
	}

	public void function testConvertTypesLeavesUnconvertibleUntouched() localmode="true" {
		// a value that cannot be cast to the declared type is kept as it is
		qry = queryNew( "n", "integer", [ [ "notANumber" ] ] );
		res = queryConvertTypes( qry );

		assertEquals( "java.lang.String", typeOf( res.n[ 1 ] ) );
		assertEquals( "notANumber", res.n[ 1 ] );
	}

	public void function testConvertTypesObjectColumnComplexValuesUntouched() localmode="true" {
		// columns with a type that has no matching CFML type (object/other) are left alone,
		// including complex values like structs, arrays and nested queries.
		// note: we can NOT use getMetaData().getName() here because getMetaData() on a
		// struct/array/query returns a metadata struct/array, not a java Class - so we
		// verify the preserved type with isStruct()/isArray()/isQuery() instead.
		qry = queryNew( "data", "object" );
		queryAddRow( qry, 3 );
		querySetCell( qry, "data", { foo: "bar" }, 1 );
		querySetCell( qry, "data", [ 1, 2, 3 ], 2 );
		querySetCell( qry, "data", queryNew( "x", "integer", [ [ 42 ] ] ), 3 );

		res = queryConvertTypes( qry );

		// struct preserved
		assertTrue( isStruct( res.data[ 1 ] ) );
		assertEquals( "bar", res.data[ 1 ].foo );

		// array preserved
		assertTrue( isArray( res.data[ 2 ] ) );
		assertEquals( 3, arrayLen( res.data[ 2 ] ) );
		assertEquals( 2, res.data[ 2 ][ 2 ] );

		// nested query preserved
		assertTrue( isQuery( res.data[ 3 ] ) );
		assertEquals( 42, res.data[ 3 ].x[ 1 ] );
	}

	public void function testConvertTypesComplexValueInNumericColumnUntouched() localmode="true" {
		// a complex value that cannot be cast to the declared (numeric) type must be
		// left as it is rather than throwing or being nulled.
		qry = queryNew( "n", "integer" );
		queryAddRow( qry );
		querySetCell( qry, "n", [ 1, 2 ], 1 );

		res = queryConvertTypes( qry );

		assertTrue( isArray( res.n[ 1 ] ) );
		assertEquals( 2, arrayLen( res.n[ 1 ] ) );
	}
}
