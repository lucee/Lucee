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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	/**
	@comment_annotation 1
	*/
	private function annotationtest1() inline_annotation=1 {}

	/**
	@comment_annotation 1
	*/
	private function annotationtest2() inline_annotation=1 {}
	
	

	public void function testAnnotations(){
		var udfs=getMetaData(this).functions;
		loop array="#udfs#" item="local.udf" {
			if(!find('annotationtest',udf.name)) continue;
			assertEquals(true,isDefined('udf.inline_annotation'));
			assertEquals(true,isDefined('udf.comment_annotation'));
		}
	}
} 
</cfscript>