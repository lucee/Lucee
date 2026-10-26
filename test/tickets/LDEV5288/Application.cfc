component {
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,10)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.name="ldev-5288";
	this.sessionType="j2ee";
	this.sessionCookie = { httpOnly: true, secure: true, sameSite: "strict" };
}