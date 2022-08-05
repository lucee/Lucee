<cfscript>
	queryExecute( "DELETE FROM LDEV4121 WHERE id='12345x'" );

	queryExecute("insert into LDEV4121(id, name) values('12345x', NULL)" );

	ormFlush();

	theOrg = entityLoadByPK( "Org", '12345x' );
	writeoutput(theorg.getName());
</cfscript>