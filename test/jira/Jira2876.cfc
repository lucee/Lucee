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
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	

	public function testNoFilter() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="";
		assert(qDir.recordCount == 10);
	}
	

	public function testSuffix() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="*.js";
		assert(qDir.recordCount == 1);
	}


	public function testContains() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="*json*";
		assert(qDir.recordCount == 2);
	}


	public function testMultiA() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="testa*2013*.txt";
		assert(qDir.recordCount == 3);
	}


	public function testMultiB() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="test?*2014*.txt";
		assert(qDir.recordCount == 5);
	}


	public function testOneA() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="test?-20140201.txt";
		assert(qDir.recordCount == 2);
	}


	public function testOneB() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="testa-2013123?.txt";
		assert(qDir.recordCount == 2);
	}


	public function testPrefix() {

		directory name="local.qDir" directory=getTestDir() recurse=true filter="testb*";
		assert(qDir.recordCount == 1);
	}


	function getTestDir() {

		var result = GetCurrentTemplatePath();

		result = left(result, len(result) - 4);

		return result;
	}

}