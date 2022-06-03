component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {
	
	function beforeAll() {
		variables.adm = new Administrator("server",request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
		variables.defaultCharset = adm.getCharset();
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3224", function() {
			it(title="checking admin getCharset()", body=function( currentSpec ){
				variables.adm.updateCharset(templateCharset="ISO-8859-1",webCharset="ISO-8859-1",resourceCharset="ISO-8859-1");

				var charset = variables.adm.getCharset();

				expect(charset.webCharset).toBe("ISO-8859-1");
				expect(charset.templateCharset).toBe("ISO-8859-1");
				expect(charset.resourceCharset).toBe("ISO-8859-1");
			});
		});
	}

	function afterAll() {
		variables.adm.updateCharset(argumentCollection=variables.defaultCharset);
	}
	
}