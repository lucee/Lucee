component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function beforeAll(){
		variables.badFile = getTempFile(getTempDirectory(), "ldev1676" , "evil" );
		variables.badFileContent = "Sauron";
		fileWrite( badFile, variables.badFileContent );
		//systemOutput("XXE badfile: #badfile#", true);
		if ( find( "Windows", server.os.name ) > 0 )
			badfile = createObject("java","java.io.File").init( badfile ).toURI(); //escape it for xml, hello windows!
		//systemOutput("XXE badfile (uri): #badfile#", true);
		variables.doctypeXml = '<?xml version="1.0" encoding="ISO-8859-1"?>
			<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN" "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
			<hibernate-mapping></hibernate-mapping>';

		variables.entityXml = '<?xml version="1.0" encoding="ISO-8859-1"?>
			<!DOCTYPE foo [
			<!ELEMENT foo ANY >
				<cfoutput><!ENTITY xxe SYSTEM "#badfile#" ></cfoutput>
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

		describe( "testcase for LDEV-3110, xml features support for isXml", function () {

			it ( "isXml enabled doctype protections", function(){
				expect( isXml( doctypeXml ) ).toBeFalse();
			});

			it ( "isXml disabled doctype protections", function(){
				expect( isXml(doctypeXml, {
					"externalGeneralEntities": true,
					"secure": false,
					"disallowDoctypeDecl": false
				})).toBeTrue();
			});

			it ( "isXMl enabled XXE protections", function(){
				expect( isXml( entityXml ) ).toBeFalse();
			});

			it ( "isXml disabled XXE protections", function(){
				expect ( isXml( entityXml, {
					"externalGeneralEntities": true,
					"secure": false,
					"disallowDoctypeDecl": false
				})).toBeFalse();
			});

		});
		
		describe( "testcase for LDEV-3110, xml features support for adobe allowExternalEntities alias", function () {

			it ( "isXml conflicting Entities directives should fail", function(){
				expect ( function() {
					xmlParse( entityXml, false, {
						"externalGeneralEntities": true, // should be the same!
						"allowExternalEntities": false, // should be the same!
						"secure": false,
						"disallowDoctypeDecl": false
					});
				}).toThrow("java.lang.RuntimeException");
			});

			it ( "isXml enabled XXE protections, adobe syntax", function(){
				expect( isXml( entityXml, {
					"allowExternalEntities": true,
					"secure": false,
					"disallowDoctypeDecl": false
				})).toBeFalse();
			});

			it ( "isXml disabled XXE protections, adobe syntax", function(){
				expect(  isXml( entityXml, {
					"allowExternalEntities": false,
					"secure": false,
					"disallowDoctypeDecl": false
				})).toBeTrue()
			});
		});
		
	}

}
