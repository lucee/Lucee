<cfscript>
	param name="url.scopeCascading" default="standard";
	errors = [];

	function check(val, result) {
		if (arguments.val neq arguments.result){
			ArrayAppend(errors, "[#arguments.val#] was not [#arguments.result#], #callStackGet('string', 1, 1)#");
		}
	}

	try {
		application action="update" scopeCascading="#url.scopeCascading#";

		q = query( a:['query'], b:['query'], c:['query']);
		c = 'variables';
		
		application action="update" searchResults=false;
		loop query="q" {
			check( isNull(a), 'true' );
			check( q.a, 'query' );
			check( q.b,  'query' );
			check( isNull( b ), 'true');
			check( c, 'variables' ); 
		}

		application action="update" searchQueries=true;
		loop query="q" {
			check( isNull( a ), 'false' );
			check( q.a, 'query' );
			check( q.b, 'query' );
			// in a plain old .cfm template, query scope gets checked before variables scope
			check( b,  'query'); // not in a function, so there's no local scope
			check( c, 'query' ); // query scope gets checked before variables scope
		}
	} catch(e) {
		echo( e.message );
	} finally {
		application action="update" scopeCascading="standard";
	}
	if ( arrayLen( errors ) )
		echo( serializeJson( errors ) );
</cfscript>
