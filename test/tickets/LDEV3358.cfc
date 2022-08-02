component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function beforeAll(){
		variables.uri = createURI("LDEV3358");
	}

	function run ( testResults , testBox ) {
		describe("This testcase for LDEV-3358",function(){
			
			it(title="Column with data type SMALLINT signed value max range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='id_signed',  value=32767 }
				);
				expect(trim(result.filecontent)).toBe(32767);
			});

			it(title="Column with data type SMALLINT signed value between the range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='id_signed', value=32222}
				);
				expect(trim(result.filecontent)).toBe(32222);
			});

			it(title="Column with data type SMALLINT signed value between the range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='id_signed', value=31000 }
				);
				expect(trim(result.filecontent)).toBe(31000);
			});
			
			it(title="Column with data type SMALLINT unsigned value between the range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='id_unsigned', value=32767}
					);
					expect(trim(result.filecontent)).toBe(32767);
				});

			it(title="Column with data type SMALLINT unsigned value between the range but higher than signed range without sqltype",body =function( currentSpec ){
					local.result = _InternalRequest(
						template :  "#uri#/test.cfm",
						forms = {scene=2, columnName='id_unsigned', value=32768 }
					);
					expect(trim(result.filecontent)).toBe(32768);
			});

			it(title="Column with data type SMALLINT unsigned value between the range but higher than signed range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='id_unsigned', value=60000 }
				);
				expect(trim(result.filecontent)).toBe(60000);
			});	

			it(title="Column with data type SMALLINT unsigned value between the range but higher than signed range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='id_unsigned', value=32768 }
				);
				expect(trim(result.filecontent)).toBe(32768);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}