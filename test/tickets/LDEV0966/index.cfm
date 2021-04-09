<cfscript>

query datasource="test" name="qry1" {
	echo("select 1 as one");
}
query datasource="test" name="qry1" {
	echo("select * from tblitem");
}

transaction {
	//systemOutput("before new",true,true);
	variables.item	= entityNew('tblitem', { type: 1 });
	//systemOutput("before save",true,true);
	entitySave(variables.item);
	//systemOutput("before rollback",true,true);
	transactionRollback();
	//systemOutput("after rollback",true,true);
}

/**/
query datasource="test" name="qry2" {
	echo("select * from tblitem");
}
echo(qry1.recordcount==qry2.recordcount);



echo(':');


transaction {
	//systemOutput("before new",true,true);
	variables.item	= entityNew('tblitem', { type: 1 });
	//systemOutput("before save",true,true);
	entitySave(variables.item);
	//systemOutput("before rollback",true,true);
	transactionCommit();
	//systemOutput("after rollback",true,true);
}

/**/
query datasource="test" name="qry3" {
	echo("select * from tblitem");
}
echo(qry2.recordcount==qry3.recordcount);







</cfscript>