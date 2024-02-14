component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for isPDFObject() function", body=function() {
			it(title="Checking the isPDFObject() function", body=function( currentSpec ) {
				```
					<cfdocument name="test" format="pdf">
						<h1>Welcome to Lucee</h1>
					</cfdocument>
				```
				expect(isPDFObject(test)).toBeTrue();
				expect(isPDFObject(arrayNew(1))).toBeFalse();
				expect(isPDFObject(createObject('java','java.util.HashMap'))).toBeFalse();
			});
		});
	}
}