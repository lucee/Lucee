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
		if(!isNull(variables.mongodb.server)) {
			variables.supported=true;
		}
		else
			variables.supported=false;

		return !variables.supported;
	}

	private struct function getCredentials() {
		// getting the credetials from the enviroment variables
		return server.getDatasource("mongoDB");
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

	public function beforeTests() skip="isNotSupported" {
		if(isNotSupported()) return;
		var uri = "mongodb://#variables.mongoDB.server#:#variables.mongoDB.port#";
		if (!isempty(variables.mongoDB.username) && !isEmpty(variables.mongoDB.password))
			uri = "mongodb://#variables.mongoDB.username#:#variables.mongoDB.password#@#variables.mongoDB.server#:#variables.mongoDB.port#";

		//systemOutput("MongoDB URI: " & uri, true, true);

		// test host/port via http, should return: "It looks like you are trying to access MongoDB over HTTP on the native driver port."
		/*
		try {
			http method="GET" url="http://#variables.mongoDB.server#:#variables.mongoDB.port#/#variables.mongoDB.db#" result="local.httpRes";
			systemOutput("OOO#chr(10)#" & httpRes.fileContent, true, true);
		}
		catch (ex){
			systemOutput("XXX#chr(10)#" & ex.toString(), true, true);
		} //*/

		db = MongoDBConnect(variables.mongoDB.db, uri);
 	}

	//public function afterTests(){}


	public void function testConnectByArgs() skip="isNotSupported" {
		if(isNotSupported()) return;
		var mongo = MongoDBConnect(variables.mongoDB.db, variables.mongoDB.server, variables.mongoDB.port);
		assertEquals(variables.mongoDB.db, mongo.getName());
	}

	public void function testConnectByURI() skip="isNotSupported" {
		if(isNotSupported()) return;
		var uri = "mongodb://#variables.mongoDB.server#:#variables.mongoDB.port#";
		if (!isempty(variables.mongoDB.username) && !isEmpty(variables.mongoDB.password))
			uri = "mongodb://#variables.mongoDB.username#:#variables.mongoDB.password#@#variables.mongoDB.server#:#variables.mongoDB.port#";

		var mongo = MongoDBConnect(variables.mongoDB.db, uri);
 		assertEquals(variables.mongoDB.db, mongo.getName());
	}

	// skip until authenticate is implemented
	public void function testAuthenticate() skip="true" {
		if(isNotSupported()) return;
		var mongo = MongoDBConnect(variables.mongoDB.db, variables.mongoDB.server, variables.mongoDB.port);
		mongo.authenticate(variables.mongoDB.username, variables.mongoDB.password);
	}

	public void function testIdConversion() skip="isNotSupported" {
		if(isNotSupported()) return;
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
		if(isNotSupported()) return;
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
		if(isNotSupported()) return;
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
		if(isNotSupported()) return;
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
		if(isNotSupported()) return;
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

	public void function testAggregateResults() skip="true" { // TODO broken https://luceeserver.atlassian.net/browse/LDEV-3432
		if(isNotSupported()) return;
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
		if(isNotSupported()) return;
		var coll = resetTestCollection();

		// aggregate with array of pipeline operations as first argument with struct options as second argument returns Cursor
		var results = coll.aggregate([{"$group":{"_id":"$grp", "vals":{"$push":"$name"}}},{"$sort":{"_id":1}}],{});
		$assert.isTrue( results.hasNext() );
		$assert.lengthOf( results.next().vals, 3 );
	}

	public void function testWriteConcern() skip="isNotSupported" {
		if(isNotSupported()) return;
		var coll = resetTestCollection();

		coll.setWriteConcern("UNACKNOWLEDGED");

		var wc = coll.getWriteConcern();
		$assert.isFalse(wc.isAcknowledged());
	}

	public void function testIndexing() skip="isNotSupported" {
		if(isNotSupported()) return;
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
		if(isNotSupported()) return;
		var coll = resetTestCollection();

		$assert.typeOf("struct", coll.stats());
		$assert.typeOf("numeric", coll.dataSize());
		$assert.typeOf("numeric", coll.storageSize());
	}

	public void function testGroupAndDistinct() skip="isNotSupported" {
		if(isNotSupported()) return;
		var coll = resetTestCollection();
		$assert.isEqual(2, coll.distinct("grp").len());

		// group is not implemented yet!
	}

	public void function testMapReduce() skip="true" { // TODO broken https://luceeserver.atlassian.net/browse/LDEV-3432
		if(isNotSupported()) return;
		var coll = resetTestCollection();
		var fMap = "function() {
				var output = {id:this._id, name:this.name};
				emit(this._id,output);
			}";

		var fRed = "function(key, values) {
				var outs = {name:null };
				values.forEach(function(v) {
					if(outs.name===null) {
						outs.name = v.name;
					}
				});
				return outs;
			}";

		coll.mapReduce(fMap, fRed, "testmapreduce", {});
		$assert.isEqual(5, db.getCollection("testmapreduce").count());
	}

	public void function testRename() skip="isNotSupported" {
		if(isNotSupported()) return;

		try {
			if ( structKeyExists(db, "test2") )
				db["test2"].drop(); // avoid collection exists error if test fails
		} catch (e) {};
		var coll = resetTestCollection();
		coll.rename("test2");
		$assert.isEqual(5, db["test2"].count());

		db["test2"].drop();
	}

	private void function testCacheAsScope() skip="isNotSupported" {
		local.id=createUniqueId();
		local.uri=createURI("mongodb/index.cfm");

		// on the first request everything is equal
		local.result=_InternalRequest(template:uri,urls:{appName:id},addtoken:true);
		local.sct=evaluate(result.filecontent);
		loop list="client,session" item="scp" {
			assertEquals(sct[scp].lastvisit&"",sct[scp].timecreated&"");
		}

		sleep(1000);

		// on the second request time is different
		local.result=_InternalRequest(template:uri,urls:{appName:id},addtoken:true);
		local.sct=evaluate(result.filecontent);
		loop list="client,session" item="scp" {
			assertEquals(sct[scp].lastvisit&"",sct[scp].timecreated&"");
		}

		sleep(1000);

		// on the third everything is different
		local.result=_InternalRequest(template:uri,urls:{appName:id},addtoken:true);
		local.sct=evaluate(result.filecontent);
		loop list="client,session" item="scp" {
			assertNotEquals(sct[scp].lastvisit&"",sct[scp].timecreated&"");
		}
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}


}
</cfscript>