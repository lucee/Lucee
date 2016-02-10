<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var mongoDB={};
		if(!isNull(server.system.environment.MONGODB_HOST) && !isNull(server.system.environment.MONGODB_PORT) && !isNull(server.system.environment.MONGODB_USERNAME) && !isNull(server.system.environment.MONGODB_PASSWORD)) {
			mongoDB.host=server.system.environment.MONGODB_HOST;
			mongoDB.port=server.system.environment.MONGODB_PORT;
			mongoDB.user=server.system.environment.MONGODB_USERNAME;
			mongoDB.pass=server.system.environment.MONGODB_PASSWORD;
		}
		// getting the credetials from the system variables
		else if(!isNull(server.system.properties.MONGODB_HOST) && !isNull(server.system.properties.MONGODB_PORT) && !isNull(server.system.properties.MONGODB_USERNAME) && !isNull(server.system.properties.MONGODB_PASSWORD)) {
			mongoDB.host=server.system.properties.MONGODB_HOST;
			mongoDB.port=server.system.properties.MONGODB_PORT;
			mongoDB.user=server.system.properties.MONGODB_USERNAME;
			mongoDB.pass=server.system.properties.MONGODB_PASSWORD;
		}
		return mongoDB;
	}
	
	public function setUp(){
		variables.mongodb=getCredencials();
		if(!isNull(variables.mongodb.host)) {
			variables.supported=true;
		}
		else 
			variables.supported=false;
	}

	//public function beforeTests(){}
	
	//public function afterTests(){}
	

	public void function testIdConversion(){
		if(variables.supported) {
			content = {'name':'Susi'};
			mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
			mongo.authenticate(variables.mongoDB.user,variables.mongoDB.pass);

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