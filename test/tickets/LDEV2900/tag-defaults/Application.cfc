component {
	this.name="cookie-tagdefaults-ldev-2900";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="cfml";

	if ( structKeyExists(url, "samesite") )
		this.tag.cookie.sameSite = url.samesite;
	this.tag.cookie.path = "/test";
	this.tag.cookie.httpOnly = true;
}