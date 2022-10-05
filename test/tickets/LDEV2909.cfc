component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" skip="true" {

	function beforeAll() {
		variables.xml = xmlparse('<?xml version="1.0" encoding="UTF-8"?>
		<note>
			<to>Tove</to>
			<from>Jani</from>
			<heading>Reminder</heading>
			<body>Do not forget me this weekend!</body>
		</note>');

		variables.schema = '<?xml version="1.0"?>
		<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">

		<xs:element name="note">
			<xs:complexType>
				<xs:sequence>
					<xs:element name="to" type="xs:string"/>
					<xs:element name="from" type="xs:string"/>
					<xs:element name="heading" type="xs:string"/>
					<xs:element name="body" type="xs:string"/>
				</xs:sequence>
			</xs:complexType>
		</xs:element>

		</xs:schema>';
	}
	
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-2909", function() {
			it(title="Checking xmlValidate()", body=function( currentSpec ) {
				local.result = xmlValidate(variables.xml,variables.schema);
				expect(result.errors).toBeEmpty();
				expect(result.fatalerrors).toBeEmpty();
				expect(result.warnings).toBeEmpty();
				expect(result.status).toBeTrue();
			});
		});
	}
}
