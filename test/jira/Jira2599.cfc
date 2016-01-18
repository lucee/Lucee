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

	public void function testIdConversion(){
		if(!isNull(request.testMongoDBExtension) && request.testMongoDBExtension) {
			content = {'name':'Susi'};
			mongo = MongoDBConnect("test");
			mongo['test'].insert(content);
			
			
			//db.test2.insert({susi:"Sorglos"});

			
			//Get by Name
			var id = mongo['test'].findOne({'name':'Susi'}, {'_id':1});
			
			//Get by Id : fails
			var byid = mongo['test'].findOne({'_id':id});
		}
		
	}
} 
</cfscript>