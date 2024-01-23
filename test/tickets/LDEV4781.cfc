component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4781", body=function() {
			it(title = "checking xmlSearch with lenient=true", body = function( currentSpec ) {

				```
				<cfxml variable="xml_document" lenient=true>
					<office>
						<employee>
							<emp_name>lucee_dev</emp_name>
							<emp_no>121</emp_no>
						</employee>
					</office>
				</cfxml>
				```

				expect( XmlSearch(xml_document, "/office/employee").isEmpty() ).toBeFalse();
				expect( arrayIndexExists(XmlSearch(xml_document, "/office/employee"), 1) ).toBeTrue();
			});
		});
	}

}