
component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for getTagData()", function() {
			it(title="checking getTagData() function", body=function( currentSpec ) {
				var tagData = getTagData("cf","query")
				expect(tagData).toHaveKey("nameSpace");
				expect(tagData).toHaveKey("name");
				expect(tagData).toHaveKey("description");
				expect(tagData).toHaveKey("attributeCollection");
				expect(tagData).toHaveKey("attributes");
				expect(tagData.name).toBe("query");
				expect(tagData.type).toBe("java");
				expect(tagData.status).toBe("implemented");
				expect(tagData.attributes.name).toHaveKey("required");
			});
		});
	}
}