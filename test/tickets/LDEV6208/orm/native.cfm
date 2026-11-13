<cfscript>
	r = {};
	// opens the ORM session
	entityLoad( "LDEV6208Item" );
	r.before = isWithinTransaction();

	tx = ormGetSession().beginTransaction();
	r.insideCommit = isWithinTransaction();
	tx.commit();
	r.afterCommit = isWithinTransaction();

	tx = ormGetSession().beginTransaction();
	r.insideRollback = isWithinTransaction();
	tx.rollback();
	r.afterRollback = isWithinTransaction();

	writeOutput( serializeJSON( r ) );
</cfscript>
