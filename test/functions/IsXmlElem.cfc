component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for isXmlElem() function", body = function() {
			it( title = "Checking isXmlElem() function", body = function( currentSpec ) {
				```
				<cfxml variable="xmlobject">
					<office>
						<employee>
							<emp_name>lucee_dev</emp_name>
							<emp_no>121</emp_no>
						</employee>
					</office>
				</cfxml>
				```
				expect(IsXmlElem(xmlobject.office)).toBeTrue();
			});
		});
	}
}

