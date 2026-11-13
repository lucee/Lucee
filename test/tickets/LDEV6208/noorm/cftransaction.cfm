<cfscript>
	r = {};
	r.before = isWithinTransaction();
	transaction {
		r.inside = isWithinTransaction();
		queryExecute( "SELECT 1" );
		r.insideAfterQuery = isWithinTransaction();
	}
	r.after = isWithinTransaction();

	transaction {
		r.insideRollbackTx = isWithinTransaction();
		transaction action="rollback";
	}
	r.afterRollback = isWithinTransaction();
	writeOutput( serializeJSON( r ) );
</cfscript>
