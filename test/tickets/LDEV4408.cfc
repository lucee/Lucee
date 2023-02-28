component extends = "org.lucee.cfml.test.LuceeTestCase" labels="redis" {

	function beforeAll(){
		variables.uri = createURI("LDEV4408");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV4408", function(){
			it(title = "checking session variables after appplication action=update with redis",
					skip=isNotSupported(),
					body = function( currentSpec ){
				var arr = [].set( 1, 100, false );

				arr.each( function( el, idx, arr ){
					local.result = _InternalRequest(
						template : "#uri#/index.cfm"
					);
					local.res = deserializeJSON( result.filecontent );
					
					// nothing should change in the session after an <cfapplication action='update'>
					loop collection=res.before key="local.k" value="local.v" {
						expect( res.after ).toHaveKey( local.k );
						expect( res.after[ k ] ).toBe( res.before[ k ] );
					}

					expect( res.after ).toHaveKey( "trackingId" ); // added in onSessionStart
					arguments.arr[ arguments.idx ] = true;

					// sleep( randRange( 5, 20 ) );

				}, true, 4 );

				// systemOutput(arr);

				loop array=arr item="local.a" {
					expect( a ).toBeTrue();
				}

			});
		});
	}



	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function isNotSupported() {
		var res = server.getTestService( "redis" );
		return isNull(res) || len(res)==0;
	}
}