<!--- 
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
 ---><cfsetting showdebugoutput="no">

 <cfscript>
startTime=sessionStartTime()&"";
session.x=1;
client.x=1;
urltoken=session.urltoken;
sleep(1000);


if(!isDefined("session.x")) echo( "session.x must exist");
sessionInvalidate();

if(isDefined("session.x")) echo( "session.x should not exist");
if(!isDefined("client.x")) echo( "client.x must exist");

if(!(sessionStartTime()>startTime)) echo( "start time must change");
if(session.urltoken==urltoken) echo("urltoken should change");

</cfscript>