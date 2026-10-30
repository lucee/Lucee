component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfparam" {

	function beforeAll() {
		variables.uri = createURI( "LDEV2938" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2938 cfparam attribute set (allowed values)", function() {

			it( "script: accepts a value of a list", function() {
				var rating = "medium";
				param name="rating" set="low,medium,high";
				expect( rating ).toBe( "medium" );
			});

			it( "script: accepts a value of an array", function() {
				var rating = "high";
				param name="rating" set=[ "low", "medium", "high" ];
				expect( rating ).toBe( "high" );
			});

			it( "script: spaces around the items of a list are ignored", function() {
				var rating = "medium";
				param name="rating" set="low , medium , high";
				expect( rating ).toBe( "medium" );
			});

			it( "script: throws an error naming the allowed values", function() {
				var rating = "extreme";
				var msg = "";
				try {
					param name="rating" set=[ "low", "medium", "high" ];
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).notToBe( "" );
				expect( msg ).toInclude( "extreme" );
				expect( msg ).toInclude( "rating" );
				expect( msg ).toInclude( "low, medium, high" );
			});

			it( "script: throws for a list as well", function() {
				var rating = "extreme";
				expect( function() {
					param name="rating" set="low,medium,high";
				}).toThrow( "expression" );
			});

			it( "script: the comparison is case sensitive", function() {
				var rating = "High";
				expect( function() {
					param name="rating" set="low,medium,high";
				}).toThrow();
			});

			it( "script: an empty value is not valid, unless it is part of the array", function() {
				var rating = "";
				expect( function() {
					param name="rating" set="low,medium,high";
				}).toThrow();
				param name="rating" set=[ "", "low" ];
				expect( rating ).toBe( "" );
			});

			it( "script: the default is used when the variable does not exist and it is checked as well", function() {
				param name="local.mood" default="low" set="low,medium,high";
				expect( local.mood ).toBe( "low" );
				expect( function() {
					param name="local.mood2" default="wrong" set="low,medium,high";
				}).toThrow();
			});

			it( "script: set can be combined with type", function() {
				var level = 2;
				param name="level" type="integer" set="1,2,3";
				expect( level ).toBe( 2 );

				var level = 5;
				expect( function() {
					param name="level" type="integer" set="1,2,3";
				}).toThrow();

				// the type is checked first
				var level = "abc";
				var msg = "";
				try {
					param name="level" type="integer" set="1,2,3";
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "not a valid integer" );
			});

			it( "script: numbers are compared as strings", function() {
				var level = 3;
				param name="level" set=[ 1, 2, 3 ];
				expect( level ).toBe( 3 );
			});

			it( "script: an empty set and a non simple value are errors", function() {
				var rating = "a";
				expect( function() {
					param name="rating" set=[];
				}).toThrow();
				var rating = [ "a" ];
				expect( function() {
					param name="rating" set="a,b";
				}).toThrow();
				var rating = "a";
				expect( function() {
					param name="rating" set={ a: 1 };
				}).toThrow();
			});

			it( "without set nothing changes", function() {
				var rating = "anything";
				param name="rating" default="x";
				param name="rating" type="string";
				expect( rating ).toBe( "anything" );
			});

			it( "tag: list", function() {
				expect( trim( _InternalRequest( template: uri & "/tag.cfm", url: { mode: "list", rating: "high" } ).fileContent ) ).toBe( "high" );
				expect( trim( _InternalRequest( template: uri & "/tag.cfm", url: { mode: "list" } ).fileContent ) ).toBe( "low" );
				expect( function() {
					_InternalRequest( template: uri & "/tag.cfm", url: { mode: "list", rating: "extreme" } );
				}).toThrow();
			});

			it( "tag: array", function() {
				expect( trim( _InternalRequest( template: uri & "/tag.cfm", url: { mode: "array", rating: "medium" } ).fileContent ) ).toBe( "medium" );
				expect( function() {
					_InternalRequest( template: uri & "/tag.cfm", url: { mode: "array", rating: "Medium" } );
				}).toThrow();
			});

			it( "tag: the default is checked", function() {
				expect( function() {
					_InternalRequest( template: uri & "/tag.cfm", url: { mode: "default" } );
				}).toThrow();
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
