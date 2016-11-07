component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Getting caches from Application.cfc", function() {
			it('application.cfc',  function( currentSpec ) {
				uri=createURI("LDEV1046/index.cfm");
				local.result=_InternalRequest(
					template:uri
				);
				var data=evaluate(trim(result.filecontent));
				
				assertEquals('lucee.runtime.cache.ram.RamCache',data.cache.connections.test.class);
				assertEquals(false,data.cache.connections.test.storage);
				assertEquals(0,data.cache.connections.test.custom.timeToIdleSeconds);
			});
		});

		describe( "Getting caches from cfapplication", function() {
			it('application.cfm',  function( currentSpec ) {
				application action="update" caches={test1046:{
					class: 'lucee.runtime.cache.ram.RamCache'
					, storage: false
					, custom: {'timeToIdleSeconds':'0','timeToLiveSeconds':'0'}
					, default: ''
				}};

				var data=getApplicationMetadata();
				assertEquals('lucee.runtime.cache.ram.RamCache',data.cache.connections.test1046.class);
				assertEquals(false,data.cache.connections.test1046.storage);
				assertEquals(0,data.cache.connections.test1046.custom.timeToIdleSeconds);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}