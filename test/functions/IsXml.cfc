component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for isXML() function", body=function() {
			it(title="Checking the isXML() function", body=function( currentSpec ) {
				Var xmlString = '<order id="1">
				<customer name="saravanan"/>
				<items>
				<item id="12">
				<quantity>2</quantity>
				<unitprice>12</unitprice>
				</item>
				</items>
				</order>'
				expect(isXML(XmlNew())).toBeTrue();
				expect(isXML(xmlString)).toBeTrue();
				expect(isXML("xmlString")).toBeFalse();
			});
		});
	}
}