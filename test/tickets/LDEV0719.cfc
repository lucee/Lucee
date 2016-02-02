/*
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase" {

 	public void function testPropery(){
    	var test=new LDEV0719.Test();

    	var props=getMetaData(test).properties;
    	
		assertEquals(4,props.len()); // we have 4 properties

		local.names="";
		local.propByName={};
		loop array=props item="local.el" {
			names=listAppend(names,el.name);
			propByName[el.name]=el;
		}
		names=listSort(names,"textNoCase");
		assertEquals("myProperty1,myProperty2,myProperty3,myProperty4",names);

		// property 1
		assertEquals(2,propByName.myProperty1.count());
		assertEquals("myProperty1",propByName.myProperty1.name);
		assertEquals("any",propByName.myProperty1.type);
 
		// property 2
		assertEquals(2,propByName.myProperty2.count());
		assertEquals("myProperty2",propByName.myProperty2.name);
		assertEquals("string",propByName.myProperty2.type);

		// property 3
		assertEquals(3,propByName.myProperty3.count());
		assertEquals("myProperty3",propByName.myProperty3.name);
		assertEquals("any",propByName.myProperty3.type);
		assertEquals("",propByName.myProperty3.inject);

		// property 4
		assertEquals(3,propByName.myProperty4.count());
		assertEquals("myProperty4",propByName.myProperty4.name);
		assertEquals("any",propByName.myProperty4.type);
		assertEquals("myService",propByName.myProperty4.inject);
    }
 }