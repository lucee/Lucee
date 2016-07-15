<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
		sct=structNew('ordered');
		sct.string='Susi';
		sct.random=createObject('java','java.util.Random').init();

	public void function testWDDX(){
		wddx action="cfml2wddx" input=sct output="local.res";
		
		assertEquals(
			"<wddxPacket version='1.0'><header/><data><struct><var name='STRING'><string>Susi</string></var><var name='RANDOM'><struct type='Ljava.util.Random;'></struct></var></struct></data></wddxPacket>"
			,res
			);
	}

	public void function testJS(){
		wddx action="cfml2js" input=sct output="local.res" TOPLEVELVARIABLE="a";
		
		assertEquals(
			'a=new Object();a["string"]="Susi";a["random"]=null;'
			,res
			);
	}

} 
</cfscript>