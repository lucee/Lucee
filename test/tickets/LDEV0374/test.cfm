<cfscript>
	param name="FORM.Scene" default="1";
	param name="FORM.Purpose" default="";
	
	// Refreshing ORM objects
	// ormReload();

	t1 = entityNew("users");
	t2 = entityNew("users");
	if( FORM.Scene EQ 1 ){
		// using now()
		t1.setDateJoined(now());
		t2.setDateJoined(dateAdd("d", 4, now()));
	}else if( FORM.Scene EQ 2 ){
		// using dateFormat()
		t1.setDateJoined(dateFormat(now()));
		t2.setDateJoined(dateFormat(dateAdd("d", 4, now())));
	}

	EntitySave(t1);
	EntitySave(t2);
	// Getting data from db
	t3 = entityLoad("users");
	date1 = t3[1].getDateJoined();
	date2 = t3[2].getDateJoined();

	try{
		if( FORM.Purpose == "dateDiffMember" ){
			writeOutput(date2.diff( "d", date1 ));
		}else if( FORM.Purpose == "dateCompareMember" ){
			writeOutput(date1.compare( date2, "d" ));
		}else if( FORM.Purpose == "dateDiff" ){
			writeOutput(dateDiff("d", date1, date2));
		}else if( FORM.Purpose == "dateCompare" ){
			writeOutput(dateCompare(date1, date2, "d"));
		}
	} catch( any e ){
		writeOutput( e.Message );
	}

</cfscript>