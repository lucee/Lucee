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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi">

	<cffunction name="testEncodeForCSS">
		<cfscript>
		var enc=EncodeForCSS('<script>');
		// Esapi adds a space after the hex code and owasp does not, both are valid css encodings, so we remove spaces before comparing
		assertEquals('\3cscript\3e',replace(enc," ","","all"));
		</cfscript>
	</cffunction>
	<cffunction name="testEncodeForCSSMember">
		<cfscript>
		var enc='<script>'.encodeForCSS();
		// Esapi adds a space after the hex code and owasp does not, both are valid css encodings, so we remove spaces before comparing
		assertEquals('\3cscript\3e',replace(enc," ","","all"));
		</cfscript>
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>