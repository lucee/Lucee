component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,customtag" {

	function beforeAll() {
		application action="update" customtagPaths=[ getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV3722" ];
	}

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3722", function() {

			it( title="a custom tag called from a thread can write to the thread scope with caller.thread", body=function( currentSpec ) {
				thread name="LDEV_3722_a" {
					thread.before = "before worked";
					cf_myTag();
					thread.after = "after worked";
				}
				thread action="join" name="LDEV_3722_a";

				var t = cfthread.LDEV_3722_a;
				expect( structKeyExists( t, "before" ) ).toBeTrue( "before" );
				expect( structKeyExists( t, "setFromCustomTag" ) ).toBeTrue( "set by the custom tag" );
				expect( structKeyExists( t, "after" ) ).toBeTrue( "after (the thread scope was moved into the variables scope)" );
			});

			it( title="the value set with caller.thread by a custom tag is the value in the thread scope", body=function( currentSpec ) {
				thread name="LDEV_3722_b" {
					cf_myTag();
				}
				thread action="join" name="LDEV_3722_b";

				expect( cfthread.LDEV_3722_b ).toHaveKey( "setFromCustomTag" );
				expect( cfthread.LDEV_3722_b.setFromCustomTag ).toBe( "hey there" );
			});
		});
	}
}
