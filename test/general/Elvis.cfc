<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

	public void function testVarNotExisting(){
		assertEquals('NotExisting',ljkl.jljl.ghu?:'NotExisting');
		assertEquals('NotExisting',ljklkju?:'NotExisting');
	}

	public void function testVarExisting(){
		var a.b.c.d="Existing";
		assertEquals('Existing',a.b.c.d?:'NotExisting');
		assertTrue(isStruct(a?:'NotExisting'));
	}


	public void function testFuncNotExisting(){
		assertEquals('NotExisting',ljkl.jljl.ghu()?:'NotExisting');
		assertEquals('NotExisting',ljklkju()?:'NotExisting');
	}

	public void function testFuncExisting(){
		var a.b.c.d=testElvis;
		assertEquals('Existing',a.b.c.d()?:'NotExisting');
		assertEquals('Existing',testElvis()?:'NotExisting');
	}

	public void function testFuncExistingRtnNull1(){
		var a.b.c.d=rtnNull;
		assertEquals('NotExisting',a.b.c.d()?:'NotExisting');
	}

	public void function testFuncExistingRtnNull2(){
		assertEquals('NotExisting',rtnNull()?:'NotExisting');
	}

	public void function testFuncExistingRtnNull3(){
		var a.b.c.d=rtnNull;
		assertEquals('NotExisting',a.b.c.d(a:1)?:'NotExisting');
	}

	public void function testFuncExistingRtnNull4(){
		assertEquals('NotExisting',rtnNull(a:1)?:'NotExisting');
	}

	public void function testSaveNavOp() localmode=true {
		assertTrue(isNull(myvar?.firstlevel()));
		assertTrue(isNull(myvar?.firstlevel?.nextlevel()));
		assertTrue(isNull(myvar?.firstlevel?.nextlevel?.udf()));

		x=myvar?.firstlevel();
		assertTrue(isNull(x));
		x=myvar?.firstlevel?.nextlevel();
		assertTrue(isNull(x));
		x=myvar?.firstlevel?.nextlevel?.udf();
		assertTrue(isNull(x));

		x=myvar?.firstlevel;
		assertTrue(isNull(x));
		x=myvar?.firstlevel?.nextlevel;
		assertTrue(isNull(x));
		x=myvar?.firstlevel?.nextlevel?.udf;
		assertTrue(isNull(x));
	}


	private function testElvis(){		
		return "Existing";
	}
	private function rtnNull(){	
	}

} 
</cfscript>