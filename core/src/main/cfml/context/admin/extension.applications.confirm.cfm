<!--- 
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
 --->
<cfoutput>
	<br /><br /><br />
	<div class="center">
		<cfif structKeyExists(session,'confirm')>
			<cfif session.confirm.success>
				#session.confirm.text#
			<cfelse>
				<div class="error">#session.confirm.text#</div>
			</cfif>
		</cfif>
		
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.ok#">
		</cfform>
	</div>
</cfoutput>