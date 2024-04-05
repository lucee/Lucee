component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	public function beforeAll(){
		variables.ts=getTimeZone();
	}
	
	public function afterAll(){
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4772", body=function() {
			it(title = "Checking evaluate() with datetime", body = function( currentSpec ) {
				
				// set old archive as mapping
				admin 
						action="updateComponentMapping"
						type="web"
						password=request.WEBADMINPASSWORD
						virtual="/test4772"
						primary="archive"
						physical=""
						archive=expandPath("../artifacts/lars/lucee-5.lar");
						
				var ss=new org.cfpoi.spreadsheet.Spreadsheet();
				
			});

		});
	}
} 