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

	private any function resetTestCollection() {
		var coll = db.getCollection("test");
		var docs = [
			 {"_id":1, "grp":1, "name":"One"}
			,{"_id":2, "grp":1, "name":"Two"}
			,{"_id":3, "grp":1, "name":"Three"}
			,{"_id":4, "grp":2, "name":"Four"}
			,{"_id":5, "grp":2, "name":"Five"}
		];

		coll.drop();
		coll.insert(docs);

		return coll;		
	}
	
	// public function setUp(){}

	public function beforeTests() {
		db = MongoDBConnect("test","mongodb://#variables.mongoDB.user#:#variables.mongoDB.pass#@#variables.mongoDB.host#:#variables.mongoDB.port#");
	}
	
	//public function afterTests(){}
	

	public void function testConnectByArgs() skip="isNotSupported" {
		var mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		assertEquals("test",mongo.getName());
	}

	public void function testConnectByURI() skip="isNotSupported" {
		var mongo = MongoDBConnect("test","mongodb://#variables.mongoDB.user#:#variables.mongoDB.pass#@#variables.mongoDB.host#:#variables.mongoDB.port#");
		assertEquals("test",mongo.getName());
	}

	// skip until authenticate is implemented
	public void function testAuthenticate() skip="true" {
		var mongo = MongoDBConnect("test",variables.mongoDB.host,variables.mongoDB.port);
		mongo.authenticate(variables.mongoDB.user,variables.mongoDB.pass);
	}

	public void function testIdConversion() skip="isNotSupported" {
		var content = {'name':'Susi'};
		db.getCollection("test").insert(content);
		
		//Get by Name
		var id = db['test'].findOne({'name':'Susi'});
		assertEquals("Susi",id.name)
		
		//Get by Id : fails
		var byid = db['test'].findOne({'_id':id});
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
		var coll = db.getCollection("test");
		var docs = [
			 {"_id":1, "name":"One"}
			,{"_id":2, "name":"Two"}
			,{"_id":3, "name":"Three"}
		]

		var moreDocs = [
			 {"_id":4, "name":"Four"}
			,{"_id":5, "name":"Five"}
			,{"_id":6, "name":"Six"}
		]

		// clear out collection, verify empty
		coll.drop();
		$assert.null(coll.findOne());

		// insert docs, verify count
		coll.insert(docs);
		$assert.isEqual( 3, coll.count() );

		coll.insertMany(moreDocs);

		// find a doc, test cursor methods
		var docsFound = coll.find({"name":"One"});
		$assert.isTrue( docsFound.hasNext() );
		$assert.isEqual( 1, docsFound.size() );
		$assert.typeOf( "struct", docsFound.next() );
		$assert.isFalse( docsFound.hasNext() );

		// find with limit and sort
		docsFound = coll.find().sort({"_id":-1}).limit(2);
		$assert.isEqual( 2, docsFound.size() );
		$assert.isEqual( 6, docsFound.count() );
		$assert.isEqual( "Six", docsFound.next().name );
	}

	public void function testUpdate() skip="isNotSupported" {
		var coll = resetTestCollection();

		// single update with criteria
		coll.update({"_id":1}, {"$set":{"updated":true}});
		$assert.isTrue( coll.findOne({"_id":1}).updated );

		// reset data
		coll = resetTestCollection();

		// single update, no criteria 
		coll.update({},{"$set":{"updated":true}});
		$assert.isEqual(1, coll.find({"updated":true}).size());

		// reset data
		coll = resetTestCollection();

		// multi update, no criteria 
		coll.update({},{"$set":{"updated":true}},false,true);
		$assert.isEqual(5, coll.find({"updated":true}).size());
	
		// find and modify
		var doc = coll.findAndModify({"_id":1},{"$set":{"modified":true}});
		$assert.isEqual(1, coll.find({"modified":true}).size());
		$assert.isEqual(1, doc._id);
	}

	public void function testRemove() skip="isNotSupported" {
		var coll = resetTestCollection();

		// remove 1 doc
		coll.remove({"_id":1});
		$assert.isEqual( 4, coll.count() );

		// find and remove 1 doc
		var doc = coll.findAndRemove({"_id":2});
		$assert.isEqual( 2, doc._id );
		$assert.isEqual( 3, coll.count() );

		// remove all docs
		coll.remove({});
		$assert.isEqual( 0, coll.count() );
	}

	public void function testAggregateResults() skip="isNotSupported" {
		var coll = resetTestCollection();

		// aggregate with N... structs as arguments returns AggregationResult
		var results = coll.aggregate({"$group":{"_id":"$grp", "vals":{"$push":"$name"}}});
		$assert.typeOf( "array", results.results() );
		$assert.lengthOf( results.results(), 2 );

		// aggregate with array of pipeline operations as single argument returns AggregationResult
		var results = coll.aggregate([{"$group":{"_id":"$grp", "vals":{"$push":"$name"}}}]);
		$assert.typeOf( "array", results.results() );
		$assert.lengthOf( results.results(), 2 );
	}

	public void function testAggregateCursor() skip="isNotSupported" {
		var coll = resetTestCollection();

		// aggregate with array of pipeline operations as first argument with struct options as second argument returns Cursor
		var results = coll.aggregate([{"$group":{"_id":"$grp", "vals":{"$push":"$name"}}},{"$sort":{"_id":1}}],{});
		$assert.isTrue( results.hasNext() );
		$assert.lengthOf( results.next().vals, 3 );
	}

	public void function testWriteConcern() skip="isNotSupported" {
		var coll = resetTestCollection();

		coll.setWriteConcern("UNACKNOWLEDGED");

		var wc = coll.getWriteConcern();
		$assert.isFalse(wc.isAcknowledged());
	}

	public void function testIndexing() skip="isNotSupported" {
		var coll = resetTestCollection();

		// get indexes
		var idx = coll.getIndexes();
		$assert.typeOf("array",idx);

		// create indexes
		coll.createIndex("grp");
		coll.createIndex({"name":1},{"name":"name"});
		idx = coll.getIndexes();
		$assert.lengthOf(idx, 3);

		// index size
		var idxSize = coll.totalIndexSize();
		$assert.typeOf("numeric", idxSize);

		// drop index by name
		coll.dropIndex("name");
		idx = coll.getIndexes();
		$assert.lengthOf(idx,2); // only _id + grp indexes should remain after dropIndex('name');

		// drop all indexes
		coll.dropIndexes();
		idx = coll.getIndexes();
		$assert.lengthOf(idx,1); // only _id index should remain after dropIndexes();
	}

	public void function testCollectionUtils() skip="isNotSupported" {
		var coll = resetTestCollection();

		$assert.typeOf("struct", coll.stats());
		$assert.typeOf("numeric", coll.dataSize());		
		$assert.typeOf("numeric", coll.storageSize());		
	}

	public void function testGroupAndDistinct() skip="isNotSupported" {
		var coll = resetTestCollection();
		$assert.isEqual(2, coll.distinct("grp").len());

		// group is not implemented yet!		
	}

	public void function testMapReduce() skip="isNotSupported" {
		var coll = resetTestCollection();
		var fMap = "
			function(){
				var output = { id:this._id, name:this.name };
				emit(this._id,output);
			}		
		";

		var fRed = "
			function(key, values) {
				var outs = { name:null };
				values.forEach(function(v){
					if (outs.name===null) {
						outs.name = v.name;
					}
				});
				return outs;
			};
		"

		coll.mapReduce(fMap, fRed, "testmapreduce", {});
		$assert.isEqual(5, db.getCollection("testmapreduce").count());
	}

	public void function testRename() skip="isNotSupported" {
		var coll = resetTestCollection();
		coll.rename("test2");
		$assert.isEqual(5, db["test2"].count());

		db["test2"].drop();				
	}
}
</cfscript>