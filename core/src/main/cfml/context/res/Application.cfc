/**
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
 */
component {

	setting showdebugOutput=false;

	this.name = "__LUCEE_STATIC_RESOURCE" & (left(CGI.CF_TEMPLATE_PATH, 6) == "zip://" ? "_ARC" : "");

	this.clientManagement  = false;
	this.sessionManagement = false;
	this.setClientCookies  = false;
	this.setDomainCookies  = false;
	this.applicationTimeout= createTimeSpan(1,0,0,0);
	this.localMode         = true;
	this.scriptProtect     = "all";
	this.web.charset       = "utf-8";

	variables.isDebug = false;		// ATTN: set to false for production!


	function onApplicationStart() {
		if ( (structKeyExists(server.system.environment, "lucee_admin_enabled") && !server.system.environment["lucee_admin_enabled"])
 			|| ( structKeyExists(server.system.properties, "lucee.admin.enabled") && !server.system.properties["lucee.admin.enabled"] ) ){
			setting showdebugoutput=false;
			cfheader(statuscode="404", statustext="Invalid access");
			abort;
		}
		Application.objects.missingTemplateHandler = new StaticResourceProvider();
	}

	public function onRequestStart() {
		if ( findNoCase( cgi.script_name, cgi.request_url ) eq 0 ){
			setting showdebugoutput=false;
			cfheader(statuscode="404", statustext="Invalid access");
			cfabort;
		}
	}


	function onMissingTemplate(target) {

		if (variables.isDebug)
			onApplicationStart();		// disable cache for debug/develop

		Application.objects.missingTemplateHandler.onMissingTemplate(target);
	}

}