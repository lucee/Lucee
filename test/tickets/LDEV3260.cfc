component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-3260 onMissingProperty", function() {

			it( title="dot notation read of a missing key calls onMissingProperty", body=function( currentSpec ) {
				var obj = new LDEV3260.Basic();
				expect( obj.foo ).toBe( "missing:foo" );
			} );

			it( title="bracket notation read of a missing key calls onMissingProperty", body=function( currentSpec ) {
				var obj = new LDEV3260.Basic();
				expect( obj[ "foo" ] ).toBe( "missing:foo" );
				var name = "dynamic";
				expect( obj[ name ] ).toBe( "missing:dynamic" );
			} );

			it( title="the name is passed as written", body=function( currentSpec ) {
				var obj = new LDEV3260.Basic();
				expect( obj[ "SomeThing" ] ).toBe( "missing:SomeThing" );
			} );

			it( title="an existing key is returned, onMissingProperty is not called", body=function( currentSpec ) {
				var obj = new LDEV3260.Basic();
				expect( obj.existing ).toBe( "present" );
			} );

			it( title="the value can be used in an expression", body=function( currentSpec ) {
				var obj = new LDEV3260.Basic();
				expect( len( obj.foo ) ).toBe( 11 );
				expect( obj.foo & "!" ).toBe( "missing:foo!" );
			} );

			it( title="onMissingProperty can return null", body=function( currentSpec ) {
				var obj = new LDEV3260.NullHandler();
				expect( isNull( obj.foo ) ).toBeTrue();
			} );

			it( title="onMissingProperty of the parent component is used", body=function( currentSpec ) {
				var obj = new LDEV3260.Child();
				expect( obj.foo ).toBe( "missing:foo" );
				expect( obj.own ).toBe( "child" );
				expect( obj.existing ).toBe( "present" );
			} );

			it( title="a child can override onMissingProperty", body=function( currentSpec ) {
				var obj = new LDEV3260.Override();
				expect( obj.foo ).toBe( "override:foo" );
			} );

			it( title="implicit accessors of properties still work", body=function( currentSpec ) {
				var obj = new LDEV3260.Accessors();
				expect( obj.getTitle() ).toBe( "the-title" );
				obj.setTitle( "changed" );
				expect( obj.getTitle() ).toBe( "changed" );
				expect( obj.other ).toBe( "missing:other" );
			} );

			it( title="onMissingMethod is not used for reads and onMissingProperty is not used for calls", body=function( currentSpec ) {
				var obj = new LDEV3260.Both();
				expect( obj.foo ).toBe( "property:foo" );
				expect( obj.foo() ).toBe( "method:foo" );
			} );

			it( title="onMissingMethod alone does not handle a read", body=function( currentSpec ) {
				var obj = new LDEV3260.OnlyMethod();
				expect( function() {
					var x = obj.foo;
				} ).toThrow( message="has no accessible Member with name [foo]" );
				expect( obj.foo() ).toBe( "method:foo" );
			} );

			it( title="a private onMissingProperty is not used, like onMissingMethod", body=function( currentSpec ) {
				var obj = new LDEV3260.Private();
				expect( function() {
					var x = obj.foo;
				} ).toThrow( message="has no accessible Member with name [foo]" );
			} );

			it( title="getters are used before onMissingProperty when trigger data member is enabled", body=function( currentSpec ) {
				var result = deserializeJSON( _internalRequest( template: "#createURI( 'LDEV3260' )#/trigger.cfm" ).filecontent );
				expect( result.getter ).toBe( "getter-foo" );
				expect( result.missing ).toBe( "missing:bar" );
				expect( result.noHandler ).toInclude( "has no accessible Member with name [bar]" );
			} );

			describe( "not triggered", function() {

				it( title="structKeyExists", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( structKeyExists( obj, "foo" ) ).toBeFalse();
					expect( structKeyExists( obj, "existing" ) ).toBeTrue();
				} );

				it( title="isDefined", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( isDefined( "obj.foo" ) ).toBeFalse();
					expect( isDefined( "obj.existing" ) ).toBeTrue();
				} );

				it( title="isNull", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( isNull( obj.foo ) ).toBeTrue();
				} );

				it( title="the elvis operator", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( obj.foo ?: "fallback" ).toBe( "fallback" );
				} );

				it( title="structGet with a default via struct access functions", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( structKeyArray( obj ) ).notToInclude( "foo" );
				} );

			} );

			describe( "no handler defined", function() {

				it( title="the error for a missing key is unchanged", body=function( currentSpec ) {
					var obj = new LDEV3260.None();
					expect( function() {
						var x = obj.foo;
					} ).toThrow( message="has no accessible Member with name [foo]" );
					expect( function() {
						var x = obj[ "foo" ];
					} ).toThrow( message="has no accessible Member with name [foo]" );
				} );

				it( title="existing members are returned", body=function( currentSpec ) {
					var obj = new LDEV3260.None();
					expect( obj.existing ).toBe( "present" );
				} );

			} );

			describe( "inside the component", function() {

				it( title="a missing key in the variables scope is not handled", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( function() {
						obj.readVariables( "foo" );
					} ).toThrow();
					expect( obj.readVariables( "hidden" ) ).toBe( "in-variables" );
				} );

				it( title="a missing key in the this scope is not handled", body=function( currentSpec ) {
					var obj = new LDEV3260.Basic();
					expect( function() {
						obj.readThis( "foo" );
					} ).toThrow( message="has no accessible Member with name [foo]" );
					expect( obj.readThis( "existing" ) ).toBe( "present" );
				} );

			} );

			describe( "recursion", function() {

				it( title="reading a missing key inside onMissingProperty throws the normal error", body=function( currentSpec ) {
					var obj = new LDEV3260.Recursion();
					expect( obj.plain ).toBe( "ok:plain" );
					expect( function() {
						var x = obj.viaThis;
					} ).toThrow( message="has no accessible Member with name [neverDefined]" );
					expect( function() {
						var x = obj.viaSelf;
					} ).toThrow( message="has no accessible Member with name [neverDefined]" );
					expect( function() {
						var x = obj.viaBracket;
					} ).toThrow( message="has no accessible Member with name [neverDefined]" );
					expect( function() {
						var x = obj.viaOther;
					} ).toThrow( message="has no accessible Member with name [neverDefined]" );
				} );

				it( title="the handler is called once per read", body=function( currentSpec ) {
					var obj = new LDEV3260.Recursion();
					var before = obj.getCalls();
					var x = obj.once;
					expect( obj.getCalls() ).toBe( before + 1 );
				} );

			} );

		} );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
