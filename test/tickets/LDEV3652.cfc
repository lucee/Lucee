component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-3652 with triggerDataMember", function() {
			/*****************************************************************************
				Application.cfc has this.triggerDataMember = true;
			*****************************************************************************/
			it(title="Testing SerializeJSON() with a standard component with accessors", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testComponent.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","age":20,"birthDate":"1990-11-20","isCurrent":false}');
			});
			
			it(title="Testing SerializeJSON() with a component that has a property with remotingFetch set to false", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testRemotingFetch.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name"}');
			});

			it(title="Testing SerializeJSON() with a component that has a property with getter set to false", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testFalseGetter.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name"}');
			});

			it(title="Testing SerializeJSON() with a component that has a property with custom getter function", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testCustomGetter.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","password":"Not Available"}');
			});

			it(title="Testing SerializeJSON() with a component that has a property with getter set to false but with a custom getter function", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testFalseGetterCustomGetter.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","password":"Not Available"}');
			});

			it(title="Testing SerializeJSON() with a struct", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testStruct.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"PROP":"prop"}');
			});

			it(title="Testing SerializeJSON() with an array", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testArray.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('["prop",20,true]');
			});

			it(title="Testing SerializeJSON() with an stuct with a nested struct", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_1/testNestedStruct.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"NESTED":{"PROP":"nestedProp"}}');
			});
		});

		describe( "Test suite for LDEV-3652 without triggerDataMember", function() {
			/*****************************************************************************
				No Application.cfc
			*****************************************************************************/
			it(title="Testing SerializeJSON() with a standard component with accessors", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testComponent.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","age":20,"birthDate":"1990-11-20","isCurrent":false}');
			});
			
			it(title="Testing SerializeJSON() with a component that has a property with remotingFetch set to false", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testRemotingFetch.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name"}');
			});

			it(title="Testing SerializeJSON() with a component that has a property with getter set to false", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testFalseGetter.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","password":"testPassword1234"}');
			});

			it(title="Testing SerializeJSON() with a component that has a property with custom getter function", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testCustomGetter.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","password":"testPassword1234"}');
			});

			it(title="Testing SerializeJSON() with a component that has a property with getter set to false but with a custom getter function", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testFalseGetterCustomGetter.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"name":"Test Name","password":"testPassword1234"}');
			});

			it(title="Testing SerializeJSON() with a struct", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testStruct.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"PROP":"prop"}');
			});

			it(title="Testing SerializeJSON() with an array", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testArray.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('["prop",20,true]');
			});

			it(title="Testing SerializeJSON() with an stuct with a nested struct", body = function( currentSpec ) {
				var uri=createURI("LDEV3652_2/testNestedStruct.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"NESTED":{"PROP":"nestedProp"}}');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
