
component {
	this.name = "lucee_debbug#server.lucee.version#";
	this.clientmanagement="no";
	this.clientstorage="file"; 
	this.scriptprotect="all";
	this.sessionmanagement="yes";
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,30,0)#";
	this.setclientcookies="yes";
	this.setdomaincookies="no"; 
	this.applicationtimeout="#createTimeSpan(1,0,0,0)#";
	this.localmode="update";
	this.web.charset="utf-8";
	this.scopeCascading="strict";
	setting showdebugoutput=false;

	public function onRequestStart() {
		if ( findNoCase( cgi.script_name, cgi.request_url ) eq 0 || !IsDebugMode() ){
			setting showdebugoutput=false;
			cfheader(statuscode="404", statustext="Invalid access");
			cfabort;
		}
	}
}