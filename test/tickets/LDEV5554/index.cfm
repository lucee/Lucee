<cfscript>
	// writes one entry for each of the ways something ends up in out.log or err.log
	System = createObject( "java", "java.lang.System" );
	marker = System.getProperty( "LDEV5554.marker" );
	cfg = createObject( "java", "lucee.runtime.engine.ThreadLocalPageContext" ).getConfig();
	LogUtil = createObject( "java", "lucee.commons.io.log.LogUtil" );
	// Log.LEVEL_INFO goes to out.log, Log.LEVEL_ERROR goes to err.log
	LogUtil.logGlobal( cfg, 1, "LDEV5554", marker & "-global-out" );
	LogUtil.logGlobal( cfg, 4, "LDEV5554", marker & "-global-err" );
	// the writers of the config and the System streams, redirected to out.log and err.log when lucee.system.out and lucee.system.err are set to log
	cfg.getOutWriter().println( marker & "-writer-out" );
	cfg.getErrWriter().println( marker & "-writer-err" );
	cfg.getOutWriter().flush();
	cfg.getErrWriter().flush();
	System.out.println( marker & "-system-out" );
	System.err.println( marker & "-system-err" );
	System.out.flush();
	System.err.flush();
	// the process does not end on its own
	System.exit( 0 );
</cfscript>
