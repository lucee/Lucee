<!--- 
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfcomponent><cfscript>

this.name="webadmin#server.lucee.version#";
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
this.sessionCookie.httpOnly = true; // prevent access to session cookies from javascript
this.sessionCookie.secure = isSSL();
this.sessionCookie.sameSite = "strict";
this.sessionCookie.path = getAppFolderPath();
this.tag.cookie.httpOnly = true;
this.tag.cookie.secure = isSSL();
this.tag.cookie.sameSite = "strict";
this.tag.cookie.path = getAppFolderPath();
this.tag.location.addtoken = false;
this.scopeCascading = "strict";
this.hasApplicationCFC = true;

this.xmlFeatures = {
	externalGeneralEntities: false,
	secure: true,
	disallowDoctypeDecl: true
};

public function onRequestStart() {
	if ( findNoCase( cgi.script_name, cgi.request_url ) eq 0 ){
		setting showdebugoutput=false;
		cfheader(statuscode="404", statustext="Invalid access");
		cfabort;
	}
	
	try {
		if ( structKeyExists(session, "passwordWeb") ) {
			admin action="connect"
				type="web"
				password="#session.passwordWeb#";
		}
		if (structKeyExists(session, "passwordServer")) {
			admin action="connect"
				type="server"
				password="#session.passwordServer#";
		}
	} catch( e ) {
		sessionInvalidate();
	}
	
	// we only allow access to admin|web|server|index[.cfm]
	if ( listFindNoCase("admin.cfm,index.cfm,server.cfm,web.cfm", listLast(cgi.script_name,"/") ) eq 0) {
			setting showdebugoutput=false;
			cfheader(statuscode="404", statustext="Invalid access");
			cfcontent(reset="true");
			abort;
		}
}

public function onApplicationStart(){
	if ( (structKeyExists(server.system.environment, "lucee_admin_enabled") && !server.system.environment["lucee_admin_enabled"])
 			|| ( structKeyExists(server.system.properties, "lucee.admin.enabled") && !server.system.properties["lucee.admin.enabled"] ) ){
		setting showdebugoutput=false;
		cfheader(statuscode="404", statustext="Invalid access");
		abort;
	}
	inspectTemplates();
}

private function getAppFolderPath() cachedwithin="request" {

	var folder = listToArray( cgi.SCRIPT_NAME , "/\" );
	if ( arrayLast( folder ) contains ".cfm" )
		arrayPop( folder );

	return "/" & arrayToList( folder, "/" ) & "/";
}

private boolean function isSSL() cachedwithin="request" {
	if ( isBoolean( CGI.SERVER_PORT_SECURE ) AND CGI.SERVER_PORT_SECURE ) {
		return true;
	}
	var headers = GetHTTPRequestHeaders();

	// check typical proxy headers for SSL
	if ( ( headers[ "x-forwarded-proto" ] ?: "") eq "https" ) {
		return true;
	}
	if ( ( headers[ "x-scheme" ] ?: "" eq "https") ) {
		return true;
	}
	// CGI.HTTPS
	if ( structKeyExists( cgi, "https" ) && cgi.https eq "on" ) {
		return true;
	}
	return false;
}

</cfscript></cfcomponent>
