<!---
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 *
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="tagFunctionNon" access="private">
		<cfreturn getTickCount()>
	</cffunction>
	<cffunction name="tagFunctionCreateTimespan" cachedWithin="#createTimespan(0,0,0,0,20)#" access="private">
		<cfreturn getTickCount()>
	</cffunction>
	<cffunction name="tagFunctionDouble" cachedWithin="0.0000003" access="private">
		<cfreturn getTickCount()>
	</cffunction>
	<cffunction name="tagFunctionRequest" cachedWithin="request" access="private">
		<cfreturn getTickCount()>
	</cffunction>
	<cfscript>
	private function scriptFunctionNon() {
		return getTickCount();
	}
	private function scriptFunctionCreateTimespan() cachedWithin="#createTimespan(0,0,0,0,20)#"{
		return getTickCount();
	}
	private function scriptFunctionDouble() cachedWithin="0.0000003"{
		return getTickCount();
	}
	private function scriptFunctionRequest() cachedWithin="request"{
		return getTickCount();
	}

	function testTagFunctionNon(){
		var initVal=tagFunctionNon();
		sleep(1);
		assertEquals(false,initVal == tagFunctionNon());
	}

	function testScriptFunctionNon(){
		var initVal=scriptFunctionNon();
		sleep(1);
		assertEquals(false,initVal == scriptFunctionNon());
	}


	function testScriptFunctionCreateTimespan(){
		var initVal=scriptFunctionCreateTimespan();
		sleep(1);
		expect(initVal).toBe(scriptFunctionCreateTimespan());
		sleep(25);
		expect(initVal).notToBe(scriptFunctionCreateTimespan());
	}

	function testTagFunctionCreateTimespan(){
		var initVal=tagFunctionCreateTimespan();
		sleep(1);
		expect(initVal).toBe(tagFunctionCreateTimespan());
		sleep(25);
		expect(initVal).notToBe(tagFunctionCreateTimespan());
	}

	// createTimeSpan(0,0,0,0,25)
	function testTagFunctionDouble(){
		var initVal=tagFunctionDouble();
		sleep(2);
		assertEquals(true,initVal == tagFunctionDouble());
		sleep(30);
		assertEquals(false,initVal == tagFunctionDouble());
	}
	function testScriptFunctionDouble(){
		var initVal=scriptFunctionDouble();
		sleep(2);
		assertEquals(true,initVal == scriptFunctionDouble());
		sleep(30);
		assertEquals(false,initVal == scriptFunctionDouble());
	}


	function testScriptFunctionRequest(){
		var initVal=scriptFunctionRequest();
		sleep(1);
		assertEquals(true,initVal == scriptFunctionRequest());
	}
	function testTagFunctionRequest(){
		var initVal=tagFunctionRequest(argumentcollection:{});
		sleep(1);
		assertEquals(true,initVal == tagFunctionRequest());
	}
	</cfscript>

</cfcomponent>