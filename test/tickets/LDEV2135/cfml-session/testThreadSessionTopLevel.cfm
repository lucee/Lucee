<cfscript>
	//systemOutput(url.toJson(), true);
	session.sessionCluster = url.sessionCluster;
	session.start = 'survived';
	session.threads = [];
	session.sessionStorage = url.sessionStorage;

	_threads = [];

	for(i in [ 1, 2, 3, 4, 5 ] ) {
		name = "ldev2135-#i#";
		arrayAppend( _threads, name);
		thread name="#name#" {
			try {
				ArrayAppend( session.threads, thread.name );
				// sleep( 10 ); // might need to be 1000
				throw(type="blah", message="boom");
			} catch(any e) {
				dump(session);
			}
		}
	}
	session.beforeJoin = 'hello';
	thread action="join" name="#_threads.toList()#";
	session.afterJoin = 'goodbye';
	echo( session.toJson() );
	// This top-level assignment triggers hasChanges=true, so no sessionCommit/sessionTouch needed
	session.topLevel = true;
</cfscript>
