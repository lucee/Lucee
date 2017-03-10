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

<cffunction name="testEncryptMember" localMode="modern">

<cfset assertEquals(
					"hallo welt",
					trim(decrypt("hallo welt".encrypt("stringkey"),"stringkey"))
		)>
	
</cffunction>

	<cffunction name="testEncrypt" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(
	left="#trim(decrypt(encrypt("hallo welt","stringkey"),"stringkey"))#" 
	,right="hallo welt")>
	

	<cfset key=generateSecretKey("AES")>
<cfset valueEquals(
		left="#trim(decrypt(encrypt("hallo welt",key,"AES"),key,"AES"))#" 
		,right="hallo welt")>

	<cfset key=generateSecretKey("BLOWFISH")>
<cfset valueEquals(
		left="#trim(decrypt(encrypt("hallo welt",key,"BLOWFISH"),key,"BLOWFISH"))#" 
		,right="hallo welt")>

	<cfset key=generateSecretKey("DES")>
<cfset valueEquals(
		left="#trim(decrypt(encrypt("hallo welt",key,"DES"),key,"DES"))#" 
		,right="hallo welt")>

	<cfset key=generateSecretKey("DESEDE")>
<cfset valueEquals(
		left="#trim(decrypt(encrypt("hallo welt",key,"DESEDE"),key,"DESEDE"))#" 
		,right="hallo welt")>
	
	<cftry>
	<cfset key="susi">
<cfset valueEquals(
		left="#trim(decrypt(encrypt("hallo welt",key,"AES"),key,"AES"))#" 
		,right="hallo welt")>
		<cfset fail("must throw:The key specified is not a valid key for this encryption: Invalid AES key length: 24.")>
		<cfcatch></cfcatch>
	</cftry>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>

	<cfscript>
	
	public void function testRC4(){
		var algo="RC4";
		var value="554122";
		var key=GenerateSecretKey(algo);
		var enc=Encrypt(value, key, algo);
		var dec=Decrypt(enc, key, algo);
		assertEquals(value,dec);
	}

	public void function testRC42(){
		var algo="RC4";
		var value="554122";
		var key="test";
		var enc=Encrypt(value, key, algo);
		var dec=Decrypt(enc, key, algo);
		assertEquals(value,dec);
	}
</cfscript>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>