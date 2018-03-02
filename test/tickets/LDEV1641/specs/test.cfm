<cfscript>
	setting showdebugoutput="no";
	param name="FORM.Scene" default="1";
	function clearDB(){
		//Wipe the db
		queryExecute("DELETE FROM card_processor");
		queryExecute("DELETE FROM card_processor_type");

	};

	ormReload();

	if(FORM.Scene == 1){
		hasError = false;
		try{
			clearDB();
			start1 = queryExecute("SELECT * FROM card_processor");
			start2 = queryExecute("SELECT * FROM card_processor_type");

			id1 = replace(createUUID(), "-", "", "all");
			id2 = "VERY-LONG-ID-THAT-SHOULD-NOT-SAVE-IN-THE-DB";
			item1 = EntityNew("CardProcessor", {id:id1, name:"TestCardProcessor"});
			item2 = EntityNew("CardProcessorType", {id:id2, label:"Merchantware"}); //this should not save, the IUD is too long

			transaction {
				EntitySave(item1);
				EntitySave(item2);
				transactionCommit();
			}

		} catch ( any e ){
			hasError = true;
		}
		end1 = queryExecute("SELECT * FROM card_processor");
		end2 = queryExecute("SELECT * FROM card_processor_type");
		writeOutput("#end1.recordCount#|#end2.recordCount#|#hasError#");
	}

</cfscript>