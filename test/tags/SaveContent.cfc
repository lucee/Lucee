component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Testcase for cfsavecontent", function() {

			it( title="trim=true trims the content", body=function( currentSpec ) {
				savecontent variable="local.c" trim=true {
					echo( " a " );
				}
				expect( c ).toBe( "a" );
			});

			it( title="trim=false keeps the whitespace", body=function( currentSpec ) {
				savecontent variable="local.c" trim=false {
					echo( " a " );
				}
				expect( c ).toBe( " a " );
			});

			it( title="trim from a previous call doesn't leak into the next, after the tag is reused", body=function( currentSpec ) {
				savecontent variable="local.before" {
					echo( " a " );
				}
				savecontent variable="local.trimmed" trim=true {
					echo( " a " );
				}
				savecontent variable="local.after" {
					echo( " a " );
				}
				savecontent variable="local.untrimmed" trim=false {
					echo( " a " );
				}
				savecontent variable="local.afterUntrimmed" {
					echo( " a " );
				}
				expect( trimmed ).toBe( "a" );
				expect( untrimmed ).toBe( " a " );
				expect( after ).toBe( before );
				expect( afterUntrimmed ).toBe( before );
			});
		});
	}
}
