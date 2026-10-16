/*
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" skip=true {

	variables.from="susi@sorglos.de";
	variables.to="geisse@peter.ch";

	public function beforeTests(){
		variables.creds = getCredentials();

		if (structCount(creds.smtp) == 0 || structCount(creds.pop) == 0) return;

		admin 
			action="updateMailServer"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			hostname="#creds.smtp.SERVER#"
			dbusername="#creds.smtp.USERNAME#"
			dbpassword=""
			life="1"
			idle="1"
			port="#creds.smtp.PORT_INSECURE#"
			id="123"
			tls="true"
			ssl="false"
			reuseConnection=false;
	}

	public function afterTests(){
		// skip clean up when no server configured
		if (structCount(creds.smtp) == 0 || structCount(creds.pop) == 0) return;

		admin 
			action="removeMailServer"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			hostname="#creds.smtp.SERVER#";
	}

	function teardown( currentMethod ){
		createObject("java", "java.lang.System").clearProperty("lucee.mail.use.7bit.transfer.encoding.for.html.parts");
	}

	private function getCredentials() {
		return {
			smtp : server.getTestService("smtp"),
			pop : server.getTestService("pop")
		}
	}

	public boolean function notHasServices() {
		return structCount(server.getTestService("smtp")) == 0 || structCount(server.getTestService("pop")) == 0;
	}

	private struct function getMails() {
		pop action="getAll" name="local.inboxemails" server="#creds.pop.server#" password="#creds.pop.password#" port="#creds.pop.PORT_INSECURE#" secure="no" username="#variables.to#";
	
		sct = queryGetRow(inboxemails, queryRecordCount(inboxemails));
	
		// delete inbox mails after getting mail for the test So the inbox always have one mail and getting mail will take less time
		removeAllMessages();
	
		sct.header = headerToStruct(sct.header);
		return sct;
	}
	
	private void function removeAllMessages() {
		pop
			action="delete"
			server="#creds.pop.server#"
			port="#creds.pop.PORT_INSECURE#"
			secure="no"
			username="#variables.to#"
			password="#variables.to#"
		;
	}
	
	// converts the header string to struct
	private struct function headerToStruct(Required string header) localmode=true {
		var results = {};
		var headerStream = createObject("java", "java.io.ByteArrayInputStream").init(javaCast("string", arguments.header).getBytes("UTF-8"));
		var InternetHeaders = createObject("java", "javax.mail.internet.InternetHeaders").init(headerStream);
		var headers = InternetHeaders.getAllHeaders();

		while( headers.hasMoreElements() ){
			var headerEl = headers.nextElement();
			// store the key/value pair
			results[headerEl.getName()] = headerEl.getValue();
		}

		return results;
	}

	/*
		For some tests, we need the raw message so we can inspect that the message
		format was generated as expected.
	*/
	private any function getMailAsEML(){
		var results = [];
		var host = "#creds.pop.server#";
		var user = "#variables.to#";
		var password = "#variables.to#";

		var properties = createObject("java", "java.lang.System").getProperties();
		var mSession = createObject("java", "javax.mail.Session").getDefaultInstance(properties);
		var store = mSession.getStore("pop3");

		store.connect(host, javaCast("int", creds.pop.PORT_INSECURE), user, password);

		try {
			var folder = store.getFolder("inbox");
			var mFolder = createObject("java", "javax.mail.Folder");
			folder.open(mFolder.READ_ONLY);

			var messages = folder.getMessages();

			for( var message in messages ){
				var messageDetails = {
						"headers" = {}
					, "content" = ""
				};

				// get the headers
				var headers = message.getAllHeaders();

				while( headers.hasMoreElements() ){
					var header = headers.nextElement();
					messageDetails.headers[header.getName()] = header.getValue();
				}

				// get the full body
				var baos = createObject("java", "java.io.ByteArrayOutputStream").init();

				// write the message to the output stream
				try {
					message.writeTo(baos);
					// convert the output stream to a string
					messageDetails.content = baos.toString();
				} finally {
					// close the output stream
					baos.close();
				}

				results.append(messageDetails);
			}
		} finally {
			folder.close(true);
			store.close();
		}

		// delete inbox mails after getting mail for the test So the inbox always have one mail and getting mail will take less time
		removeAllMessages();

		return results;
	}

	private any function getFirstMailAsEML(){
		var results = getMailAsEML();

		return results.len() ? results[1] : "";
	}




	// tests
	public function testSimpleMail() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a text email!");
		}
		
		var mails=getMails();

		assertEquals("test mail1",mails.subject);
		assertEquals("This is a text email!",mails["body"].trim());
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
		assertEquals("text/plain; charset=UTF-8",mails.header["content-type"]);
	}


	public function testHTMLMail() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail type="html" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a HTML email!");
		}

		var mails=getMails();

		assertEquals("text/html; charset=UTF-8",mails.header["content-type"]);
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
	}

	public function testTextMail() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail type="plain" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a text email!");
		}

		var mails=getMails();

		assertEquals("text/plain; charset=UTF-8",mails.header["content-type"]);
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
	}

	public function testTextMailPart() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="text" {
				echo("This is a text email!");
			}
		}

		var mails=getMails();

		assertEquals("test mail1",mails.subject);
		assertEquals("This is a text email!",mails["body"].trim());
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
		assertEquals("text/plain; charset=UTF-8",mails.header["content-type"]);
	}


	public function testHTMLMailPart() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="html" {
				echo("This is a html email!");
			}
		}
		
		var mails=getMails();

		assertEquals("text/html; charset=UTF-8",mails.header["content-type"]);
	}

	public function testMultiMailPart() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="html" {
				echo("This is a html email!");
			}
			mailpart type="text" {
				echo("This is a text email!");
			}
		}

		var mails=getMails();

		expect(findNoCase("multipart/alternative;", mails.header["Content-Type"])).toBeGT(0);
	}

	public function testTextOnlyPartShouldUse7bitEncoding() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a text email!");
		}

		var mails=getMails();
		//debug(mails);

		assertEquals("text/plain; charset=UTF-8", mails.header["Content-Type"]?:"");
		// single parts store their encoding in the header
		assertEquals("7bit", mails.header["Content-Transfer-Encoding"]?:"");
	}

	public function testHtmlOnlyPartShouldUseQuotedPrintableEncoding() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail type="html" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("<p>This is a text email!</p>#chr(10)#<p>another line</p>");
		}

		var mails=getMails();
		//debug(mails);

		assertEquals("text/html; charset=UTF-8", mails.header["Content-Type"]?:"");
		// single parts store their encoding in the header
		assertEquals("quoted-printable", mails.header["Content-Transfer-Encoding"]?:"");
	}

	public function testMultiMailPartShouldUse7bitEncodingForTextAndQuotedPrintableEncodingForHtml() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="text" {
				echo("This is a html email!");
			}
			mailpart type="html" {
				echo("<p>This is a text email!</p>#chr(10)#<p>another line</p>");
			}
		}

		var message=getFirstMailAsEML();
		//debug(mails);

		// since we have multiple parts, we need to check the parts in the body
		assertTrue(reFindNoCase("(?m)^multipart/alternative;", message.headers["Content-Type"]?:""), "Expected multipart/alternative content type!");
		assertTrue(reFindNoCase("\n------=[A-Z0-9._]+\r?\nContent-Type: text\/plain;[^\r\n]*\r?\nContent-Transfer-Encoding: 7bit\r?\n", message.content), "Wrong content encoding for Text part!");
		assertTrue(reFindNoCase("\n------=[A-Z0-9._]+\r?\nContent-Type: text/html;[^\r\n]*\r?\nContent-Transfer-Encoding: quoted-printable\r?\n", message.content), "Wrong content encoding for HTML part!");
	}

	public function testHtmlOnlyPartShouldUse7bitEncodingWhenSystemPropertySet() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		// fallback to the old behavior of using 7bit encoding
		createObject("java", "java.lang.System").setProperty("lucee.mail.use.7bit.transfer.encoding.for.html.parts", "true");

		mail type="html" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("<p>This is a text email!</p>#chr(10)#<p>another line</p>");
		}

		var mails=getMails();
		//debug(mails);

		assertEquals("text/html; charset=UTF-8", mails.header["Content-Type"]?:"");
		// single parts store their encoding in the header
		assertEquals("7bit", mails.header["Content-Transfer-Encoding"]?:"");
	}

	public function testMultiMailPartShouldUse7bitEncodingForTextAnd7bitEncodingForHtmlWhenSystemPropertySet() localmode="true" skip="notHasServices" {
		if(getJavaVersion()>8) return;
		// fallback to the old behavior of using 7bit encoding
		createObject("java", "java.lang.System").setProperty("lucee.mail.use.7bit.transfer.encoding.for.html.parts", "true");

		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="text" {
				echo("This is a html email!");
			}
			mailpart type="html" {
				echo("<p>This is a text email!</p>#chr(10)#<p>another line</p>");
			}
		}

		var message=getFirstMailAsEML();
		//debug(mails);

		// since we have multiple parts, we need to check the parts in the body
		assertTrue(reFindNoCase("(?m)^multipart/alternative;", message.headers["Content-Type"]?:""), "Expected multipart/alternative content type!");
		assertTrue(reFindNoCase("\n------=[A-Z0-9._]+\r?\nContent-Type: text\/plain;[^\r\n]*\r?\nContent-Transfer-Encoding: 7bit\r?\n", message.content), "Wrong content encoding for Text part!");
		assertTrue(reFindNoCase("\n------=[A-Z0-9._]+\r?\nContent-Type: text/html;[^\r\n]*\r?\nContent-Transfer-Encoding: 7bit\r?\n", message.content), "Wrong content encoding for HTML part!");
	}

	private function getJavaVersion() {
		var raw=server.java.version;
		var arr=listToArray(raw,'.');
		if (arr[1]==1) // version 1-9
			return arr[2];
		return listFirst( arr[1], "-" ); // return 25 from java 25-ea
	}
}