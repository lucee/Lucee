component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function beforeAll(){
		variables.doctypeXml = '<?xml version="1.0" encoding="ISO-8859-1"?>
			<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN" "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
			<hibernate-mapping></hibernate-mapping>';

		variables.entityXml = '<?xml version="1.0" encoding="ISO-8859-1"?>
			<!DOCTYPE foo [
			<!ELEMENT foo ANY >
				<!ENTITY xxe SYSTEM "http://update.lucee.org/rest/update/provider/echoGet/cgi" >
			]>
			<foo>&xxe;</foo>'; // that url 404s

		application action="update" xmlFeatures={
			"secure": true,
			"disallowDoctypeDecl": true,
			"externalGeneralEntities": false
		};
	}	

	function afterAll() {
		application action="update" xmlFeatures={
			"secure": true,
			"disallowDoctypeDecl": true,
			"externalGeneralEntities": false
		};
	}

	function run( testresults , testbox ) {
		describe( "testcase for LDEV-3110, xml features support for xmlparse", function () {

			it ( "xmlparse enabled doctype protections", function(){
				expect( function(){
					xmlParse( doctypeXml );
				}).toThrow();
			});

			it ( "xmlparse disabled doctype protections", function(){
				expect( function(){
					xmlParse(doctypeXml, false, {
						"externalGeneralEntities": true,
						"secure": false,
						"disallowDoctypeDecl": false
					})
				}).notToThrow();
			});

			it ( "xmlparse enabled XXE protections", function(){
				expect( function(){
					xmlParse( entityXml );
				}).toThrow();
			});

			it ( "xmlparse disabled XXE protections", function(){
				expect (function(){
					xmlParse( entityXml, false, {
						"externalGeneralEntities": true,
						"secure": false,
						"disallowDoctypeDecl": false
					})
				}).ToThrow("java.io.FileNotFoundException"); // as http://update.lucee.org/rest/update/provider/echoGet/cgi 404s
			});

		});
	}

}
