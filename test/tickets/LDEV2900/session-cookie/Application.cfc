component {
	this.name="cfml-session-cookie#createGUID()#";
	this.sessionManagement = true;
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.sessioncookie =  {
		HTTPONLY='true',
		timeout=createTimeSpan(1, 0, 0, 0),
		sameSite="strict",
		domain="www.edu.com",
		path="\test",
		secure="true"
	};
}