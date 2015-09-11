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
 ---><cfcomponent hint="Note" extends="lucee.admin.plugin.Plugin">
	
	<cffunction name="init"
		hint="this function will be called to initalize">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		
	</cffunction>


	<cffunction name="overview" output="yes"
		hint="load data for a single note">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		<cfset req.root="cache:///">
        <!--- create some files
		<cfif not DirectoryExists("#req.root#sub")>
            <cffile action="write" addnewline="yes" file="ram://susi.txt" mode="777" output="Hello Susi" fixnewline="no">
            <cfdirectory directory="#req.root#sub" action="create" mode="777">
            <cffile action="write" addnewline="yes" file="ram://sub/susi.txt" mode="777" output="Hello Susi Hello Susi Hello Susi Hello Susi Hello Susi Hello Susi Hello Susi Hello Susi Hello Susi " fixnewline="no">
        </cfif>
        --->
        <cfdirectory directory="#req.root#" action="list" name="req.listing" recurse="yes">
        <cfparam name="req.note_tab" default="info">
        
        <!--- info --->
        	
            <!--- calculate size --->
            <cfset var tmp="">
            <cfset req.size=0>
            <cfset req.countDir=0>
            <cfset req.countFile=0>
            <cfset QueryAddColumn(req.listing,"dspSize",array())>
            <cfset QueryAddColumn(req.listing,"path",array())>
            <cfloop query="req.listing">
				<cfset req.size+=req.listing.size>
                <cfif req.listing.type EQ "file">
                	<cfset req.countFile++>
                <cfelse>
                	<cfset req.countDir++>
                </cfif>
                <cfset req.listing.dspSize=byteFormat(req.listing.size)>
                <cfset tmp=req.listing.directory>
                <cfif right(tmp,1) NEQ "/" and right(tmp,1) NEQ "\" and right(tmp,1) NEQ server.separator.file>
                	<cfset tmp&=server.separator.file>
                </cfif>
                <cfset req.listing.path=tmp&req.listing.name>
             </cfloop>
            <cfset req.dspSize=byteFormat(req.size)>
            
        
	</cffunction>
	
	<cffunction name="delete" output="no"
		hint="delete records in resource">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		
        <cfif StructKeyExists(form,"path")>
       		<cfloop array="#form.path#" index="p">
            	<cfif FileExists(p)>
					<cffile action="delete" file="#p#">
                <cfelseif DirectoryExists(p)>
                	<cfdirectory directory="#p#" action="delete" recurse="yes">
				</cfif>
            </cfloop> 
        </cfif>
        
		<cfreturn "redirect:overview">
	</cffunction>
	
    
    <cffunction name="byteFormat" output="no">
	<cfargument name="raw" type="numeric">
    <cfif raw EQ 0><cfreturn 0></cfif>
    <cfset var b=raw>
    <cfset var rtn="">
   	<cfset var kb=int(b/1024)>
    <cfset var mb=0>
    <cfset var gb=0>
    <cfset var tb=0>
    
    <cfif kb GT 0>
    	<cfset b-=kb*1024>
        <cfset mb=int(kb/1024)>
        <cfif mb GT 0>
        	<cfset kb-=mb*1024>
			<cfset gb=int(mb/1024)>
            <cfif gb GT 0>
                <cfset mb-=gb*1024>
				<cfset tb=int(gb/1024)>
                <cfif tb GT 0>
                    <cfset gb-=tb*1024>
                </cfif>
            </cfif>
        </cfif>
    </cfif>
    
    <cfif tb><cfset rtn&=tb&"tb "></cfif>
    <cfif gb><cfset rtn&=gb&"gb "></cfif>
    <cfif mb><cfset rtn&=mb&"mb "></cfif>
    <cfif kb><cfset rtn&=kb&"kb "></cfif>
    <cfif b><cfset rtn&=b&"b "></cfif>
    <cfreturn trim(rtn)&" ("&raw&")">
</cffunction>
	
</cfcomponent>