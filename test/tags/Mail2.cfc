component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail"
	javaSettings = '{
		maven: [
			"com.icegreen:greenmail:2.1.3"
		]
	}'{
	
	processingdirective pageencoding="UTF-8";

	import com.icegreen.greenmail.util.GreenMail;
	import com.icegreen.greenmail.util.ServerSetupTest;
	import com.icegreen.greenmail.util.GreenMailUtil;

	variables.from="susi@sorglos.de";
	variables.to="geisse@peter.ch";

	function beforeAll() {
		if ( !isNull( application.testSMTP ) ) {
			application.testSMTP.purgeEmailFromAllMailboxes();
			application.testSMTP.stop();
		}
		application.testSMTP = new GreenMail( ServerSetupTest::SMTP.dynamicPort() );
		application.testSMTP.start();
		variables.port = application.testSMTP.getSmtp().getServerSetup().port;
	}

	function afterAll() {
		if ( !isNull( application.testSMTP ) ) {
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
					var froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					var tos=msg.getAllRecipients();
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
					var froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					var tos=msg.getAllRecipients();
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
					var froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					var tos=msg.getAllRecipients();
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
					var froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					var tos=msg.getAllRecipients();
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
					var froms=msg.getFrom();
					expect( len(froms) ).toBe( 1 );
					expect( froms[1].getAddress() ).toBe( variables.from );
					
					// to
					var tos=msg.getAllRecipients();
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

			it(title="verify mail server", body = function( currentSpec ) {
				lock name="test:mail" {
					var SMTPVerifier=createObject("java","lucee.runtime.net.mail.SMTPVerifier");
        			expect( SMTPVerifier.verify("localhost", nullValue(), nullValue(), variables.port) ).toBeTrue();
				}
			});	


			it(title="send part with umlaut in file name ans subject", body = function( currentSpec ) {
			
			try{
				lock name="test:mail" {
					var mail=application.testSMTP;
					
					var subject="öäüéàè€";
					var filename="Das ist ein sehr langer sehr langer sehr langer sehr
					langer Filename mit ä Ä ü Ü ß und Ös und andere Leerzeichen.txt";
					if ( server.os.name contains "windows" )
						filename = ReReplace( filename,"\s", " ", "all" ); // Windows doesn't allow new lines etc in filenames
					var curr=getDirectoryFromPath(getCurrentTemplatePath());
					var file=curr&filename;
					fileWrite(file, subject);
					
					mail.purgeEmailFromAllMailboxes();
					mail to=variables.to from=variables.from subject=subject spoolEnable=false server="localhost" port=variables.port type="html" {
						echo(subject);
						mailparam file=file ;
					}

					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( 1 );

					var msg=messages[1];

					expect( msg.getSubject() ).toBe( subject);
					
					var content=msg.getContent();
					expect( content.getCount() ).toBe( 2 );

					// body
					var body=content.getBodyPart(0);
					expect( body.getContentType() ).toBe( "text/html; charset=UTF-8" );
					var content = body.getContent();
					loop from="1" to=len(content) index="local.c"{
						systemOutput("[" & asc(content[c]) & "] : []" & chr(content[c]) & "] : [" & content[c] & "]", true);
					}
					expect( body.getContent() ).toBe( subject );
					
					// attachment
					var attachment=content.getBodyPart(1);
					expect( attachment.getDisposition() ).toBe( "attachment" );
					expect( isNull(attachment.getContentID()) ).toBeTrue();
				
					// file name
					expect( attachment.getFileName() ).toBe( filename );

					application.testSMTP.purgeEmailFromAllMailboxes();
					
				}
			}
			finally {
				if(!isNull(file) && fileExists(file)) fileDelete(file);
			}
				
			});	

			it(title="send mail with message-id, case shouldn't matter LDEV-2473", body = function( currentSpec ) {
				
				lock name="test:mail" {
					application.testSMTP.purgeEmailFromAllMailboxes();
					var messageId = "test-#createUniqueID()#";
					var headerNames = [ "Message-ID", "Message-Id", "message-id", "MESSAGE-ID" ];
					arrayEach( headerNames, function( el, idx ) {
						mail to=variables.to from=variables.from subject="mail #idx# with #el#" 
								spoolEnable=false server="localhost" port=variables.port {
							mailparam name="#el#" value="<#messageId#-#el#>";
							mailpart type="html" {
								echo("This is a html email!");
							}
						}
					});

					var mail=application.testSMTP;
					var messages = mail.getReceivedMessages();
					expect( len(messages) ).toBe( len( headerNames ) );

					arrayEach( headerNames, function( el, idx ) {
						expect( messages[ idx ].getSubject() ).toBe( "mail #idx# with #el#");
						var msgHeaders = getMessageHeaders( messages[ idx ] );
						expect( msgHeaders).toHaveKey( "Message-Id" );
						expect( msgHeaders["Message-Id"] ).toBeWithCase( "<" & messageId & "-" & el & ">" );
					});

					application.testSMTP.purgeEmailFromAllMailboxes();
				}
			});
		});
	}

	private function getMessageHeaders( msg ){
		var str = GreenMailUtil::getHeaders( arguments.msg );
		var tmp = listToArray( str, chr( 10 ) );
		var headers = structNew( "ordered" );
		arrayEach( tmp, function( v ){
			headers[ listFirst( v, ":" ) ] = trim(listRest( v, ":" ));
		});
		return headers;
	}
}