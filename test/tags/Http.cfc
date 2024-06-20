/*
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	variables.updateProvider =  server.getTestService("updateProvider").url;

	public function testHTTP() localmode="true"{
		http url="http://www.google.com";
		expect( cfhttp.error ).toBe(  false );
		expect( cfhttp.status_code ).toBe( 200 );
	}

	public function testHTTPs() localmode="true"{
		http url="https://www.google.com";
		expect( cfhttp.status_code ).toBe( 200 );
		expect( cfhttp.error ).toBe( false);
	}

	public function testInvalidHostName() localmode="true"{
		http url="https://www.lucee.o1rg";
		expect( cfhttp.error ).toBe( true );
		expect( cfhttp.status_code ).toBe( 0 );

		expect( function(){
			http url="https://www.lucee.o1rg" throwOnError=true;
		}).toThrow();
	}

	public function test404() localmode="true"{
		http url="#variables.updateProvider#/rest/update/provider/404";
		expect( cfhttp.error ).toBe( true );
		expect( cfhttp.status_code ).toBe( 404 );
		expect( function(){
			http url="#variables.updateProvider#/rest/update/provider/404" throwOnError=true;
		}).toThrow();
	}


	public void function testDefaultHTTPParamType(){
		http url="#variables.updateProvider#/rest/update/provider/echoGet" result="local.res" method="get"{
			httpparam name="susi" value="Sorglos";
		}
		res=deserializeJSON(res.filecontent);

		assertEquals("Sorglos",res.url.susi);
	}

	public void function testPatch(){
		http url="#variables.updateProvider#/rest/update/provider/echoGet" result="local.res" method="patch"{
			httpparam name="susi" value="Sorglos";
		}
	}

	public void function testImplicit() skip=true {
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="#variables.updateProvider#/rest/update/provider/echoPut" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" value=data;
		}
		res=deserializeJSON(res.filecontent);
		assertEquals(data,res.httpRequestData.content);
	}

	public void function testQuery(){
		http url="#variables.updateProvider#/rest/update/provider/echoGet" result="local.res" method="query"{
			httpparam name="susi" value="Sorglos";
		}
	}

	public void function testExplicit() skip=true {
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="#variables.updateProvider#/rest/update/provider/echoPut" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" mimetype="text/plain; charset=UTF-8" value=data;
		}
		var res=deserializeJSON(res.filecontent);
		assertEquals(data,res.httpRequestData.content);
	}

	public void function testCheckTLSVersion(){
		http url="https://www.howsmyssl.com/a/check" result="local.res";
		expect(isJson(res.filecontent)).toBeTrue();
		var tlsReport = DeserializeJson(res.filecontent);
		SystemOutput("", true);
		SystemOutput("CFHTTP is using [#tlsReport.tls_version#] (jvm default)", true);
	}
	public void function testCachedHttpRequest(){
		http url="#variables.updateProvider#/rest/update/provider/echoGet" result="local.res" method="get" cachedWithin="request"{
			httpparam name="susi" value="Sorglos";
		}
		http url="#variables.updateProvider#/rest/update/provider/echoGet" result="local.res2" method="get" cachedWithin="request"{
			httpparam name="susi" value="Sorglos";
		}
		systemOutput("", true);
		systemOutput(res.filecontent, true);
		res = evaluate( res.filecontent );
		res = deserializeJSON( res.filecontent );
		res2 = deserializeJSON( res2.filecontent );
		expect( res.url.susi ).toBe( res2.url.susi );
	}
}