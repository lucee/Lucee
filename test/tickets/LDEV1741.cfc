component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm,cache,ehCache" {
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1741", body=function() {
			it( title='checking ORM secondary ehcache with this.ormsettings.cacheconfig = "ehcache.xml" ',body=function( currentSpec ) {
				var uri = createURI("LDEV1741");
				var result1 = _InternalRequest(
					template:"#uri#/App1/index.cfm",
					urls:{appName:"MyAppOne"}
				);

				var result2 = _InternalRequest(
					template:"#uri#/App1/index.cfm",
					urls:"appName=MyAppTwo"
				);

				assertEquals(200, result2.status_code);

				if( result2.status_code == 200  )
				assertEquals("Bar", result2.filecontent.trim());
			});

			it( title='checking ORM secondary ehcache without cacheconfig',body=function( currentSpec ) {
				var uri = createURI("LDEV1741");
				var result1 = _InternalRequest(
					template:"#uri#/App2/index.cfm",
					urls:{appName:"testOne"}
				);

				var result2 = _InternalRequest(
					template:"#uri#/App2/index.cfm",
					urls:{appName:"testTwo"}
				);

				assertEquals(200, result2.status_code);
				if( result2.status_code == 200  )
				assertEquals("Bar", result2.filecontent.trim());
			});
		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}