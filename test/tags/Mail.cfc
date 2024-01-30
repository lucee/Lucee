component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {
	
	variables.port=30250;
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
			it(title="send a simple text mail", body = function( currentSpec ) {
				lock name="test:mail" {
					application.testSMTP.purgeEmailFromAllMailboxes();
					mail to=variables.to from=variables.from subject="simple text mail" spoolEnable=false server="localhost" port=variables.port {
						echo("This is a text email!");
					}

					var mail=application.testSMTP;

					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( 1 );
					var msg=messages[1];
					
					// from
					froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					tos=msg.getAllRecipients();
					expect( len(tos) ).toBe( 1 );
					expect( tos[1].getAddress() ).toBe( variables.to );
					
					// subject
					expect( msg.getSubject().toString() ).toBe( "simple text mail" );
					expect( msg. getContent() ).toBe( "This is a text email!" );
					expect( msg.getContentType() ).toBe( "text/plain; charset=UTF-8" );
					application.testSMTP.purgeEmailFromAllMailboxes();
				}
			});	

			it(title="send a simple html mail", body = function( currentSpec ) {
				
				lock name="test:mail" {
					application.testSMTP.purgeEmailFromAllMailboxes();
					mail type="html"  to=variables.to from=variables.from subject="simple html mail" spoolEnable=false server="localhost" port=variables.port {
						echo("This is a html email!");
					}

					var mail=application.testSMTP;

					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( 1 );
					var msg=messages[1];
					
					// from
					froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					tos=msg.getAllRecipients();
					expect( len(tos) ).toBe( 1 );
					expect( tos[1].getAddress() ).toBe( variables.to );
					
					// subject
					expect( msg.getSubject().toString() ).toBe( "simple html mail" );
					expect( msg. getContent() ).toBe( "This is a html email!" );
					expect( msg.getContentType()).toBe( "text/html; charset=UTF-8" );
					application.testSMTP.purgeEmailFromAllMailboxes();
				}
				
			});	

			it(title="send part text mail", body = function( currentSpec ) {
				
				lock name="test:mail" {
					application.testSMTP.purgeEmailFromAllMailboxes();
					mail to=variables.to from=variables.from subject="part text mail" spoolEnable=false server="localhost" port=variables.port {
						mailpart type="text" {
							echo("This is a text email!");
						}
					}

					var mail=application.testSMTP;

					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( 1 );
					var msg=messages[1];
					
					// from
					froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					tos=msg.getAllRecipients();
					expect( len(tos) ).toBe( 1 );
					expect( tos[1].getAddress() ).toBe( variables.to );
					
					// subject
					expect( msg.getSubject().toString() ).toBe( "part text mail" );
					expect( msg. getContent() ).toBe( "This is a text email!" );
					expect( msg.getContentType()).toBe( "text/plain; charset=UTF-8" );
					application.testSMTP.purgeEmailFromAllMailboxes();
				}
				
			});	

			it(title="send part html mail", body = function( currentSpec ) {
				
				lock name="test:mail" {
					application.testSMTP.purgeEmailFromAllMailboxes();
					mail to=variables.to from=variables.from subject="part html mail" spoolEnable=false server="localhost" port=variables.port {
						mailpart type="html" {
							echo("This is a html email!");
						}
					}

					var mail=application.testSMTP;

					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( 1 );
					var msg=messages[1];
					
					// from
					froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					tos=msg.getAllRecipients();
					expect( len(tos) ).toBe( 1 );
					expect( tos[1].getAddress() ).toBe( variables.to );
					
					// subject
					expect( msg.getSubject().toString() ).toBe( "part html mail" );
					expect( msg. getContent() ).toBe( "This is a html email!" );
					expect( msg.getContentType()).toBe( "text/html; charset=UTF-8" );
					application.testSMTP.purgeEmailFromAllMailboxes();
				}
				
			});	

			it(title="send muti part (html and text) mail", body = function( currentSpec ) {
				
				lock name="test:mail" {
					application.testSMTP.purgeEmailFromAllMailboxes();
					mail to=variables.to from=variables.from subject="multi part mail" spoolEnable=false server="localhost" port=variables.port {
						mailpart type="text" {
							echo("This is a text email!");
						}
						mailpart type="html" {
							echo("This is a html email!");
						}
					}

					var mail=application.testSMTP;

					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( 1 );
					var msg=messages[1];
					
					// from
					froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					tos=msg.getAllRecipients();
					expect( len(tos) ).toBe( 1 );
					expect( tos[1].getAddress() ).toBe( variables.to );
					
					// subject
					expect( msg.getSubject().toString() ).toBe( "multi part mail" );
					
					expect( left(msg.getContentType(),9) ).toBe( "multipart" );
					var parts=msg.getContent();
					expect( parts.getCount() ).toBe( 2 );
					expect( parts.getBodyPart(0).getContentType() ).toBe( "text/plain; charset=UTF-8" );
					expect( trim(parts.getBodyPart(0).getContent()) ).toBe( "This is a text email!" );
					expect( parts.getBodyPart(1).getContentType() ).toBe( "text/html; charset=UTF-8" );
					expect( trim(parts.getBodyPart(1).getContent()) ).toBe( "This is a html email!" );
					
					
					application.testSMTP.purgeEmailFromAllMailboxes();
				}
				
			});	

			

		});






	}
}