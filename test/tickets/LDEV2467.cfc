component extends="org.lucee.cfml.test.LuceeTestCase" labels="parser" {

	function run( testResults, testBox ) {
		describe( "LDEV-2467 modulus operator followed by a division or multiplication", function() {

			// the modulus operator has a lower precedence than "*", "/" and "\", so "a % b / c" is "a % ( b / c )"

			it( title="tag cfset: % and mod followed by /, * and \", body=function( currentSpec ) {
				include "LDEV2467/tag.cfm";
				var r = request.ldev2467;
				expect( r.modDiv ).toBe( 1 );
				expect( r.modDivOrig ).toBe( 0 );
				expect( r.modMulti ).toBe( 2 );
				expect( r.modIntDiv ).toBe( 1 );
				expect( r.keywordDiv ).toBe( 1 );
				expect( r.keywordMulti ).toBe( 2 );
			});

			it( title="tag cfset: / and * before %, parentheses and chained %", body=function( currentSpec ) {
				include "LDEV2467/tag.cfm";
				var r = request.ldev2467;
				expect( r.divMod ).toBe( 2 );
				expect( r.multiMod ).toBe( 3 );
				expect( r.parens ).toBe( 0.5 );
				expect( r.chained ).toBe( 3 );
			});

			it( title="script: % and mod followed by / and *", body=function( currentSpec ) {
				include "LDEV2467/script.cfm";
				var r = request.ldev2467;
				expect( r.modDiv ).toBe( 1 );
				expect( r.modMulti ).toBe( 2 );
				expect( r.keywordDiv ).toBe( 1 );
			});

			it( title="script: %= still works", body=function( currentSpec ) {
				include "LDEV2467/script.cfm";
				expect( request.ldev2467.assign ).toBe( 2 );
			});

		});
	}

}
