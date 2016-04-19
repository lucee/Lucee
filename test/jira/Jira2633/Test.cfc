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
component {
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	function getSisterVariables(){
		return function(){
			
			rtn.a=function(){
				variables.jira2633 = "2633";
			};
			
			rtn.b=function(){
				if(isNull(variables.jira2633)) return "";
				else return variables.jira2633;
			};
			
			// local check
			rtn.a();
			rtn.c=rtn.b();
			structDelete(variables,"jira2633",false);
			return rtn;
		};
	}
	
	
	function getSisterThis(){
		return function(){
			
			rtn.a=function(){
				this.jira2633 = "2633";
			};
			
			rtn.b=function(){
				if(isNull(this.jira2633)) return "";
				else return this.jira2633;
			};
			
			// local check
			rtn.a();
			rtn.c=rtn.b();
			structDelete(this,"jira2633",false);
			return rtn;
		};
	}
	
	
	function getSisterUndefined(){
		return function(){
			
			rtn.a=function(){
				jira2633 = "2633";
			};
			
			rtn.b=function(){
				if(isNull(jira2633)) return "";
				else return jira2633;
			};
			
			// local check
			rtn.a();
			rtn.c=rtn.b();
			//structDelete(this,"jira2633",false);
			return rtn;
		};
	}
	
	
	
	
	
	
	
	
	
	public function getVariables(numeric level=1){
		if(level>2) throw "level is to big";
		if(level<1) throw "level is to small";
		variables.test="test->variables";
		
		if(level==2)
			return function (){
				return function (){
					if(!isNull(variables.test)) return variables.test;
					return "";
				};
			};
		
		return function (){
			if(!isNull(variables.test)) return variables.test;
			return "";
		};
	}
	public function getThis(numeric level=1){
		if(level>2) throw "level is to big";
		if(level<1) throw "level is to small";
		this.test="test->this";
		arguments.test="test->arguments";
		local.test="test->local";
		
		
		if(level==2)
			return function (){
				return function (){
					if(!isNull(this.test)) return this.test;
					return "";
				};
			};
		
		
		return function (){
			if(!isNull(this.test)) return this.test;
			return "";
		}
	}
	public function getUndefined(numeric level=1){
		if(level>2) throw "level is to big";
		if(level<1) throw "level is to small";
		variables.test="test->variables";
		this.test="test->this";
		arguments.test="test->arguments";
		local.test="test->local";
		
		
		if(level==2)
			return function (){
				return function (){
					if(!isNull(test)) return test;
					return "";
				};
			};
		
		
		
		
		return function (){
			if(!isNull(test)) return test;
			
			return "";
		}
	}
} 
</cfscript>