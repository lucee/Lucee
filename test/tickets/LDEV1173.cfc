component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1173_1");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1173", function() {
			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as boolean", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe('{"ROWCOUNT":2,"COLUMNS":["FOO","BAR"],"DATA":{"FOO":[1,3],"BAR":[2,4]}}');
			});

			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as struct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);

				var arr=deserializeJSON(local.result.filecontent.trim());

				expect(arr[1].foo).toBe(1);
				expect(arr[1].bar).toBe(2);
				expect(arr[2].foo).toBe(3);
				expect(arr[2].bar).toBe(4);

			});
			
			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as row", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=3}
				);

				var qry=deserializeJSON(local.result.filecontent.trim(),false);

				expect(qry.recordcount).toBe(2);
				expect(qry.columnlist).toBe("foo,bar");
				expect(qry.foo[1]).toBe(1);
				expect(qry.bar[1]).toBe(2);
				expect(qry.foo[2]).toBe(3);
				expect(qry.bar[2]).toBe(4);

			});
			

			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as column", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=4}
				);

				var qry=deserializeJSON(local.result.filecontent.trim(),false);

				expect(qry.recordcount).toBe(2);
				expect(qry.columnlist).toBe("foo,bar");
				expect(qry.foo[1]).toBe(1);
				expect(qry.bar[1]).toBe(2);
				expect(qry.foo[2]).toBe(3);
				expect(qry.bar[2]).toBe(4);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
