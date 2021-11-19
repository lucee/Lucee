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
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function test(){
		var t=structnew('linked');
		t["1"]="1";
		t["5"]="1";
		t["10"]="1";
		data=setOut();
		try{
			writedump(var:t,format:'text',output:'console');
		}
		finally {
			local.content=trim(getOut(data));
		}
		
		assertEquals("Struct(ordered)1string15string110string1",replace(replace(replace(replace(content,chr(13),'','all'),chr(10),'','all'),'	','','all'),' ','','all'));
		/*
		try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}
	
	
	function setup(){
		dir=expandPath("{temp-directory}");
	}
	
	private function setOut(){
		var System=createObject('java','java.lang.System');
		var out=System.out;
		var path=dir&"f"&gettickCount()&".txt";
		var fos=createObject('java','java.io.FileOutputStream').init(path);
		var ps=createObject('java','java.io.PrintStream').init(fos);
		System.setOut(ps);
		return {out:out,ps:ps,path=path};
	}
	
	private function getOut(struct data){
		var System=createObject('java','java.lang.System');
		System.setOut(data.out);
		data.ps.close();
		var c= fileread(data.path);
		filedelete(data.path);
		return c;
	}
} 
</cfscript>