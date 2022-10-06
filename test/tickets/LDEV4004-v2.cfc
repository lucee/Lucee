component extends="org.lucee.cfml.test.LuceeTestCase" skip=false labels="threads" {
	function setup(){
		variables.errors = [];
		variables.qColumnsCopy = evaluate( 'query(
			"COLUMN_NAME":["itemID","categoryID","shortDesc","longDesc","num","let","sort","startDate","endDate",
				"deleted","multiChecklist","subvChecklist","formula","unid",
				"documentID","points","required","requirementExceptions"],
			"COLUMN_DEFAULT":["(newid())","","","","","","","","","((0))","((0))","((0))","","","","","",""],
			"DATA_TYPE":["uniqueidentifier","uniqueidentifier","varchar","varchar","varchar","varchar","int","datetime","datetime","bit","bit","bit","varchar","varchar","varchar","float","varchar","varchar"],
			"IS_NULLABLE":["NO","YES","YES","YES","YES","YES","YES","YES","YES","NO","NO","NO","YES","YES","YES","YES","YES","YES"],
			"CHARACTER_MAXIMUM_LENGTH":["","",500,-1,500,500,"","","","","","",2500,500,500,"",500,-1])' );
		variables.aDataCopy = [].set(1,300,"");
	}

	// this is v2, it fails for me (zac) on windows on my laptop with a core i7 with more cores / threads than GHA runners have

	function testEachCallback(){
		//copyTableFor( threads = 10 ); // works
		copyTableForIn( threads = 1 ); // works
		copyTableForIn( threads = 10 ); // fails
		// copyTableForIn( threads = 2 ); // fails

		for (var e in variables.errors){
			systemOutput(e.message, true);
			systemOutput(e.error.message, true);
			systemOutput(e.error.stacktrace, true);
			systemOutput(" ", true);
		}
		expect( variables.errors ).toBeEmpty();
	}

	private function copyTableForIn( required numeric threads ) {
		var qColumns = duplicate( qColumnsCopy );
		var aData = duplicate( aDataCopy );	
		arrayEach( aData, function( row ) {
			var testcompare = {};
			for( var col in qColumns ) {
				try {
					// this will randomly throw -> key [column name] already exist in struct
					structinsert( testcompare, col.COLUMN_NAME, "test" );
				} catch( any cfcatch ) {
					trackError( cfcatch, "copyTableForIn: FOR-IN-#threads# threads ERROR: #col.COLUMN_NAME# collision - #len(testcompare)#" );
				}
			}
		}, ( arguments.threads > 1 ), arguments.threads );
	}

	private function trackError( error, message ){
		systemOutput( message, true );
		arrayAppend( variables.errors, { 
			message: message, 
			error: error 
		});
	}
}
