component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Generic CFC interface contracts: implements + super dispatch through an interface
	// implementation, plus duplicate preservation. The implementing CFC's interface
	// metadata is reported on the declaring level (see test/functions/GetMetaData.cfc).

	function run( testResults, testBox ){
		describe( "CFC interface (implements + super)", function(){

			it( title="child overriding interface method calls super and returns combined result", body=function( currentSpec ){
				var c = new interface.IChild( tag="alpha" );
				expect( c.ifaceMethod() ).toBe( "ichild:alpha:ibase:alpha" );
			});
			it( title="duplicate of interface-implementing child preserves super dispatch", body=function( currentSpec ){
				var orig = new interface.IChild( tag="orig" );
				var dup = duplicate( orig );
				expect( dup.ifaceMethod() ).toBe( "ichild:orig:ibase:orig" );
				dup.tag = "dup";
				expect( dup.ifaceMethod() ).toBe( "ichild:dup:ibase:dup" );
				expect( orig.ifaceMethod() ).toBe( "ichild:orig:ibase:orig" );
			});

		});
	}
}
