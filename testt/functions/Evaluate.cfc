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
	<cffunction name="testEvaluate" localMode="modern">

<!--- begin old test code --->
<cfset start=getTickCount()>

<cfset qwert=1>

<cfset valueEquals(left="#evaluate('"sss"')#", right="sss")>
<cfset valueEquals(left="#evaluate(1234)#", right="1234")>
<cfset valueEquals(left="#evaluate('qwert')#", right="1")>
<cfset valueEquals(left="#evaluate('"bnha.b.c"=3')#", right="3")>
<cfset valueEquals(left="#evaluate('len("abcd")')#", right="4")>
<cfset valueEquals(left="#evaluate('#4#')#", right="4")>

<cfset evaluate('"sss"')>

<cfset evaluate('dsdf.dasda.asdasd.adadasd.aadasd=1')>
<cfset evaluate('aaa.bbb=1')>
<cfset valueEquals(left="#dsdf.dasda.asdasd.adadasd.aadasd#", right="1")>
<cfset valueEquals(left="#aaa.bbb#", right="1")>

<cfset valueEquals(left="#evaluate(1000)#", right="1000")>
<cfset valueEquals(left="#evaluate(true)#", right="true")> 
<cfset evalTest="susi">
<cfset susi="hans">
<cfset valueEquals(left="#evaluate('evalTest')#", right="susi")> 
<cfset valueEquals(left="#evaluate(evalTest)#", right="hans")> 
<cfset valueEquals(left="#evaluate('''hello''')#", right="hello")> 
<cfset valueEquals(left="#evaluate(de('hello'))#", right="hello")> 

<cfset valueEquals(left="#evaluate("len('222')")#", right="3")> 

<cfset valueEquals(left="#evaluate("peter='hans'")#", right="hans")>
<cfset valueEquals(left="#evaluate("1+1")#", right="2")>
<cfset valueEquals(left="#evaluate("1 lt 12")#", right="#true#")>
<cfset valueEquals(left="#evaluate("'susi' lte 'susi'")#", right="#true#")>

<cfset valueEquals(left="#evaluate("'peter'='sss'")#", right="sss")>
<cfset valueEquals(left="#evaluate("Udf(1234)")#", right="hello from udf 1234")>
<cfset valueEquals(left="#evaluate("Udf2(arg1:1235)")#", right="hello from udf2 1235")>
<cfset valueEquals(left="#evaluate("variables.Udf(1234)")#", right="hello from udf 1234")>


<cfset evalqry=queryNew("aaa,bbb,ccc")>
	<cfset QueryAddRow(evalqry)>
	<cfset QuerySetCell(evalqry,"aaa","susi")>
	<cfset QuerySetCell(evalqry,"bbb","1")>
	<cfset QueryAddRow(evalqry)>
	<cfset QuerySetCell(evalqry,"aaa","peter")>
	<cfset QuerySetCell(evalqry,"bbb","2")>

<cfset valueEquals(left="#evaluate("valueList(evalqry.aaa)")#", right="susi,peter")>
<cfset valueEquals(left="#evaluate("parameterExists(a.b.c.d.ef.g.h)")#", right="#false#")>

<cfset xyx=evaluate("peterxx()")>
<cfset valueEquals(left="#isDefined("xyx")#", right="#false#")>


<cfset qry=queryNew("aaa,bbb")>
	<cfset QueryAddRow(qry)>
		<cfset QuerySetCell(qry,"aaa",1)>
		<cfset QuerySetCell(qry,"bbb",1)>
	<cfset QueryAddRow(qry)>
		<cfset QuerySetCell(qry,"aaa",2)>
		<cfset QuerySetCell(qry,"bbb",2)>
<cfset valueEquals(left="#evaluate(qry.aaa[2])#", right="2")>
<cfset valueEquals(left="#evaluate('qry.aaa')#", right="1")>
<cfset valueEquals(left="#evaluate('qry.aaa[2]')#", right="2")>
<cfset valueEquals(left="#evaluate('qry["aaa"][2]')#", right="2")>
<cfset valueEquals(left="#evaluate('local.qry["aaa"][2]')#", right="2")>
<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"][2]')#", right="2")>

<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"][2]')#", right="2")>
<cfif server.ColdFusion.ProductName EQ "lucee">
<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"]=1')#", right="1")>
<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"][2]=1')#", right="1")>
<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"][2]')#", right="1")>
<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"]')#", right="1")>
<cfset valueEquals(left="#evaluate('local[''qry'']["aaa"][2]')#", right="1")>
</cfif>

