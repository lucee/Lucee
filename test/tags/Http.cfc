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

	variables.httpbin = server.getTestService("httpbin");

	function beforeAll() {
		// Skip httpbin tests if service is not available
		variables.hasHttpbin = structCount(variables.httpbin) > 0;
		variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";
	}

	function testConnectionTimeout() {
		if (!variables.hasHttpbin) return;
		
		http url="http://#variables.httpbin.server#:#variables.httpbin.port#/delay/1" result="local.res";
		expect( res.status_code ).toBe(  200 );
		
		http url="https://lucee.org" connectionTimeout=createTimespan(0,0,0,0,1) result="local.res";
		expect( res.status_code ).toBe(  0 );
	}

	function testSocketTimeout() {
		if (!variables.hasHttpbin) return;
		
		http url="http://#variables.httpbin.server#:#variables.httpbin.port#/delay/1" result="local.res";
		expect( res.status_code ).toBe(  200 );
		
		http url="http://#variables.httpbin.server#:#variables.httpbin.port#/delay/1" sockettimeout=createTimespan(0,0,0,0,1) result="local.res";
		expect( res.status_code ).toBe(  408 );
	}

	function testTimeout() {
		if (!variables.hasHttpbin) return;
		
		  http url="http://#variables.httpbin.server#:#variables.httpbin.port#/delay/10" timeout=createTimespan(0,0,0,0,1) result="local.res";
		  expect( res.status_code ).toBe(  408 );
	}

	public function testHTTP() localmode="true"{
		if (!variables.hasHttpbin) return;
		http url="#variables.httpbinUrl#/anything";
		expect( cfhttp.error ).toBe(  false );
		expect( cfhttp.status_code ).toBe( 200 );
	}

	public function testHTTPs() localmode="true"{
		http url="https://httpbin.org/get";
		if ( cfhttp.status_code == 0 ) return; // skip if the public HTTPS endpoint is unreachable

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
		if (!variables.hasHttpbin) return;
		http url="#variables.httpbinUrl#/status/404";
		expect( cfhttp.error ).toBe( true );
		expect( cfhttp.status_code ).toBe( 404 );
		expect( function(){
			http url="#variables.httpbinUrl#/status/404" throwOnError=true;
		}).toThrow();
	}


	public void function testDefaultHTTPParamType(){
		if (!variables.hasHttpbin) return;
		http url="#variables.httpbinUrl#/anything" result="local.res" method="get"{
			httpparam name="susi" value="Sorglos";
		}
		var res=deserializeJSON(res.filecontent);

		assertEquals("Sorglos",res.args.susi);
	}

	public void function testPatch(){
		if (!variables.hasHttpbin) return;
		http url="#variables.httpbinUrl#/anything" result="local.res" method="patch"{
			httpparam name="susi" value="Sorglos";
		}
	}

	public void function testImplicit() skip=true {
		if (!variables.hasHttpbin) return;
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="#variables.httpbinUrl#/anything" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" value=data;
		}
		var res=deserializeJSON(res.filecontent);
		assertEquals(data,res.data);
	}

	public void function testQuery(){
		if (!variables.hasHttpbin) return;
		http url="#variables.httpbinUrl#/anything" result="local.res" method="query"{
			httpparam name="susi" value="Sorglos";
		}
	}

	public void function testExplicit() skip=true {
		if (!variables.hasHttpbin) return;
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="#variables.httpbinUrl#/anything" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" mimetype="text/plain; charset=UTF-8" value=data;
		}
		var res=deserializeJSON(res.filecontent);
		assertEquals(data,res.data);
	}
	
	public void function testCachedHttpRequest(){
		if (!variables.hasHttpbin) return;
		http url="#variables.httpbinUrl#/anything" result="local.res" method="get" cachedWithin="request"{
			httpparam name="susi" value="Sorglos";
		}
		http url="#variables.httpbinUrl#/anything" result="local.res2" method="get" cachedWithin="request"{
			httpparam name="susi" value="Sorglos";
		}
		var res = deserializeJSON( res.filecontent );
		var res2 = deserializeJSON( res2.filecontent );
		expect( res.args.susi ).toBe( res2.args.susi );
	}
}