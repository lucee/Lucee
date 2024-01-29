component {
	this.name="cfml-session-cookie#createGUID()#";
	this.sessionManagement = true;
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.sessioncookie = {
		httpOnly=true,
		timeout=createTimeSpan(1, 0, 0, 0),
		sameSite="none",
		domain="www.lucee.org",
		path="/",
		secure=true,
		partitioned=url.partitioned
	};
}