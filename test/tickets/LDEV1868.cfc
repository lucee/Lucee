component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-1868", function() {
			it( title='Checking Application context for MailSettings', body=function( currentSpec ) {
				defineMailSettings_mailservers(25, false, false);
				var settings  = getApplicationSettings();
				assertEquals(25, settings.mails[1].port);
				assertEquals(false, settings.mails[1].tls);
				assertEquals(false, settings.mails[1].ssl);
				
				defineMailSettings_mailservers(587, true, true);
				var settings  = getApplicationSettings();
				assertEquals(587, settings.mails[1].port);
				assertEquals(true, settings.mails[1].tls);
				assertEquals(true, settings.mails[1].ssl);
			});

			it( title='Checking Application context for Mail', body=function( currentSpec ) {
				defineMailSettings_mails(25, false, false);
				var settings  = getApplicationSettings();
				assertEquals(25, settings.mails[1].port);
				assertEquals(false, settings.mails[1].tls);
				assertEquals(false, settings.mails[1].ssl);

				defineMailSettings_mails(587, true, true);
				var settings  = getApplicationSettings();
				assertEquals(587, settings.mails[1].port);
				assertEquals(true, settings.mails[1].tls);
				assertEquals(true, settings.mails[1].ssl);
			});
		});
	}

	private void function defineMailSettings_mailservers(port, tls, ssl){
		application action="update"
			mailservers =[ {
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

	private void function defineMailSettings_mails(port, tls, ssl){
		application action="update"
			mails =[ {
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