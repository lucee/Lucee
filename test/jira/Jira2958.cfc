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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="xml">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testXMLNodes">
		<cfxml variable="local.MyDoc">
		<MyDoc>this is a test<test>test 2</test>
		<myTag>
		<tagTest>tag</tagTest>
		</myTag>
		</MyDoc>
		</cfxml>

		<cfset assertEquals(5,arrayLen(MyDoc.Mydoc.xmlNodes))>

		<cfset assertEquals(
			'<?xmlversion=1.0encoding=utf-8?>thisisatest',
			_remove(MyDoc.Mydoc.xmlNodes[1]&"")
		)>
		<cfset assertEquals("this is a test",MyDoc.Mydoc.xmlNodes[1].xmlText)>

	</cffunction>

	<!---TODO <cffunction name="testXMLChildren">
		<cfxml variable="local.MyDoc">
		<MyDoc>this is a test<test>test 2</test>
		<myTag>
		<tagTest>tag</tagTest>
		</myTag>
		</MyDoc>
		</cfxml>

		<cfset assertEquals(2,arrayLen(MyDoc.Mydoc.xmlChildren))>

		<cfset assertEquals(
			'<?xmlversion=1.0encoding=utf-8?><test>test2</test>',
			_remove(MyDoc.Mydoc.xmlChildren[1]&"")
		)>

	</cffunction>--->

	<cfscript>
	function _remove(str) {
		str=trim(str);
		str=replace(str,'"','','all');
		str=replace(str,'''','','all');
		return REReplace(str, "[[:space:]]+", "", "ALL");
	}
	</cfscript>

</cfcomponent>