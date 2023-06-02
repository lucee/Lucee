component extends = "org.lucee.cfml.test.LuceeTestCase" labels="redis" skip=true {

	function beforeAll(){
		variables.uri = createURI("LDEV4408");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV4408", function(){
			it(title = "checking session variables after appplication action=update with redis",
					skip=isNotSupported(),
					body = function( currentSpec ){
				var arr = [].set( 1, 10, false );

				arr.each( function( el, idx, arr ){
					local.result = _InternalRequest(
						template : "#uri#/index.cfm"
					);
					expect( result.filecontent ).toBeTrue( "error session variable missing - after #idx# attempts" );
				});

			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function isNotSupported() {
		var redis = server.getTestService( "redis" );
		return isEmpty(redis);
	}
}