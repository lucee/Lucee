<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			dummyFunction(1);
		}

		function run(){
			describe( title="Test cases for LDEV-1139", body=function(){
				it(title="Checking cookie with parenthesis", body=function(){
					var result = dummyFunction(2);
					expect(result).toBe("true,true|true,true");
				});
			});
		}
	</cfscript>

	<cffunction name="dummyFunction" access="private" returntype="Any">
		<cfargument name="reqType" type="numeric" default="1" required="false">

		<cfif arguments.reqType EQ 1>
			<script type="text/javascript">
				document.cookie = "user=FOO(abc); expires=Thu, 18 Dec 2017 12:00:00 UTC; path=<cfoutput>#CGI.SCRIPT_NAME#</cfoutput>";
				document.cookie = "username=FOO%28abc%29; expires=Thu, 18 Dec 2017 12:00:00 UTC; path=<cfoutput>#CGI.SCRIPT_NAME#</cfoutput>";
			</script>
		<cfelse>
			<cfset Test = GetHttpRequestData().headers.cookie>
			<cfloop list="user,username" index="currCookieName">
				<cfset tmpStart = findNoCase("#currCookieName#=", Test)+len(currCookieName)+1 />
				<cfset tmpEnd = findNoCase(";", Test, findNoCase("#currCookieName#=", Test)+len(currCookieName)) />
				<cfset tmpCnt = tmpEnd EQ 0 ? len(Test) - tmpStart : tmpEnd - tmpStart>
				<cfset CookieRead[currCookieName] = mid(Test, tmpStart, tmpCnt)>
			</cfloop>
			<cfset result = "">
			<cfloop list="user,username" index="currCookieName">
				<cfset tempResult ="">
				<cfif currCookieName EQ "user">
					<cfset tempResult = listAppend(tempResult, cookie[currCookieName] EQ "FOO(abc)")>
					<cfset tempResult = listAppend(tempResult, cookieRead[currCookieName] EQ "FOO(abc)")>
				<cfelseif currCookieName EQ "username">
					<cfset tempResult = listAppend(tempResult, cookie[currCookieName] EQ "FOO%28abc%29")>
					<cfset tempResult = listAppend(tempResult, cookieRead[currCookieName] EQ "FOO%28abc%29")>
				</cfif>
				<cfset result = listAppend(result, tempResult, "|")>
			</cfloop>
			<cfreturn result>
		</cfif>
	</cffunction>
</cfcomponent>