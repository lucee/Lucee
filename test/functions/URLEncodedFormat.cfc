<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->

	<cffunction name="testURLEncodedFormatMember" localMode="modern">
		<cfset valueEquals(left="#" ".URLEncodedFormat()#", right="%20")>
		<cfset valueEquals(left="#" ".URLEncode()#", right="+")>
	</cffunction>

	<cffunction name="testURLEncodedFormat" localMode="modern">

<!--- begin old test code --->
<cfprocessingdirective pageencoding="UTF-8">
<cfset valueEquals(left="#URLEncode("123")#", right="123")>
<cfset valueEquals(left="#URLEncodedFormat("123")#", right="123")>
<cfset valueEquals(left="#URLEncodedFormat("abcDEF123")#", right="abcDEF123")>
<!---  ---><cfset valueEquals(left="#URLEncodedFormat(" ")#", right="%20")>

<cfset special=chr(246)&chr(228)&chr(252)&chr(233)&chr(224)&chr(232)>
<cfset plain=' 	+%&$,\|/:;=?@<>##{}()[]^`~-_.*''"#special#'>

<cfset encoded=URLEncodedFormat(plain)>
<cfset replain=URLDecode(encoded)>

<cfset valueEquals(left="#encoded#", right="%20%09%2B%25%26%24%2C%5C%7C%2F%3A%3B%3D%3F%40%3C%3E%23%7B%7D%28%29%5B%5D%5E%60%7E%2D%5F%2E%2A%27%22%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8")>
<cfset valueEquals(left="#plain#", right="#replain#")>
<cfset valueEquals(left="#plain#", right="#replain#")>

<cfset valueEquals(left="#URLEncodedFormat(' ')#", right="%20")>
<cfset valueEquals(left="#URLEncodedFormat(' ','iso-8859-1')#", right="%20")>
<cfset valueEquals(left="#URLEncodedFormat(' ','utf-8')#", right="%20")>

<cfset test=URLEncodedFormat(special)>
<cfset valueEquals(left="#test#", right="%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8")>
<cfset valueEquals(left="#URLDecode(test)#", right="#special#")>

<cfset test=URLEncodedFormat(special,'iso-8859-1')>
<cfset valueEquals(left="#test#", right="%F6%E4%FC%E9%E0%E8")>
<cfset valueEquals(left="#URLDecode(test,'iso-8859-1')#", right="#special#")>

<cfset test=URLEncodedFormat(special,'utf-8')>
<cfset valueEquals(left="#test#", right="%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8")>
<cfset valueEquals(left="#URLDecode(test,'utf-8')#", right="#special#")>

<cfset valueEquals(left="#URLEncodedFormat("123")#", right="123")>



<cfset valueEquals(left="#urlencodedformat('This is a test')#", right="This%20is%20a%20test")>
<cfset valueEquals(left="#urlencodedFormat(" ++--..__~*")#", right="%20%2B%2B%2D%2D%2E%2E%5F%5F%7E%2A")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
