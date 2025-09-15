component extends="org.lucee.cfml.test.LuceeTestCase" displayname="LDEV5792" {

	function run(testResults, textbox) {
		describe(title="LDEV-5792 Dynamic Custom Tag Attribute NPE (repo)", body=function(){
			it(title="function call as custom tag attribute throws error", body=function(currentSpec){
				var result = runExample("ldev5792_simple.cfm");
				// Expect the correct output after bug is fixed
				expect(result).toInclude("test-simple");
			});

			it(title="nested function call as custom tag attribute throws error", body=function(currentSpec){
				var result = runExample("ldev5792_nested_call.cfm");
				// Expect the correct output after bug is fixed
				expect(result).toInclude("inner-value");
			});

			it(title="nested custom tag inside attribute function call throws error", body=function(currentSpec){
				var result = runExample("ldev5792_nested_tag.cfm");
				// Expect the correct output after bug is fixed
				expect(result).toInclude("nested-tag-value");
			});
		});

		describe(title="LDEV-5792 Dynamic Custom Tag Attributes", body=function(){
			it(title="assigning function result to variable before custom tag attribute does not throw error", body=function(currentSpec){
				var result = runExample("ldev5792_safe_variable.cfm");
				expect(result).toInclude("test-safe");
			});

			it(title="static string as custom tag attribute works", body=function(currentSpec){
				var result = runExample("ldev5792_static_string.cfm");
				expect(result).toInclude("static string value");
			});

			it(title="variable as custom tag attribute works", body=function(currentSpec){
				var result = runExample("ldev5792_variable_only.cfm");
				expect(result).toInclude("variable value");
			});

			it(title="expression assigned to variable as custom tag attribute works", body=function(currentSpec){
				var result = runExample("ldev5792_expression_variable.cfm");
				expect(result).toInclude("foobar");
			});
		});
	}

	private string function runExample(string fileName) {
		var result = _InternalRequest(template: createURI('LDEV5792/' & fileName));
		return result.filecontent;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
