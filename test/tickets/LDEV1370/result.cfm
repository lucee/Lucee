<cfscript>

	qry=queryExecute('select * from Test1370');
	echo("end:"&qry.recordcount&":"&qry.id&":"&qry.title&";");



	/*sess=getPageContext().getORMSession(false);
	engine=sess.getEngine();
	config=engine.getConfiguration(getPageContext());
	dump();
	if(config==null || (config.flushAtRequestEnd() && config.autoManageSession())){
				ormSession.flushAll(this);
			}
			ormSession.closeAll(this);
			manager.releaseORM();*/
</cfscript>