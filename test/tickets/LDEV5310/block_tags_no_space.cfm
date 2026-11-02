<cfscript>
	// other tags with a body, the opening bracket directly after the tag or its attributes
	result = [];
	lock name="LDEV5310" timeout="5" type="exclusive"{
		result.append( "lock" );
	}
	lock name="LDEV5310" timeout=5{
		result.append( "lock2" );
	}
	transaction{
		result.append( "transaction" );
	}
	transaction action="begin"{
		result.append( "transaction2" );
	}
	thread name="LDEV5310thread"{
		thread.done = true;
	}
	thread action="join" name="LDEV5310thread";
	result.append( "thread" & cfthread.LDEV5310thread.done );
	savecontent variable="sc"{
		echo( "savecontent" );
	}
	result.append( sc );
	stopwatch variable="sw"{
		echo( "" );
	}
	result.append( "stopwatch" );
	loop times=2{
		result.append( "loop" );
	}
	loop from=1 to=2 index="i"{
		result.append( "loop" & i );
	}
	silent{
		result.append( "silent" );
	}
	echo( result.toList() );
</cfscript>