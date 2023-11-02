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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
	private function removeSpace(str){
		return replace(replace(replace(replace(str,'	','','all'),' ','','all'),chr(10),'','all'),chr(13),'','all');
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
	</cfscript>
	
	
	<cffunction name="testCallersLocalScope" output="yes">
		<cfset var callersLocalScope = "XcallersLocalScope" />
		
		<!--- callersLocalScope --->
		<cfset var data=setOut()>
		<cftry>
			<cfdump output="console" format="text" eval=callersLocalScope  />
			<cffinally>
					<cfset content=trim(getOut(data))>
			</cffinally>
		</cftry>
		<cfset assertEquals("callersLocalScopestringXcallersLocalScope",removeSpace(content))>
		
	</cffunction>
	
	<cffunction name="testLocalCallersLocalScope" output="yes">
		<cfset var callersLocalScope = "XcallersLocalScope" />
		
		<!--- local.callersLocalScope --->
		<cfset var data=setOut()>
		<cftry>
			<cfdump output="console" format="text" eval=local.callersLocalScope  />
			<cffinally>
					<cfset content=trim(getOut(data))>
			</cffinally>
		</cftry>
		<cfset assertEquals("local.callersLocalScopestringXcallersLocalScope",removeSpace(content))>
		
	</cffunction>
	
	<cffunction name="testCallersArgScope" output="yes">
		<cfargument name="callersArgScope" default="XcallersArgScope" />
		
		<!--- callersArgScope --->
		<cfset var data=setOut()>
		<cftry>
			<cfdump output="console" format="text" eval=callersArgScope  />
			<cffinally>
					<cfset content=trim(getOut(data))>
			</cffinally>
		</cftry>
		<cfset assertEquals("callersargscopestringxcallersargscope",removeSpace(content))>
	</cffunction>
	
	<cffunction name="testArgumentsCallersArgScope" output="yes">
		<cfargument name="callersArgScope" default="XcallersArgScope" />
		
		<!--- arguments.callersArgScope --->
		<cfset var data=setOut()>
		<cftry>
			<cfdump output="console" format="text" eval=arguments.callersArgScope  />
			<cffinally>
					<cfset content=trim(getOut(data))>
			</cffinally>
		</cftry>
		<cfset assertEquals("arguments.callersargscopestringxcallersargscope",removeSpace(content))>
	</cffunction>
	
	
	<cffunction name="testCallersVarScope" output="yes">
		<cfset variables.callersVarScope="XcallersVarScope">
		
		<!--- callersVarScope --->
		<cfset var data=setOut()>
		<cftry>
			<cfdump output="console" format="text" eval=callersVarScope  />
			<cffinally>
					<cfset content=trim(getOut(data))>
			</cffinally>
		</cftry>
		<cfset assertEquals("callersVarScopestringxcallersVarScope",removeSpace(content))>
	</cffunction>
	
	
	<cffunction name="testVariablesCallersVarScope" output="yes">
		<cfset variables.callersVarScope="XcallersVarScope">
		
		<!--- variables.callersVarScope --->
		<cfset var data=setOut()>
		<cftry>
			<cfdump output="console" format="text" eval=variables.callersVarScope  />
			<cffinally>
					<cfset content=trim(getOut(data))>
			</cffinally>
		</cftry>
		<cfset assertEquals("variables.callersVarScopestringxcallersVarScope",removeSpace(content))>
	</cffunction>
	
	
</cfcomponent>