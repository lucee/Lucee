<cfscript>
	param name="url.scenario" default="";

	function names() {
		return valueList( queryExecute( "SELECT name FROM LDEV3657_Person ORDER BY name" ).name );
	}

	function newPerson( name ) {
		return entityNew( "Person", { id: createUUID(), name: arguments.name } );
	}

	queryExecute( "DELETE FROM LDEV3657_Person" );
	ormClearSession();

	result = {};

	switch ( url.scenario ) {

		case "unnamed":
			transaction {
				entitySave( newPerson( "John" ) );
				transactionSetSavepoint();
				transactionCommit();
			}
			result.names = names();
			break;

		case "named":
			transaction {
				entitySave( newPerson( "John" ) );
				transactionSetSavepoint( "sp1" );
				transactionCommit();
			}
			result.names = names();
			break;

		case "loadOnly":
			transaction {
				entityLoad( "Person" );
				transactionSetSavepoint( "sp1" );
				entitySave( newPerson( "A" ) );
				transactionRollback( "sp1" );
				transactionCommit();
			}
			result.names = names();
			break;

		case "rollbackToSavepoint":
			transaction {
				entitySave( newPerson( "A" ) );
				transactionSetSavepoint( "sp1" );
				entitySave( newPerson( "B" ) );
				transactionRollback( "sp1" );
				transactionCommit();
			}
			result.names = names();
			break;

		case "rollbackToSavepointFlushed":
			transaction {
				entitySave( newPerson( "A" ) );
				transactionSetSavepoint( "sp1" );
				entitySave( newPerson( "B" ) );
				ormFlush();
				result.insideBeforeRollback = names();
				transactionRollback( "sp1" );
				result.insideAfterRollback = names();
				transactionCommit();
			}
			result.names = names();
			break;

		case "savepointBeforeOrm":
			transaction {
				queryExecute( "INSERT INTO LDEV3657_Person( id, name ) VALUES ( 'q1', 'keep' )" );
				transactionSetSavepoint( "sp1" );
				entitySave( newPerson( "undone" ) );
				transactionRollback( "sp1" );
				entitySave( newPerson( "after" ) );
				transactionCommit();
			}
			result.names = names();
			break;

		case "twoRollbacks":
			transaction {
				entitySave( newPerson( "A" ) );
				transactionSetSavepoint( "sp1" );
				entitySave( newPerson( "B" ) );
				transactionRollback( "sp1" );
				entitySave( newPerson( "C" ) );
				transactionSetSavepoint( "sp2" );
				entitySave( newPerson( "D" ) );
				transactionRollback( "sp2" );
				entitySave( newPerson( "E" ) );
				transactionCommit();
			}
			result.names = names();
			break;

		case "fullRollbackAfterSavepoint":
			transaction {
				entitySave( newPerson( "A" ) );
				transactionSetSavepoint( "sp1" );
				entitySave( newPerson( "B" ) );
				transactionRollback();
			}
			result.names = names();
			break;

		case "heldEntityDetached":
			transaction {
				p = newPerson( "original" );
				entitySave( p );
				transactionSetSavepoint( "sp1" );
				p.setName( "modified" );
				ormFlush();
				result.insideBeforeRollback = names();
				transactionRollback( "sp1" );
				result.insideAfterRollback = names();
				result.contained = ormGetSession().contains( p );
				transactionCommit();
			}
			result.names = names();
			break;

		case "unknownSavepoint":
			transaction {
				entitySave( newPerson( "A" ) );
				transactionSetSavepoint( "sp1" );
				try {
					transactionRollback( "doesNotExist" );
					result.error = "";
				}
				catch ( any e ) {
					result.error = e.message;
				}
				transactionCommit();
			}
			result.names = names();
			break;

		case "flushFailure":
			transaction {
				entitySave( entityNew( "Strict", { id: createUUID() } ) );
				try {
					transactionSetSavepoint( "sp1" );
					result.error = "";
				}
				catch ( any e ) {
					result.error = e.message;
				}
				transactionRollback();
			}
			ormClearSession();
			break;

		default:
			throw "unknown scenario [#url.scenario#]";
	}

	writeOutput( serializeJSON( result ) );
</cfscript>
