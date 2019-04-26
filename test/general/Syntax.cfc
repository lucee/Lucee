component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){}

	function afterAll(){}

	function run( testResults , testBox ) {
		describe( "Syntax check", function() {

			it(title="compile all bundled CFML/CFC files in java", body=function(){
				
				admin action="updateMapping"
			        type="server"
			        password=request.SERVERADMINPASSWORD
			        virtual="/tmpAllSrc"
			        physical="#request.srcall#/java"
			        archive=""
			        primary="resource";

			    admin action="compileMapping"
					type="server"
					password=request.SERVERADMINPASSWORD
					virtual="/tmpAllSrc"
					stoponerror="false";

				
				//expect(data.bundle2.version).toBe("1.0.0.0");
			});


			it(title="compile all bundled CFML/CFC files in CFML", body=function(){
				
				admin action="updateMapping"
			        type="server"
			        password=request.SERVERADMINPASSWORD
			        virtual="/tmpAllSrc"
			        physical="#request.srcall#/cfml"
			        archive=""
			        primary="resource";

			    admin action="compileMapping"
					type="server"
					password=request.SERVERADMINPASSWORD
					virtual="/tmpAllSrc"
					stoponerror="false";

			});
		});
	}
}
