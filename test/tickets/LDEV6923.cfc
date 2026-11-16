component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6923: Error settings can be saved when a template is locked by a system property", function() {

			it( title="updateError should save statuscode when errorGeneralTemplate is locked and unchanged", body=function() {
				admin action="getError" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.err";
				var originalTemplate = err.str["500"];
				var original404 = err.str["404"];
				var originalStatus = err.doStatusCode;
				java.lang.System::setProperty( "lucee.errorGeneralTemplate", originalTemplate );
				try {
					var flipped = !originalStatus;
					admin
						action="updateError"
						type="server"
						password=request.SERVERADMINPASSWORD
						template500=originalTemplate
						template404=original404
						statuscode=flipped;
					admin action="getError" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.after";
					expect( after.doStatusCode ).toBe( flipped );
				}
				finally {
					java.lang.System::clearProperty( "lucee.errorGeneralTemplate" );
					try {
						admin
							action="updateError"
							type="server"
							password=request.SERVERADMINPASSWORD
							template500=originalTemplate
							template404=original404
							statuscode=originalStatus;
					}
					catch ( any e ) {}
				}
			});

		});
	}

}
