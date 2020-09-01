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
this.sessionCookie.sameSite = "strict";
this.tag.cookie.sameSite = "strict";

public function onApplicationStart(){
	if(structKeyExists(server.system.environment,"LUCEE_ADMIN_ENABLED") && server.system.environment.LUCEE_ADMIN_ENABLED EQ false){
		cfheader(statuscode="404" statustext="Invalid access");
        abort;
	}
}

</cfscript></cfcomponent>
