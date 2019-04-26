<cfscript>
	theCust = new cust();
	wequery = new Query();
	sql = "INSERT INTO users (uid,uName) VALUES ('acf')";
	wequery.setSQL(sql);
	try{
		transaction{
			theCust.setTheName('lucee');
			wequery.execute();
			writeoutput(theCust.getTheName());
		}
	}
	catch( any e){
		writeOutput(e.Detail);
	}
	
</cfscript>