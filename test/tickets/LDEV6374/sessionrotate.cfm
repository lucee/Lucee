<cfscript>
	request.cfidBeforeSessionRotate = session.cfid;
	sessionRotate();
	request.cfidAfterSessionRotate = session.cfid;
</cfscript>
