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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

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
		admin 
			action="removeMailServer"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			hostname="#creds.smtp.SERVER#";
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

	private struct function getMails() localmode=true {
		pop action="getAll" name="local.inboxemails" server="#creds.pop.server#" password="#creds.pop.password#" port="#creds.pop.PORT_INSECURE#" secure="no" username="#variables.to#";

		sct = queryGetRow(inboxemails,queryRecordCount(inboxemails));

		// delete inbox mails after getting mail for the test So the inbox always have one mail and getting mail will take less time
		pop action="delete" server="#creds.pop.server#" password="#creds.pop.password#" port="#creds.pop.PORT_INSECURE#" secure="no" username="#variables.to#";

		sct.header = headerToStruct(sct.header);	
		return sct;
	}

	// converts the header string to struct
	private struct function headerToStruct(Required string str) localmode=true {
		a = reMatchNoCase("\s([a-zA-Z-]+\:)",str);

		keyWithComma = arrayToList(a,"|").listmap((e)=>{
			return "," & e;
		},"|");

		l = replaceListNocase(str, arrayToList(a), keyWithComma, ",", "|");

		sct = {};
		loop list="#l#" item="e" index="i" {
			sct[trim(listFirst(e,":"))] = trim(listLast(e,":"));
		}
		return sct;
	}


	// tests
	public function testSimpleMail() localmode="true" skip="notHasServices" {

		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a text email!");
		}
		
		mails=getMails();

		assertEquals("test mail1",mails.subject);
		assertEquals("This is a text email!",mails["body"].trim());
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
		assertEquals("text/plain; charset=UTF-8",mails.header["content-type"]);
	}


	public function testHTMLMail() localmode="true" skip="notHasServices" {
		mail type="html" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a HTML email!");
		}
		mails=getMails();

		assertEquals("text/html; charset=UTF-8",mails.header["content-type"]);
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
	}

	public function testTextMail() localmode="true" skip="notHasServices" {
		mail type="plain" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			echo("This is a text email!");
		}
		mails=getMails();

		assertEquals("text/plain; charset=UTF-8",mails.header["content-type"]);
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
	}

	public function testTextMailPart() localmode="true" skip="notHasServices" {
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="text" {
				echo("This is a text email!");
			}
		}
		mails=getMails();

		assertEquals("test mail1",mails.subject);
		assertEquals("This is a text email!",mails["body"].trim());
		assertEquals(variables.from,mails["from"]);
		assertEquals(variables.to,mails["to"]);
		assertEquals("text/plain; charset=UTF-8",mails.header["content-type"]);
	}


	public function testHTMLMailPart() localmode="true" skip="notHasServices" {
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="html" {
				echo("This is a html email!");
			}
		}
		mails=getMails();

		assertEquals("text/html; charset=UTF-8",mails.header["content-type"]);
	}

	public function testMultiMailPart() localmode="true" skip="notHasServices" {
		mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
			mailpart type="html" {
				echo("This is a html email!");
			}
			mailpart type="text" {
				echo("This is a text email!");
			}
		}
		mails=getMails();

		expect(findNoCase("multipart/alternative;", mails.header["Content-Type"])).toBeGT(0);
	}

}