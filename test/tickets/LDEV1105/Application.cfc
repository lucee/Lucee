/**
 *
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.*
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
    this.sessionmanagement="Yes" 
	this.sessiontimeout=createTimeSpan(0,0,3,0);
	this.datasources.test = server.getDatasource(service="h2", dbFile=server._getTempDir("LDEV1105"), options={ storage:true } );

	this.sessionStorage="test";
	this.sessionCluster=true;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	
	function onSessionStart() {
		//session.susi="sorglos";
		sessionRotate();
	}
}