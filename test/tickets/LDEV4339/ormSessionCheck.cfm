<cfscript>
	HibernateSessionStats = ormGetSession("ldev4339").getSessionFactory().getStatistics();
	HibernateSessionStats.setStatisticsEnabled( true );

	openThreads = [];
	for( i = 1; i <= 5; i++ ){
		thread name="threadTest#i#"{
			ormGetSession();
		}
		openThreads.append( "threadTest#i#" );
	}
	thread action="join" name="#arrayToList( openThreads )#";

	ormGetSession().close();
	// should be 0
	writeOutput( "#HibernateSessionStats.getSessionOpenCount()#:#HibernateSessionStats.getSessionCloseCount()#" );
</Cfscript>