component extends="org.lucee.cfml.test.LuceeTestCase"{

	variables.xml=xmlparse('<?xml version="1.0" encoding="UTF-8"?>
	<shiporder orderid="889923" 
	xmlns="http://webservice.example.com/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://webservice.example.com/ https://gist.githubusercontent.com/vikaskanani/016522bc8969008e5efdf6770effc430/raw/5eec95f8eb34c538f1cfd1e543b22027e021eafa/ShipOrder.xsd">
	<orderperson>John Smith</orderperson>
	<shipto>
		<name>Ola Nordmann</name>
		<address>Langgt 23</address>
		<city>4000 Stavanger</city>
		<country>Norway</country>
	</shipto>
	<item>
		<title>Empire Burlesque</title>
		<note>Special Edition</note>
		<quantity>1</quantity>
		<price>10.90</price>
	</item>
	<item>
		<title>Hide your heart</title>
		<quantity>1</quantity>
		<price>9.90</price>
	</item>
</shiporder>');

	variables.schema='<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
		targetNamespace=''http://webservice.example.com/''
		xmlns:tns=''http://webservice.example.com/''
		elementFormDefault="qualified">
	<xs:element name="shiporder" type=''tns:shiporder'' />
	<xs:complexType name="shiporder">
		<xs:sequence>
			<xs:element name="orderperson" type="xs:string"/>
			<xs:element name="shipto" type=''tns:shipto'' />
			<xs:element maxOccurs=''unbounded'' minOccurs=''0'' name="item" type=''tns:item'' />
		</xs:sequence>
		<xs:attribute name="orderid" type="xs:string" use="required"/>
	</xs:complexType>
	<xs:complexType name="shipto">
		<xs:sequence>
			<xs:element name="name" type="xs:string"/>
			<xs:element name="address" type="xs:string"/>
			<xs:element name="city" type="xs:string"/>
			<xs:element name="country" type="xs:string"/>
		</xs:sequence>
	</xs:complexType>
	<xs:complexType name="item">
		<xs:sequence>
			<xs:element name="title" type="xs:string"/>
			<xs:element name="note" type="xs:string" minOccurs="0"/>
			<xs:element name="quantity" type="xs:positiveInteger"/>
			<xs:element name="price" type="xs:decimal"/>
		</xs:sequence>
	</xs:complexType>
</xs:schema>';



	function run( testResults , testBox ) {
		describe( "test case for LDEV-2113", function() {
			it(title = "checkingXMLValidate (schema via Element in XML)", body = function( currentSpec ) {
				local.res=xmlValidate(variables.xml);
				expect(local.res.errors.len()).toBe(0);
				expect(local.res.fatalerrors.len()).toBe(0);
				expect(local.res.warnings.len()).toBe(0);
				expect(local.res.status).toBe(true);
			});
			it(title = "checkingXMLValidate (schema via URL)", body = function( currentSpec ) {
				local.urlSchema="https://gist.githubusercontent.com/vikaskanani/016522bc8969008e5efdf6770effc430/raw/5eec95f8eb34c538f1cfd1e543b22027e021eafa/ShipOrder.xsd";
				local.res=xmlValidate(variables.xml,local.urlSchema);
				expect(local.res.errors.len()).toBe(0);
				expect(local.res.fatalerrors.len()).toBe(0);
				expect(local.res.warnings.len()).toBe(0);
				expect(local.res.status).toBe(true);
			});
			it(title = "checkingXMLValidate (schema as string)", body = function( currentSpec ) {
				local.urlSchema="https://gist.githubusercontent.com/vikaskanani/016522bc8969008e5efdf6770effc430/raw/5eec95f8eb34c538f1cfd1e543b22027e021eafa/ShipOrder.xsd";
				local.res=xmlValidate(variables.xml,variables.schema);
				expect(local.res.errors.len()).toBe(0);
				expect(local.res.fatalerrors.len()).toBe(0);
				expect(local.res.warnings.len()).toBe(0);
				expect(local.res.status).toBe(true);
			});
		});
	}

}

