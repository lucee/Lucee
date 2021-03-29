component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test case for LDEV_1868", function() {
			it( title='Checking Application context for MailSettings', body=function( currentSpec ) {
				defineMailSettings(25, false, false);
				assertEquals(25, getApplicationSettings().mailservers[1].port);
				assertEquals(false, getApplicationSettings().mailservers[1].USETLS);
				assertEquals(false, getApplicationSettings().mailservers[1].USESSL);
				defineMailSettings(587, true, true);
				assertEquals(587, getApplicationSettings().mailservers[1].port);
				assertEquals(true, getApplicationSettings().mailservers[1].USETLS);
				assertEquals(true, getApplicationSettings().mailservers[1].USESSL);
			});
		});
	}

	private void function defineMailSettings(port, tls, ssl){
		application action="update"
		mailservers =[{
		  	server :"smtp.mail.com"
			, port: arguments.port
			, userName:"testing@mail.com"
			, password:"password"
			, useTLS:arguments.tls
			, useSSL:arguments.ssl
			, lifeTimespan: createTimeSpan(0,0,1,0)
			, idleTimespan: createTimeSpan(0,0,2,0)
		}];
	}
}