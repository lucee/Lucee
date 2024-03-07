component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function run( testResults, testBox ){

		describe( "LDEV-3116 thread scope cannot be modified from outside the owner thread", function(){
			it( title="with child thread", skip=true, body=function(){
				thread name="test" action="run" {
					[1].each(function(key){
						var test = key;
					}, true);

					thread.testing = 'blah';
				}
				thread action="join" name="test";
				expect( cfthread.test ).notToHaveKey( "error", cfthread.test.error.stacktrace?: '???' );
				expect( cfthread.test.testing ).toBe( "blah" );
			} );

			it( title="without child thread", body= function(){
				thread name="test_without" action="run" {
					[1].each(function(key){
						var test = key;
					}, false);

					thread.testing = 'blah';
				}
				thread action="join" name="test_without";
				expect( cfthread.test_without ).notToHaveKey( "error",  cfthread.test_without.error.stacktrace?: '???' );
				expect( cfthread.test_without.testing ).toBe( "blah" );
			} );

		} );
	}

}
