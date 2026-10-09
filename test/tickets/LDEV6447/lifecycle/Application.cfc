component {
	this.sessionType = url.sessionType ?: "cfml";
	this.name = "ldev6447_lifecycle_" & this.sessionType;
	this.sessionManagement = true;
	this.sessionStorage = "memory";
	this.sessionTimeout = createTimeSpan( 0, 0, 0, 30 );
	this.setClientCookies = true;
	this.applicationTimeout = createTimeSpan( 0, 0, 1, 0 );
}
