component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	public function run( testResults , testBox ) {
		describe( title="Testing date functions & its equivalent member functions for ORM entity's date", body=function() {
			it(title="Testing dateDiff function", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				// Dummy request
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene:1
					}
				);

				assertEquals("", local.result.fileContent.trim());
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 1,
						Purpose: "dateDiff"
					}
				);
				assertEquals("4", local.result.fileContent.trim());
			});

			it(title="Testing dateDiff's equivalent member function", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 1,
						Purpose: "dateDiffMember"
					}
				);
				assertEquals("4", local.result.fileContent.trim());
			});

			it(title="Testing dateDiff function with formatted date", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 2,
						Purpose: "dateDiff"
					}
				);
				assertEquals("4", local.result.fileContent.trim());
			});

			it(title="Testing dateDiff's equivalent member function function with formatted date", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 2,
						Purpose: "dateDiffMember"
					}
				);
				assertEquals("4", local.result.fileContent.trim());
			});

			it(title="Testing dateCompare function", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 1,
						Purpose: "dateCompare"
					}
				);
				assertEquals("-1", local.result.fileContent.trim());
			});

			it(title="Testing dateCompare's equivalent member function", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 1,
						Purpose: "dateCompareMember"
					}
				);
				assertEquals("-1", local.result.fileContent.trim());
			});

			it(title="Testing dateCompare function with formatted date", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 2,
						Purpose: "dateCompare"
					}
				);
				assertEquals("-1", local.result.fileContent.trim());
			});

			it(title="Testing dateCompare's equivalent member function with formatted date", body=function( currentSpec ) {
				uri = createURI("LDEV0374/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene: 2,
						Purpose: "dateCompareMember"
					}
				);
				assertEquals("-1", local.result.fileContent.trim());
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}