/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
 **/
component {

	this.name = hash( getCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());
	if(not isDefined('url.original')) {
		this.sessionmanagement="Yes" 
		this.sessiontimeout=createTimeSpan(0,0,3,0);
		this.sessioncookie={httponly=false, timeout=createTimeSpan(0, 0, 0, 10), secure=true,domain=".domain.com"};
		this.authcookie={timeout=createTimeSpan(0, 0, 0, 10)};
	}

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}