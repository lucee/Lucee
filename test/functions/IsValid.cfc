/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp() localmode="true"{}
	private function realUDF() {
	   return 1;
	}

	public void function testFunction() localmode="true"{
		assertTrue(isvalid("function",realUDF));
		assertTrue(isvalid("function",function(){}));
		assertTrue(isvalid("function",()=>1));
	}
	public void function testLambda() localmode="true"{
		assertFalse(isvalid("lambda",realUDF));
		assertFalse(isvalid("lambda",function(){}));
		assertTrue(isvalid("lambda",()=>1));
	}
	public void function testClosure() localmode="true"{
		assertFalse(isvalid("closure",realUDF));
		assertTrue(isvalid("closure",function(){}));
		assertFalse(isvalid("closure",()=>1));
	}

	public void function testNumber() localmode="true"{
		assertFalse(isvalid("numeric","Wednesday, January 30, 2002 7:02:12 AM PST"));
		assertTrue(isvalid("numeric","6/2007"));
		assertTrue(isvalid("numeric","true"));
		assertTrue(isvalid("numeric",true));
		assertTrue(isvalid("numeric",123));
		assertTrue(IsValid("numeric",1));
		assertTrue(IsValid("numeric",1.3));
		assertTrue(IsValid("numeric","1"));
		assertTrue(IsValid("numeric",true));
		assertTrue(IsValid("numeric",createObject('java','java.lang.Long').init(3)));
		assertTrue(IsValid("numeric","sss".length()));
		assertTrue(isValid('numeric','3',3,3));
		assertTrue(isValid('numeric','3',2,4));
		assertFalse(IsValid("numeric","hans"));
		assertFalse(IsValid("numeric",structNew()));
		assertFalse(IsValid("numeric",arrayNew(1)));
		assertFalse(isValid('numeric','1',2,4));
		assertFalse(isValid('numeric','5',2,4));
	}


	public void function testBoolean() localmode="true"{
		assertTrue(isvalid("boolean","true"));
		assertTrue(isvalid("boolean",123.123));
		assertTrue(isvalid("boolean",true));
		assertFalse(isvalid("boolean","6/2007"));
		assertTrue(IsValid("boolean",true));
		assertTrue(IsValid("boolean",false));
		assertTrue(IsValid("boolean",1));
		assertTrue(IsValid("boolean",0));
		assertTrue(IsValid("boolean",999));
		assertTrue(IsValid("boolean","999"));
		assertTrue(IsValid("boolean",9.99));
		assertTrue(IsValid("boolean","yes"));
		assertTrue(IsValid("boolean","true"));
		assertFalse(IsValid("boolean","peter"));
		assertFalse(IsValid("boolean",now()));
	}

	public void function testDate() localmode="true"{
		assertTrue(isvalid("date","6/2007"));
		assertTrue(isvalid("date",123.123));
		assertTrue(isvalid("date",now()));
		assertFalse(isvalid("date","true"));
		assertTrue(IsValid("date",1));
		assertTrue(IsValid("date",123));
		assertTrue(IsValid("date","{ts '2004-04-10 15:53:02'}"));
		assertTrue(IsValid("date",1));
		assertFalse(IsValid("date",true));
		assertFalse(IsValid("date","xxx"));
	}

	public void function testTime() localmode="true"{
		assertTrue(IsValid("time",now()));
		assertTrue(IsValid("time","{ts '2004-04-10 15:53:02'}"));
		assertFalse(IsValid("time",1));
		assertFalse(IsValid("time",true));
		assertFalse(IsValid("time","xxx"));
		assertFalse(IsValid("time",123));
		assertFalse(IsValid("time",1));
	}

	public void function testAny() localmode="true"{
		assertTrue(IsValid("any",'string'));
		assertTrue(IsValid("any",1));
		assertTrue(IsValid("any",true));
		assertTrue(IsValid("any",now()));
		assertTrue(IsValid("any",structNew()));
		assertTrue(IsValid("any",arrayNew(1)));
		assertTrue(IsValid("any",queryNew('aaa,bbb,ccc')));

	}


	public void function testArray() localmode="true"{
		assertTrue(IsValid("array","Hello".toCharArray()));
		assertTrue(IsValid("array",arrayNew(1)));
		assertFalse(IsValid("array",true));
		assertFalse(IsValid("array",structNew()));
		assertFalse(IsValid("array","array"));

	}

	public void function testBinary() localmode="true"{
		file 
			action="readbinary" 
			file="#GetCurrentTemplatePath()#" 
			variable="local.content";
		assertTrue(IsValid("binary",content));
		assertTrue(IsValid("binary","Hello".getBytes()));
		assertFalse(IsValid("binary",true));
		assertFalse(IsValid("binary",structNew()));
		assertFalse(IsValid("binary","array"));
		assertFalse(IsValid("binary","Hello".toCharArray()));
		assertFalse(IsValid("binary",arrayNew(1)));

	}


	public void function testFloat() localmode="true"{
		assertTrue(IsValid("float",1));
		assertTrue(IsValid("float",1.3));
		assertTrue(IsValid("float","1"));
		assertTrue(IsValid("float",true));
		assertTrue(IsValid("float",createObject('java','java.lang.Long').init(3)));
		assertTrue(IsValid("float","Hello".length()));
		assertFalse(IsValid("float","hans"));
		assertFalse(IsValid("float",now()));
		assertFalse(IsValid("float",structNew()));
		assertFalse(IsValid("float",arrayNew(1)));

	}

	public void function testQuery() localmode="true"{
		assertTrue(IsValid("query",queryNew('aaa,bbb,ccc')));
		assertFalse(IsValid("query",'string'));
		assertFalse(IsValid("query",1));
		assertFalse(IsValid("query",true));
		assertFalse(IsValid("query",now()));
		assertFalse(IsValid("query",structNew()));
		assertFalse(IsValid("query",arrayNew(1)));
	}

	public void function testStruct() localmode="true"{
		assertTrue(IsValid("struct",structNew()));
		assertTrue(IsValid("struct",createObject("java","java.util.HashMap").init( )));
		assertFalse(IsValid("struct",'string'));
		assertFalse(IsValid("struct",1));
		assertFalse(IsValid("struct",true));
		assertFalse(IsValid("struct",now()));
		assertFalse(IsValid("struct",arrayNew(1)));
		assertFalse(IsValid("struct",queryNew('aaa,bbb,ccc')));
	}

	public void function testVariableName() localmode="true"{
		assertTrue(IsValid("variablename",'string'));
		assertTrue(IsValid("variablename",true));
		assertTrue(IsValid("variablename",'abc.abc'));
		assertFalse(IsValid("variablename",1));
		assertFalse(IsValid("variablename",now()));
		assertFalse(IsValid("variablename",'1abc'));
		assertFalse(IsValid("variablename",'abc abc'));
		assertFalse(IsValid("variablename",'abc&&'));
		assertFalse(IsValid("variablename",'abc[1]'));

	}

	public void function testCreditCard() localmode="true"{
		assertTrue(IsValid("creditcard",'4000000000006'));
		assertTrue(IsValid("creditcard",'378888888888858'));
		assertTrue(IsValid("creditcard",'4888888888888838'));
		assertTrue(IsValid("creditcard",'5588888888888838'));
		assertTrue(IsValid("creditcard",'6011222233334444'));
		assertTrue(IsValid("creditcard",'6011-2222-3333-4444'));
		assertTrue(IsValid("creditcard",'6011 2222 3333 4444'));
		assertTrue(IsValid("creditcard",'6011,2222,3333,4444'));
		assertFalse(IsValid("creditcard",'abc'));
		assertFalse(IsValid("creditcard",'6010222233334444'));
		assertFalse(IsValid("creditcard",'4000000000007'));
	}

	public void function testEmail() localmode="true"{
		// no longer pass with javax.mail 1.4.7 assertTrue(IsValid("email",'a-b.c@abc-_fgdg.dfgd.dj'));
		assertTrue(isValid('email','a@gmail.com'));
		assertTrue(isValid('email','A@gmail.com'));
		assertTrue(isValid('email','0@gmail.com'));
		assertTrue(isValid('email','_@gmail.com'));
		assertTrue(isValid('email','+@gmail.com'));
		assertTrue(isValid('email','-@gmail.com'));
		assertTrue(isValid('email','AZ.+-_az09@gmail.com'));
		assertTrue(isValid('email','user+foo@gmail.com'));
		assertTrue(isValid('email','user.foo@gmail.com'));
		assertTrue(isValid('email','user+foo@gmail.com'));
		assertTrue(isValid('email','user-foo@gmail.com'));
		assertTrue(isValid('email','user_foo@gmail.com'));
		assertTrue(isValid('email','user+@gmail.com'));
		assertTrue(isValid('email','user-@gmail.com'));
		assertTrue(isValid('email','user_@gmail.com'));
		assertTrue(isValid('email','+foo@gmail.com'));
		assertTrue(isValid('email','-foo@gmail.com'));
		assertTrue(isValid('email','_foo@gmail.com'));
		assertTrue(isValid('email','foo@gm.ail.com'));
		assertFalse(IsValid("email",'a-b.c@a@bc-_fgdg.dfgd.dj'));
		// MUST assertFalse(IsValid("email",'a-b.c@a+bc-_fgdg.dfgd.dj'));
		assertFalse(IsValid("email",'@abc-_fgdg.dfgd.dj'));
		assertFalse(IsValid("email",'dddd@abc-_fgdg.dfgd.'));
		assertFalse(isValid('email','user.@gmail.com'));
		assertFalse(isValid('email','.foo@gmail.com'));
		// MUST assertFalse(isValid('email','user&foo@gmail.com'));
		// MUST assertFalse(isValid('email','user/foo@gmail.com'));
		// MUST assertFalse(isValid('email','user=foo@gmail.com'));
		// MUST assertFalse(isValid('email','foo@gm+ail.com'));
		// MUSTassertFalse(isValid('email','foo@gm&ail.com'));

	}

	public void function testUUID() localmode="true"{
		assertTrue(IsValid("uuid",'858162ee-20fb-49d9-b39f9292de01572e'));
		assertFalse(IsValid("uuid",'858162ee-20fb-49d9-b39f-9292de01572e'));

	}

	public void function testGUID() localmode="true"{
		assertTrue(IsValid("guid",'858162ee-20fb-49d9-b39f-9292de01572e'));
		assertFalse(IsValid("guid",'858162ee-20fb-49d9-b39f9292de01572e'));

	}

	public void function testInteger() localmode="true"{
		assertTrue(IsValid("integer",'1234'));
		assertTrue(IsValid("integer",123));
		assertTrue(IsValid("integer",123.000));
		assertFalse(IsValid("integer",'123.4'));
		assertFalse(IsValid("integer",12.3));
		assertFalse(IsValid("integer","-21474836480"));
		assertFalse(IsValid("integer","21474836470"));
		assertFalse(IsValid("integer",true));

	}

	public void function testSSN() localmode="true"{
		assertTrue(IsValid("ssn",'987-65-4320'));
	}

	public void function testString() localmode="true"{
		assertTrue(IsValid("string",''));
		assertTrue(IsValid("string",'string'));
		assertTrue(IsValid("string",1));
		assertTrue(IsValid("string",true));
		assertTrue(IsValid("string",now()));
		assertFalse(IsValid("string",structNew()));
		assertFalse(IsValid("string",arrayNew(1)));
		assertFalse(IsValid("string",queryNew('aaa,bbb,ccc')));
		assertFalse(IsValid("string",createObject("java","java.util.HashMap").init( )));
		assertTrue(isValid('string','ddd',3,3));
		assertTrue(isValid('string','ddd',2,4));
		assertFalse(isValid('string','d',2,4));
		assertFalse(isValid('string','ddddd',2,4));

	}


	public void function testPhone() localmode="true"{
		assertTrue(IsValid("telephone",'1.678.256.3011'));
		assertTrue(IsValid("telephone",'16782563011'));
	}

	public void function testURL() localmode="true"{
		assertTrue(IsValid("url",'https://www.lucee.org/'));
		assertTrue(IsValid("url",  "http://a"));
		assertTrue(IsValid("url", "http://www.lucee.com/svn.cfm?repositorypath=viewCount%2FviewCount-plugin-Mangoblog-v1.4.zip%3A108&download=1"));
		assertFalse(IsValid("url",  "http://.-"));
		assertFalse(IsValid("url",'1.678.256.3011'));
		assertFalse(IsValid("url",  "http://."));
		// MUST assertFalse(IsValid("url", "http://www.lucee.com/svn.cfm?repositorypath=viewCount/viewCount-plugin-Mangoblog-v1.4.zip:108&download=1"));

	}

	public void function testZip() localmode="true"{
		assertTrue(IsValid("zipcode",'12345'));
		assertTrue(IsValid("zipcode",'12345-1234'));
		assertTrue(IsValid("zipcode",'12345 1234'));
		assertFalse(IsValid("zipcode",'12345|1234'));
		assertFalse(IsValid("zipcode",'12345 123'));
		assertFalse(IsValid("zipcode",'123451234'));

	}

	public void function testRange() localmode="true"{
		assertTrue(IsValid("range",'3',1,5));
		assertTrue(IsValid("range",'1',1,5));
		assertTrue(IsValid("range",'5',1,5));
		assertFalse(IsValid("range",'6',1,5));
		assertFalse(IsValid("range",'0',1,5));
		assertFalse(IsValid("range",'10',10,5));
		assertFalse(IsValid("range",'s',1,5));
		assertFalse(isValid('range','ddd',1,4));

	}


	public void function testRegex() localmode="true"{
		assertTrue(IsValid("regex",'abc','...'));
		assertTrue(IsValid("regex",'abc','.+'));
		assertTrue(IsValid("regex",'abc','[abc]{3}'));
		assertFalse(IsValid("regex",'ABC','[abc]{3}'));
		assertFalse(IsValid("regex",'(abc','[abc]{3}'));

	}

	public void function testUSDate() localmode="true"{
		assertTrue(isValid('usdate','1/1/2001'));
		assertTrue(isValid('usdate','12/31/2001'));
		assertTrue(isValid('usdate','2/29/2000'));
		assertTrue(isValid('usdate','1.1.2001'));
		assertTrue(isValid('usdate','12.31.2001'));
		assertTrue(isValid('usdate','2.29.2000'));
		assertTrue(isValid('usdate','1-1-2001'));
		assertTrue(isValid('usdate','12-31-2001'));
		assertTrue(isValid('usdate','2-29-2000'));
		assertFalse(isValid('usdate','31/12/2001'));
		assertFalse(isValid('usdate','13/31/2001'));
		assertFalse(isValid('usdate','2/29/2001'));
		assertFalse(isValid('usdate','31.12.2001'));
		assertFalse(isValid('usdate','13.31.2001'));
		assertFalse(isValid('usdate','2.29.2001'));
		assertFalse(isValid('usdate','31-12-2001'));
		assertFalse(isValid('usdate','13-31-2001'));
		assertFalse(isValid('usdate','2-29-2001'));
		assertFalse(isValid('usdate','1-1.2001'));
		assertFalse(isValid('usdate','1/1.2001'));
		assertFalse(isValid('usdate',now()));

	}

	public void function testEuroDate() localmode="true"{
		assertTrue(isValid('eurodate','1/1/2001'));
		assertTrue(isValid('eurodate','31/12/2001'));
		assertTrue(isValid('eurodate','29/2/2000'));
		assertTrue(isValid('eurodate','1.1.2001'));
		assertTrue(isValid('eurodate','31.12.2001'));
		assertTrue(isValid('eurodate','29.2.2000'));
		assertFalse(isValid('eurodate','12/31/2001'));
		assertFalse(isValid('eurodate','31/13/2001'));
		assertFalse(isValid('eurodate','29/2/2001'));
		assertFalse(isValid('eurodate','12.31.2001'));
		assertFalse(isValid('eurodate','31.13.2001'));
		assertFalse(isValid('eurodate','29.2.2001'));
		assertFalse(isValid('eurodate','1-1.2001'));
		assertFalse(isValid('eurodate','1/1.2001'));
		assertFalse(isValid('eurodate',""&now()));
		assertFalse(isValid('eurodate',now()));

	}

	public void function testJSON() localmode="true"{
		assertTrue(isValid('json','{}'));
		assertTrue(isValid('json','[]'));
		assertTrue(isValid('json','[{}]'));
		assertTrue(isValid('json','{"a":"a"}'));
		assertFalse(isValid('json','{a:a}'));
		assertFalse(isValid('json','string'));
		assertTrue(isValid('json',1));
		assertTrue(isValid('json',true));
	}

	public void function testObject() localmode="true"{
		assertTrue(isValid("object",createObject("java", "java.lang.System")));
        assertTrue(isValid("object",createObject("component","org.lucee.cfml.test.LuceeTestCase")));
        assertFalse(isValid("object","string"));
        assertFalse(isValid("object",1));
        assertFalse(isValid("object",true));
        assertFalse(isValid("object",{}));
        assertFalse(isValid("object",[]));
	}

}