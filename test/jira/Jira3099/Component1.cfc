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
component {

	variables.testValue = "Component1";

	public function testClosure(){
		
		local.com2 = new component2();
		local.com2.override = function() {			
			echo(serialize(listSort(structKeyList(this),"textnocase")));
			echo(serialize(listSort(structKeyList(variables.this),"textnocase")));
		}		
		local.com2.override();
	}

	public function testUDF(){
		
		local.com2 = new component2();
		local.com2.override = susi;
		local.com2.override();
	}

	private function susi(){			
		echo(serialize(listSort(structKeyList(this),"textnocase")));
		echo(serialize(listSort(structKeyList(variables.this),"textnocase"))); 
	}
}