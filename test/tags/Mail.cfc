component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3" {
	
	variables.port=3025;
	variables.from="susi@sorglos.de";
	variables.to="geisse@peter.ch";


	function beforeAll() {
		if(isNull(application.testSMTP)) {
			var ServerSetup=createObject("java","com.icegreen.greenmail.util.ServerSetup","org.lucee.greenmail","1.6.15");
			var GreenMail=createObject("java","com.icegreen.greenmail.util.GreenMail","org.lucee.greenmail","1.6.15");
			application.testSMTP = GreenMail.init(ServerSetup.init(variables.port, nullValue(), ServerSetup.PROTOCOL_SMTP));
			application.testSMTP.start();
		}
		else {
			application.testSMTP.purgeEmailFromAllMailboxes();
		}


    }

    function afterAll() {
        if(!isNull(application.testSMTP)) {
			application.testSMTP.purgeEmailFromAllMailboxes();
			application.testSMTP.stop();
		}
    }
	
	
	
	function run( testResults , testBox ) {
		describe( title="Test suite for the tag cfmail", body=function() {
			it(title="send a simple text mail",skip=Util::isBackBlazeNotSupported(), body = function( currentSpec ) {
				
				mail to=variables.to from=variables.from subject="simple text mail" spoolEnable=false server="localhost" port=variables.port {
					echo("This is a text email!");
				}

				var messages = mail.getReceivedMessages();
				expect( len(messages) ).toBe( 1 );
				
				var msg=messages[1];
				
				// from
				froms=msg.getFrom();
				expect( len(froms) ).toBe( 1 );
				from=froms[1];
				expect( from ).toBe( variables.from );
				
				// to
				tos=msg.getAllRecipients();
				expect( len(tos) ).toBe( 1 );
				to=tos[1];
				expect( to.toString() ).toBe( variables.to );
				
				// subject
				expect( msg.getSubject() ).toBe( "simple text mail" );
					
			});	
			
	
		});
	}
}