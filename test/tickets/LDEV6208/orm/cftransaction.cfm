<cfscript>
	r = {};
	r.before = isWithinTransaction();
	transaction {
		r.inside = isWithinTransaction();
		entityLoad( "LDEV6208Item" );
		r.insideAfterOrm = isWithinTransaction();
	}
	r.after = isWithinTransaction();
	writeOutput( serializeJSON( r ) );
</cfscript>