<!--- 
<cfset valueEquals(left="#evaluate('udfx(1)')#", right="1")>
<cfset valueEquals(left="#evaluate('udfx(arg1:1)')#", right="1")>
<cfset valueEquals(left="#evaluate('udfx(arg1=1)')#", right="1")>
 --->
<cfset valueEquals(left="#udfx(1,2)#", right="12")>
<cfset valueEquals(left="#udfx(arg2:1,arg1:2)#", right="21")>
<cfset valueEquals(left="#udfx(arg2=1,arg1=2)#", right="21")>
<cfset valueEquals(left="#udfx(arg2=1,arg1=2)#", right="21")>

<cfset valueEquals(left="#evaluate('udfx(1,2)')#", right="12")>
<cfset valueEquals(left="#evaluate('udfx(arg2:1,arg1:2)')#", right="21")>
<cfset valueEquals(left="#evaluate('udfx(arg2=1,arg1=2)')#", right="21")>

<cfset q=querynew("aaa")>
<cfset queryAddRow(q,1)>
	<cfset QuerySetCell(q,"aaa",1)>

<cfset valueEquals(left="#isNumeric(evaluate('q.aaa'))#", right="true")>
<cfset valueEquals(left="#isNumeric(evaluate('q.aaa'))#", right="true")>
<cfset valueEquals(left="#(evaluate('q.aaa+1'))#", right="2")>
<cfset valueEquals(left="#isNumeric(evaluate('q.aaa[1]'))#", right="true")>

<cfset xid=1>
<cfset valueEquals(left="#evaluate('isNumeric(xid) and xid NEQ 0')#", right="true")>




<cfset qry=querynew('aaa')>
<cfset QueryAddRow(qry)>
<cfset QuerySetCell(qry,'aaa',1)>
<cfset QueryAddRow(qry)>
<cfset QuerySetCell(qry,'aaa',2)>
<cfset valueEquals(left="#isNumeric(qry.aaa)#", right="true")>
<cfset valueEquals(left="#evaluate('isNumeric(qry.aaa[2])')#", right="true")>
<cfset valueEquals(left="#evaluate('isNumeric(qry.aaa[1])')#", right="true")>
<cfset valueEquals(left="#evaluate('(qry.aaa[2])')#", right="2")>
<cfset valueEquals(left="#evaluate('isNumeric(qry.aaa)')#", right="true")>
<cfset valueEquals(left="#isNumeric(qry.aaa)#", right="true")>
<cfset valueEquals(left="#evaluate('qry.aaa+1')#", right="2")>
<cfset valueEquals(left="#evaluate('qry.aaa&1')#", right="11")>
<cfset valueEquals(left="#evaluate('qry.aaa-1')#", right="0")>
<cfset valueEquals(left="#evaluate('qry.aaa/1')#", right="1")>
<cfset valueEquals(left="#evaluate('qry.aaa*2')#", right="2")>

<!--- 
<cfdump var="#qry.columnlist#">
<cfset a=arrayNew(1)>
<cfset a[1]="a">
<cfset a[2]="b">
<cfset QueryAddColumn(qry,"recordcount",a)>


<cfdump var="#qry.recordcount#">
 --->
 
<cfif server.ColdFusion.ProductName EQ "lucee">
<cfset valueEquals(left="#evaluate('qry.recordcount[2]')#", right="#qry.recordcount[2]#")>
<cfset valueEquals(left="#evaluate('qry.currentrow[2]')#", right="2")>
<cfset valueEquals(left="#evaluate('qry.columnlist[2]')#", right="aaa")>
</cfif>
<cfset valueEquals(left="#evaluate('qry.recordcount')#", right="#qry.recordcount#")>
<cfset valueEquals(left="#evaluate('qry.currentrow')#", right="#qry.currentrow#")>
<cfset valueEquals(left="#evaluate('qry.columnlist')#", right="#qry.columnlist#")>
<!--- 
<cfset valueEquals(left="#evaluate('qry[1].recordcount')#", right="#qry[1].recordcount#")> --->

<cfset value.x=1>

<cfset dtt=CreateDate(2000,12, 12)>
<cfset test="#value#">
<cfset test=evaluate('"##value##"')>
<cfset valueEquals(left="#test.x#", right="1")>


<cfset test=evaluate('"##dtt##abc"')>
<cfset valueEquals(left="#test#", right="{ts '2000-12-12 00:00:00'}abc")>

