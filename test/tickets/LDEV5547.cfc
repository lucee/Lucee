component extends="org.lucee.cfml.test.LuceeTestCase" labels="component,property" skip=true {
	// skipped until LDEV-5547 is fixed: literal defaults of numeric/boolean properties are stored as strings

	function run( testResults, testBox ) {
		describe( "LDEV-5547 property default values are typed by the property type", function() {

			it( title="script literal defaults match the property type", body=function() {
				checkTyped( new LDEV5547.ScriptLiteral() );
			});

			it( title="script ##expression## defaults match the property type", body=function() {
				checkTyped( new LDEV5547.ScriptExpression() );
			});

			it( title="tag literal defaults match the property type", body=function() {
				checkTyped( new LDEV5547.TagLiteral() );
			});

			it( title="literal and ##expression## defaults produce the same types", body=function() {
				var lit = typesOf( new LDEV5547.ScriptLiteral() );
				var exp = typesOf( new LDEV5547.ScriptExpression() );
				expect( lit ).toBe( exp );
			});

			it( title="string and untyped defaults stay strings", body=function() {
				var o = new LDEV5547.ScriptLiteral();
				expect( className( o.getName() ) ).toBe( "java.lang.String" );
				expect( className( o.getUntyped() ) ).toBe( "java.lang.String" );
				expect( o.getUntyped() ).toBe( "789" );
			});
		});
	}

	private function checkTyped( o ) {
		expect( className( o.getId() ) ).notToBe( "java.lang.String", "numeric default=123" );
		expect( isNumeric( o.getId() ) ).toBeTrue();
		expect( o.getId() ).toBe( 123 );
		expect( className( o.getQuotedId() ) ).notToBe( "java.lang.String", "numeric default=""456""" );
		expect( o.getQuotedId() ).toBe( 456 );
		expect( className( o.getLuceeRocks() ) ).toBe( "java.lang.Boolean", "boolean default=true" );
		expect( o.getLuceeRocks() ).toBeTrue();
		expect( className( o.getQuotedFlag() ) ).toBe( "java.lang.Boolean", "boolean default=""false""" );
		expect( o.getQuotedFlag() ).toBeFalse();
		expect( className( o.getName() ) ).toBe( "java.lang.String" );
	}

	private struct function typesOf( o ) {
		return [
			id: className( o.getId() ),
			luceeRocks: className( o.getLuceeRocks() ),
			quotedId: className( o.getQuotedId() ),
			quotedFlag: className( o.getQuotedFlag() ),
			name: className( o.getName() )
		];
	}

	private string function className( val ) {
		return getMetadata( arguments.val ).getName();
	}
}
