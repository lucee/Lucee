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
	
	variables.suffix="Query";

	
	public void function testCFML2JS() localmode=true {
		sct={string:'Susi',random:createObject('java','java.util.Random').init()};
		wddx topLevelVariable="susi" action="cfml2js" input="#sct#" output="res";
		assertEquals('susi=new Object();susi["random"]=null;susi["string"]="Susi";',res);
	}

	public void function testCFML2WDDX() localmode=true {
		sct={string:'Susi',random:createObject('java','java.util.Random').init()};
		wddx topLevelVariable="susi" action="cfml2js" input="#sct#" output="res";
		assertEquals(
			"<wddxPacket version='1.0'><header/><data><struct><var name='RANDOM'><struct type='Ljava.util.Random;'></struct></var><var name='STRING'><string>Susi</string></var></struct></data></wddxPacket>"
			,res);
	}


	// TODO add testcases for missing actions
} 
</cfscript>