<cfset test=evaluate('"##dtt####dtt##"')>
<cfset valueEquals(left="#test#", right="{ts '2000-12-12 00:00:00'}{ts '2000-12-12 00:00:00'}")>

<cfset test="#dtt##dtt#">
<cfset valueEquals(left="#test#", right="{ts '2000-12-12 00:00:00'}{ts '2000-12-12 00:00:00'}")>

<cfset test=evaluate('"abc##dtt##abc"')>
<cfset valueEquals(left="#test#", right="abc{ts '2000-12-12 00:00:00'}abc")>

<cfset test=evaluate('"ab####c"')>
<cfset valueEquals(left="#test#", right="ab##c")>

<cfset test=evaluate('""')>
<cfset valueEquals(left="#test#", right="")>
<cfset valueEquals(left="#evaluate("'abc'&'def'")#", right="abcdef")>
<cfset valueEquals(left="#evaluate("'abc' does not contain 'a'")#", right="#false#")>
<cfset valueEquals(left="#evaluate("3*3/3")#", right="3")>
<cfset valueEquals(left="#evaluate("Len('abc')")#", right="3")>
<cfset valueEquals(left="#evaluate("'a#2-1#b#1#c'")#", right="a1b1c")>



<cfset test=evaluate('"abc"')>
<cfset valueEquals(left="#test#", right="abc")>

<cftry>
	<cfset test=evaluate('"##value## "')>
	<cfset fail("must throw:Cant cast Complex Object Type Struct to String")>
	<cfcatch></cfcatch>
</cftry>
<!---  
@todo do support this
<cfset valueEquals(left="#evaluate("struct(a=1,b=2,c=3)")#", right="CFMLSTRing")> --->


<cfset evaluate('sct.a=1')>
<cfset evaluate('sct.1a=1')>
<cfset evaluate('sct.1=1')>
<cfset evaluate('dsdf.dasda.asdasd.adadasd.aadasd=1')>
<cfset p="susi">
<cfset "#ListFirst(p)#" = 1>

<cfset t="#structNew()#">

<cfset evaluate('"##ListFirst(p)##" = 1')>



<cfset valueEquals(left="#evaluate('false and true')#", right="false")>
<cfset valueEquals(left="#evaluate('false and false')#", right="false")>
<cfset valueEquals(left="#evaluate('true and true')#", right="true")>
<cfset valueEquals(left="#evaluate('true and false')#", right="false")>

<cfsavecontent variable="content"><cfset evaluate('true and isCalled()')></cfsavecontent>
<cfset valueEquals(left="#content#", right="is called")>

<cfsavecontent variable="content"><cfset evaluate('false and isCalled()')></cfsavecontent>

<cfset valueEquals(left="#left(evaluate('7.5^1.3'),12)#",right="13.727126706")>
<cfset valueEquals(left="#left(evaluate('27^1.3'),12)#",right="72.572635247")>

<cfset valueEquals(left="#evaluate('27^1/3')#", right="9")>
<cfset valueEquals(left="#evaluate('27^(1/3)')#", right="3")>

<cfset xml='<peter><hans attr1="q"></hans></peter>'>
<cfset XmlSearch(xml,'/peter')/>
<cfset evaluate("XmlSearch(xml,'/peter')")>


<cfset a=".3">
<cfset b="3.">
<cfset valueEquals(left="#a+b#", right="3.3")>
<cfset valueEquals(left="#evaluate("a+b")#", right="3.3")>


<cfset valueEquals(left="#evaluate("")#", right="")>
<cfset valueEquals(left="#evaluate(" ")#", right="")>
<cfset valueEquals(left="#evaluate("0")#", right="0")>

	<cfset a=structNew()>
    <cfset c=structNew()>
    <cfset a.b=listToArray('d')>
    <cfset c.d.e="Susi">
    <cfset f="e">
    <cfset sVal = evaluate( "c[ a.b[ 1 ] ].#f#" )>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	
	
<cffunction name="udf2" output="false" access="private">
	<cfargument name="arg1" required="false" default="">
	<cfreturn "hello from udf2 "&arg1>
	
</cffunction>

<cffunction name="udfx" access="private">
	<cfargument name="arg1">
	<cfargument name="arg2">
	<cfreturn arguments.arg1&arguments.arg2>
</cffunction>
	<cfscript>
	private function isCalled() {
		writeOutput("is called");
		return true;
	}
	private function udf(str) {
		return "hello from udf "&str;
	}
	private function udf3(str) {
		return listToArray(str);
	}
	private function peterxx() {
	}
</cfscript>
	
	
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>