component {
	this.name="cookie-partitioned-ldev-4756";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="cfml";

	if ( structKeyExists(url, "secure") )
		this.tag.cookie.secure = url.secure;
	if ( structKeyExists(url, "path") )
		this.tag.cookie.path = url.path;
	if ( structKeyExists(url, "partitioned") )
		this.tag.cookie.partitioned = url.partitioned;
}