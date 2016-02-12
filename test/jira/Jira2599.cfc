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

	// skip closure
	function isNotSupported() {
		variables.mongodb=getCredentials();
		if(!isNull(variables.mongodb.host)) {
			variables.supported=true;
		}
		else 
			variables.supported=false;

		return !variables.supported;
	}

	private struct function getCredentials() {
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
	
	// public function setUp(){}

	//public function beforeTests(){}
	
	//public function afterTests(){}
	

	public void function testConnectByArgs() skip="isNotSupported" {
		mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		assertEquals("test",mongo.getName());
	}

	public void function testConnectByURI() skip="isNotSupported" {
		mongo = MongoDBConnect("test","mongodb://#variables.mongoDB.host#:#variables.mongoDB.port#");
		assertEquals("test",mongo.getName());
	}

	// skip until authenticate is implemented
	public void function testAuthenticate() skip="true" {
		mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		mongo.authenticate(variables.mongoDB.user,variables.mongoDB.pass);
	}

	public void function testIdConversion() skip="isNotSupported" {
		content = {'name':'Susi'};
		mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		mongo.getCollection("test").insert(content);
		
		//Get by Name
		var id = mongo['test'].findOne({'name':'Susi'});
		assertEquals("Susi",id.name)
		
		//Get by Id : fails
		var byid = mongo['test'].findOne({'_id':id});
		assertEquals(isNull(byid),true)
	}

	public void function testMongoDBID() skip="isNotSupported" {
		var id = MongoDBID();
		$assert.key(id,"date");
		$assert.key(id,"timestamp");
		$assert.key(id,"id");

		var dateSeed = now().add("d",-1)
		id = MongoDBID(dateSeed);
		$assert.isEqual(dateSeed,id.getDate());
	
		var idSeed = "56be2538ddd75f08acde1e46";
		id = MongoDBID(idSeed);
		$assert.isEqual(idSeed,id.toString());
	}

	public void function testInsertAndFind() skip="isNotSupported" {
		var mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		var coll = mongo.getCollection("test");
		var docs = [
			 {"_id":1, "name":"One"}
			,{"_id":2, "name":"Two"}
			,{"_id":3, "name":"Three"}
		]

		// clear out collection, verify empty
		coll.drop();
		$assert.null(coll.findOne());

		// insert docs, verify count
		coll.insert(docs);
		$assert.isEqual( 3, coll.count() );

		// find a doc, test cursor methods
		var docsFound = coll.find({"name":"One"});
		$assert.isTrue( docsFound.hasNext() );
		$assert.isEqual( 1, docsFound.size() );
		$assert.typeOf( "struct", docsFound.next() );
		$assert.isFalse( docsFound.hasNext() );

		// find with limit and sort
		docsFound = coll.find().sort({"_id":-1}).limit(2);
		$assert.isEqual( 2, docsFound.size() );
		$assert.isEqual( 3, docsFound.count() );
		$assert.isEqual( "Three", docsFound.next().name );
	}

	public void function testUpdate() skip="isNotSupported" {
		var mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		var coll = mongo.getCollection("test");
		var docs = [
			 {"_id":1, "name":"One"}
			,{"_id":2, "name":"Two"}
			,{"_id":3, "name":"Three"}
			,{"_id":4, "name":"Four"}
			,{"_id":5, "name":"Five"}
		]

		coll.drop();
		coll.insert(docs);

		// single update with criteria
		coll.update({"_id":1}, {"$set":{"updated":true}});
		$assert.isTrue( coll.findOne({"_id":1}).updated );

		// reset data
		coll.drop();
		coll.insert(docs);

		// single update, no criteria 
		coll.update({},{"$set":{"updated":true}});
		$assert.isEqual(1, coll.find({"updated":true}).size());

		// reset data
		coll.drop();
		coll.insert(docs);

		// multi update, no criteria 
		coll.update({},{"$set":{"updated":true}},false,true);
		$assert.isEqual(5, coll.find({"updated":true}).size());
	}

}
</cfscript>