<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
 --->
 <cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->

	<cffunction name="testNumbersBreakingInByteForm1" localMode="modern">
		<cfset i=0>
		<cfset assertEquals("9465656331668701",deserializeJson('9465656331668701'))>
	</cffunction>

	<cffunction name="testNumbersBreakingInByteForm2" localMode="modern">
		<cfset i=0>
		<cfset assertEquals("9465656331668701",Evaluate('"9465656331668701"'))>
	</cffunction>

	<cffunction name="testDeSerializeJSON" localMode="modern">

<!--- begin old test code --->
<cfset server.enable21=1>

<cfset sct.a=listToArray('a,b,c,d')>
<cfset sct.b=true>
<cfset sct['susi sorglos']="""">
<cfset sct.d=[1,2,"qq""qq",arrayNew(1),23]>

<cfset qry=queryNew('aaa,bbb')>

<cfset QueryAddRow(qry)>
<cfset querysetCell(qry,'aaa',"a")>
<cfset querysetCell(qry,'bbb',"b")>

<cfset QueryAddRow(qry)>
<cfset querysetCell(qry,'aaa',"c")>
<cfset querysetCell(qry,'bbb',"d")>


<cfset valueEquals(left="#deserializeJSON(1)#x", right="1x")>
<cfset valueEquals(left="#deserializeJSON(true)#", right="true")>
<cfset valueEquals(left="#deserializeJSON('"su##si"')#", right='su##si')>
<cfset valueEquals(left="#deserializeJSON('"sus\"i"')#", right='sus"i')>
<cfset valueEquals(left="#deserializeJSON('"Januar, 01 2000 01:01:01"')#", right='Januar, 01 2000 01:01:01')>
<cfset valueEquals(left="#isArray(deserializeJSON('["a","b","c\"c"]'))#", right='true')>

<cfset s.a="x">
<cfset valueEquals(left="#isStruct(deserializeJSON('{"A":"x"}'))#", right=true)>

<cfset qry1='{"COLUMNS":["AAA","BBB"],"DATA":[["a","b"],["c","d"]]}'>
<cfset qry2='{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}'>

<cfset valueEquals(left="#isQuery(deserializeJSON(qry1,false))#", right='true')>
<cfset valueEquals(left="#isQuery(deserializeJSON(qry1,true))#", right='false')>
<cfset valueEquals(left="#isQuery(deserializeJSON(qry2,false))#", right='true')>
<cfset valueEquals(left="#isQuery(deserializeJSON(qry2,true))#", right='false')>



<cfset qry2='{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]},"XYZ":1}'>
<cfset valueEquals(left="#isQuery(deserializeJSON(qry2,false))#", right='false')>
<cfset qry2='{"ROWCOUNT":2,"COLUMNS":1,"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}'>
<cfset valueEquals(left="#isQuery(deserializeJSON(qry2,false))#", right='false')>
<cfset qry2='{"ROWCOUNT":2,"COLUMNS":[[1,2],"BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}'>
<cfset valueEquals(left="#isQuery(deserializeJSON(qry2,false))#", right='false')>


<cfset q1='{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":[1.0,3.0],"bbb":[2.0,4.0]}}'>
<cfset q2='{"COLUMNS":["AAA","BBB"],"DATA":[[1.0,2.0],[3.0,4.0]]}'>
<cfset q3='{"susi":[[[{"COLUMNS":["AAA","BBB"],"DATA":[[1.0,2.0],[3.0,4.0]]}]]]}'>
<cfset q4='{"susi":[[[{"COLUMNS":["AAA","BBB"],"DATA":[[{"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":[1.0,3.0],"bbb":[{"COLUMNS":["AAA","BBB"],"DATA":[[1.0,2.0],[3.0,4.0]]},4.0]}},2.0],[3.0,4.0]]}]]]}'>



<cfsavecontent variable="str">
{"profile":{"identifier":"\\\/\/-//\'\'-''\"\"\t"}} 
</cfsavecontent>
<cfset sct=DeserializeJSON(str)>
<cfset valueEquals(left="#sct.profile.identifier#", right="\//-//''-''""""	")>

<cfsavecontent variable="str">
{"profile":{"identifier":"//-//''-''"}} 
</cfsavecontent>
<cfset sct=DeserializeJSON(str)>
<cfset valueEquals(left="#sct.profile.identifier#", right="//-//''-''")>

<cfsavecontent variable="str">
{"profile":{"identifier":'//-//"-"'}} 
</cfsavecontent>
<cfset sct=DeserializeJSON(str)>
<cfset valueEquals(left="#sct.profile.identifier#", right="//-//""-""")>



<cfsavecontent variable="content">
{"geo":null,"truncated":false,"source":"web","created_at":"Thu Oct 0101:02:51 +00002009","in_reply_to_status_id":null,"favorited":false,"user":{"profile_background_image_url":"http://s.twimg.com/a/1254344155/images/themes/theme6/bg.gif","description":null,"profile_link_color":"FF3300","followers_count":8,"url":null,"following":null,"profile_background_tile":false,"friends_count":3,"profile_background_color":"709397","verified":false,"time_zone":null,"created_at":"SatMay 16 23:52:45 +00002009","statuses_count":5,"favourites_count":0,"profile_sidebar_fill_color":"A0C5C7","profile_sidebar_border_color":"86A4A6","protected":false,"profile_image_url":"http://a1.twimg.com/profile_images/266659234/5776822-2_normal.jpg","notifications":null,"location":null,"name":"brandonsloan","screen_name":"crookedbrandon","id":40568411,"geo_enabled":false,"utc_offset":null,"profile_text_color":"333333"},"in_reply_to_user_id":null,"in_reply_to_screen_name":null,"id":4512512993,"text":"\u2656\u2655\u2654\u2660\u2666\u2663\u00bd\u00bc\u20ac\u203c\u266c*\u2661\u2765\u263a\u00ae\u00ae\u2716"}

</cfsavecontent>
<cfset data=deserializejson(content)>
<cfset str="">
<cfloop index="i" from="1" to="#len(data.text)#"><cfset
str&=asc(mid(data.text,i,1))></cfloop>
<cfset valueEquals(left="#str#", right="98149813981298249830982718918883648252983642982510085978617417410006")> 


<cfoutput>


<cfsavecontent variable="json">"<cfloop index="i" from="1" to="9814">\u#toHex(i)#</cfloop>"</cfsavecontent>
<cfsavecontent variable="str"><cfloop index="i" from="1" to="9814">#i#</cfloop></cfsavecontent>
</cfoutput>
<cfset valueEquals(left="#toAsc(deserializejson(json))#", right="#str#")>

<cfset str='"\u2765\u263a\u00ae\u00ae\u2716"'>
<cfset data=deserializejson(str)>

<!--- lucee only escapes when necessary --->
<cfset valueEquals(left="#toAsc(str)#", right="#toAsc(serializeJson(data,false,"us-ascii"))#")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cfscript>
private function toHex(nbr){
	var Integer=createObject('java','java.lang.Integer');
	var str=Integer.toHexString(nbr);
	while(len(str) LT 4)str=0&str;
	return str;
}
private function toAsc(str){
	var length=len(str);
	var res='';
	var i=1;
	for(;i<=length;i++){
		res&=asc(mid(str,i,1));
	}
	
	return res;
}
</cfscript>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>