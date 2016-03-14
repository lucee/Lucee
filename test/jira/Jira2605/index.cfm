<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 ---><cfsetting showdebugoutput="false"><cfscript>
url.number=1; // avoid exception thrown by param itself 
param numeric url.number;  
param url.number; 
param numberx=36; 
param url.numbery=45; 
param url.numbery= 45; 
param url.numbery =45; 
param url.numbery = 45; 
param name="url.number";
param name="url.number" type="numeric";

echo(numberx);
echo(numbery);
</cfscript>