/**
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
 **/


component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	public function setUp(){
		variables.arr=['Text 1','Text 2','Text 3','Text 4','Text 5'];
		
		variables.strXML = '<Items>
		<item id="1">Text 1</item>
		<item id="2">Text 2</item>
		<item id="3">Text 3</item>
		<item id="4">Text 4</item>
		<item id="5">Text 5</item>
		</Items>';
	}
	
	

	
	public void function testRegularArray(){
		var arr=duplicate(variables.arr);
		ArraySwap(arr,5,4);
		assertEquals(arrayToList(arr),"Text 1,Text 2,Text 3,Text 5,Text 4");
		
		var arr=duplicate(variables.arr);
		ArraySwap(arr,4,5);
		assertEquals(arrayToList(arr),"Text 1,Text 2,Text 3,Text 5,Text 4");
	}
	
	public void function testXMLNodes(){
		// 2,1
		var xml = xmlParse(strXML);
		var nodes=xml.xmlRoot.xmlChildren;
		ArraySwap(nodes,2,1);
		assertEquals(nodesToList(nodes),'Text 2,Text 1,Text 3,Text 4,Text 5');
		
		// 3,2
		xml = xmlParse(strXML);
		nodes=xml.xmlRoot.xmlChildren;
		ArraySwap(nodes,3,2);
		assertEquals(nodesToList(nodes),'Text 1,Text 3,Text 2,Text 4,Text 5');
		
		// 4,3
		xml = xmlParse(strXML);
		nodes=xml.xmlRoot.xmlChildren;
		ArraySwap(nodes,4,3);
		assertEquals(nodesToList(nodes),'Text 1,Text 2,Text 4,Text 3,Text 5');
		
		// 5,4
		xml = xmlParse(strXML);
		nodes=xml.xmlRoot.xmlChildren;
		ArraySwap(nodes,5,4);
		assertEquals(nodesToList(nodes),'Text 1,Text 2,Text 3,Text 5,Text 4');
		
		// 4,5
		xml = xmlParse(strXML);
		nodes=xml.xmlRoot.xmlChildren;
		ArraySwap(nodes,4,5);
		assertEquals(nodesToList(nodes),'Text 1,Text 2,Text 3,Text 5,Text 4');

		
		
	}
	
	
	
	private function nodesToArray(xml nodes){
		var rtn=[];
		loop array="#nodes#" item="local.node" {
			rtn.append(node.xmlText);
		}
		return rtn;
	}
	private function nodesToList(xml nodes){
		return arrayToList(nodesToArray(nodes));
	}
}