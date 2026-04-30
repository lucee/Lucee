<cfscript>
	//systemOutput(url.toJson(), true);
	session.ldev3125 = {};
	session.ldev3125.sessionCluster = url.sessionCluster;
	session.ldev3125.start = 'survived';
	session.ldev3125.threads = [];
	session.ldev3125.sessionStorage = url.sessionStorage;

	_threads = [];

	for(i in [ 1, 2, 3, 4, 5 ] ) {
		name = "ldev2135-#i#";
		arrayAppend( _threads, name);
		thread name="#name#" {
			try {
				ArrayAppend( session.ldev3125.threads, thread.name );
				// sleep( 10 ); // might need to be 1000
				throw(type="blah", message="boom");
			} catch(any e) {
				dump(session.ldev3125);
			}
		}
	}
	session.ldev3125.beforeJoin = 'hello';
	thread action="join" name="#_threads.toList()#";
	session.ldev3125.afterJoin = 'goodbye';
	echo( session.ldev3125.toJson() );
	//systemOutput(session.toJson(), true);
	//session.topLevel = true;
	if ( structKeyExists( url, "sessionCommit" ) )
		sessionCommit();
	else if ( structKeyExists( url, "sessionTouch" ) )
		sessionTouch();
</cfscript>
