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
component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.PORT=2526;
	variables.from="susi@sorglos.de";
	variables.to="geisse@peter.ch";

	public function beforeTests(){
		admin 
			action="updateMailServer"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			hostname="localhost"
			dbusername=""
			dbpassword=""
			life="1"
			idle="1"
			port="#PORT#"
			id="123"
			tls="false"
			ssl="false"
			reuseConnection=false;

	}
	public function afterTests(){
		admin 
			action="removeMailServer"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			hostname="localhost";

	}


	private array function getMails(smtpServer) localmode=true{
		arr=[];
		it=smtpServer.getReceivedEmail();
		while(it.hasNext()) {
			email=it.next();
			names=email.getHeaderNames();
			sct={};
			while(names.hasNext()) {
				name=names.next();
				sct[name]=email.getHeaderValue(name);
			}
			if(StructCount(sct)){
				sct["body"]=email.getBody();
				arrayAppend(arr,sct);
			}
		}
		return arr;
	}

	private function start() {
		return createObject("java","com.dumbster.smtp.SimpleSmtpServer","smtp.dumbster","1.6.0").start(PORT);
	}


	public function testSimpleMail() localmode="true"{
		try { 
			smtpServer=start();
			mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
				echo("This is a text email!");
			}
		}
		finally {
			smtpServer.stop();
		}
		
		mails=getMails(smtpServer);

		assertEquals(1,mails.len());
		assertEquals("test mail1",mails[1].subject);
		assertEquals("This is a text email!",mails[1]["body"]);
		assertEquals(variables.from,mails[1]["from"]);
		assertEquals(variables.to,mails[1]["to"]);
		assertEquals("text/plain; charset=UTF-8",mails[1]["content-type"]);
	}


	public function testHTMLMail() localmode="true"{
		try { 
			smtpServer=start();
			mail type="html" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
				echo("This is a HTML email!");
			}
		}
		finally {
			smtpServer.stop();
		}
		mails=getMails(smtpServer);

		assertEquals("text/html; charset=UTF-8",mails[1]["content-type"]);
	}

	public function testTextMail() localmode="true"{
		try { 
			smtpServer=start();
			mail type="plain" to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
				echo("This is a text email!");
			}
		}
		finally {
			smtpServer.stop();
		}
		mails=getMails(smtpServer);

		assertEquals("text/plain; charset=UTF-8",mails[1]["content-type"]);
	}

	public function testTextMailPart() localmode="true"{
		try { 
			smtpServer=start();
			mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
				mailpart type="text" {
	                echo("This is a text email!");
				}
			}
		}
		finally {
			smtpServer.stop();
		}
		mails=getMails(smtpServer);

		assertEquals(1,mails.len());
		assertEquals("test mail1",mails[1].subject);
		assertEquals("This is a text email!",mails[1]["body"]);
		assertEquals(variables.from,mails[1]["from"]);
		assertEquals(variables.to,mails[1]["to"]);
		assertEquals("text/plain; charset=UTF-8",mails[1]["content-type"]);
	}


	public function testHTMLMailPart() localmode="true"{
		try { 
			smtpServer=start();
			mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
				mailpart type="html" {
	                echo("This is a html email!");
				}
			}
		}
		finally {
			smtpServer.stop(); 
		}
		mails=getMails(smtpServer);

		assertEquals(1,mails.len());
		// dump(mails);
	}

	public function testMultiMailPart() localmode="true"{
		try { 
			smtpServer=start();
			mail to=variables.to from=variables.from subject="test mail1" spoolEnable=false {
				mailpart type="html" {
	                echo("This is a html email!");
				}
				mailpart type="text" {
	                echo("This is a text email!");
				}
			}
		}
		finally {
			smtpServer.stop();
		}
		mails=getMails(smtpServer);

		assertEquals(1,mails.len());
		// dump(mails);
	}



}