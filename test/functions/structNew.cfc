component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructNew()", function() {
			variables.myStruct = [ a:1,b=2,c=3 ];
			it(title="function structNew(ordered-casesensitive)", body=function( currentSpec ) {
				
				var sct=structNew("ordered-casesensitive");
				sct["a"]="a";
				sct["b"]="b";
				sct["c"]="c";
				sct["A"]="A";
				sct["B"]="B";
				
				assertEquals( sct.keyExists("c"),true);
				assertEquals( sct.keyExists("C"),false);
				assertEquals( sct.keyList(),"a,b,c,A,B");
			});
		});

		describe("testcase for StructNew('max:3')", function() {
			
			it(title="check structnew max", body=function( currentSpec ) {
				
				var sct=structNew("max:3");
				loop list="a,b,c,d,e,f" item="local.i" {
					sct[ i ] = true;
				}

				expect( structCount( sct ) ).toBe( 3 );
				expect( listSort ( structKeyList( sct ) ) ).toBe( "d,e,f" );

				var tmp = sct[ "d" ]; // being last accessed means it will not be purged
				sct [ "g" ] = true; // ths will remove 5 as it's the oldest / last accessed

				expect( structCount( sct ) ).toBe( 3 );
				expect( listSort (structKeyList( sct ) ) ).toBe( "d,f,g" ); // but is key order fixed?
				expect( structKeyList( sct ) ).toBe( "f,d,g" );  // i expected perhaps "d,f,g"

				tmp = sct [ "g" ];
				expect( structKeyList( sct ) ).toBe( "f,d,g" );  // i expected perhaps "g,d,f"
			});

		});
	}
}

