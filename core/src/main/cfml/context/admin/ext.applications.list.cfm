<cfset hasAccess=true />
<cfset external=getExternalData(providerURLs,true)>
<cfset existing=struct() />
<!--- if user declined the agreement, show a msg --->
<cfif structKeyExists(session, "extremoved")>
	<cfoutput>
		<div class="warning">
			#stText.ext.msgafternotagreed#
		</div>
	</cfoutput>
	<cfset structDelete(session, "extremoved", false) />
</cfif>
<cfif extensions.recordcount>
	<cfoutput>
		<!--- Installed Applications --->
		<h2>#stText.ext.installed#</h2>
		<div class="itemintro">#stText.ext.installeddesc#</div>

		<div class="filterform">
			<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<ul>
					<li>
						<label for="filter">#stText.search.searchterm#:</label>
						<input type="text" name="filter" id="filter" class="txt" value="#session.extFilter.filter#" />
					</li>
					<li>
						<input type="submit" class="button submit" name="mainAction" value="#stText.buttons.filter#" />
					</li>
				</ul>
				<div class="clear"></div>
			</cfform>
		</div>	
		<cfloop list="#request.adminType=="web"?"server,web":"web"#" item="type">
			<cfset _extensions=type=="web"?extensions:serverExtensions>
		<cfif type=="server">
		<div style="text-align:center;background: ##fff;margin:10px 0px 0px 0px;border-radius: 10px;border:1px solid ##bf4f36;">
				<h3 style="color:##bf4f36;margin-top:5px">#stText.ext.installedInServer#</h3>
		</cfif>
		<div<cfif type=="web"> style="margin-top:10px"<cfelse>  style="margin:0px 0px 4px 0px"</cfif> class="extensionlist">
			<cfloop query=_extensions>
				<cfif type=="web"><cfset existing[_extensions.id]=true></cfif>
				<cfif session.extFilter.filter neq "">
					<cftry>
						<cfset prov=getProviderData(_extensions.provider)>
						<cfset provTitle=prov.info.title>
						<cfcatch>
							<cfset provTitle="">
						</cfcatch>
					</cftry>
				</cfif>
				
				<cfset cat=_extensions.categories>
				<cfif 
				session.extFilter.filter eq ""
				or doFilter(session.extFilter.filter,_extensions.name,false)
				or doFilter(session.extFilter.filter,arrayToList(cat),false)
				or doFilter(session.extFilter.filter,provTitle,false)
				><cfscript>
	
					link="#request.self#?action=#url.action#&action2=detail&id=#_extensions.id#";
					img=_extensions.image;
					if(len(img)==0) {
						loop query="#external#"{
							if(external.id==_extensions.id) {
								img=external.image;
								break;
							}
						}
					}
					dn=getDumpNail(img,130,50);
					hasUpdate=updateAvailable(queryRowData(_extensions,_extensions.currentrow),external);
					</cfscript><div class="extensionthumb">

					

						<a <cfif type=="web">href="#link#"<cfelse>style="border-color: ##E0E0E0;"</cfif> title="#_extensions.name#
Categories: #arrayToList(cat)#"><cfif hasUpdate>
       <div class="ribbon-wrapper" <cfif type=="server">style="border-color:##bf4f36"</cfif>><div class="ribbon" <cfif type=="server">style="background-color:##bf4f36"</cfif>>UPDATE ME!</div></div>
</cfif>
<cfif _extensions.trial>
       <div class="ribbon-left-wrapper"><div class="ribbon-left" <cfif type=="server">style="background-color:##bf4f36"</cfif>>TRIAL</div></div>
</cfif>
							<div class="extimg">
								<cfif len(dn)>
									<img src="#dn#" alt="#stText.ext.extThumbnail#" />
								</cfif>
							</div>
							<span <cfif type=="server">style="color:##bf4f36"</cfif>>#cut(_extensions.name,40)#<br /></span>
							<span class="comment" <cfif type=="server">style="color:##bf4f36"</cfif>>#cut(arrayToList(cat),30)#</span>
							
						</a>
					</div>
				</cfif>
			</cfloop>
			<div class="clear"></div>
		</div>
	<cfif type=="server"></div></cfif>
</cfloop>
	</cfoutput>
</cfif>








<!---  Not Installed Applications --->
<cfoutput>
	<h2>#stText.ext.notInstalled#</h2>
	<div class="itemintro">#stText.ext.notInstalleddesc#</div>

	<div class="filterform">
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<ul>
				<li>
					<label for="filter2">#stText.search.searchterm#:</label>
					<input type="text" name="filter2" id="filter2" class="txt" value="#session.extFilter.filter2#" />
				</li>
				<li>
					<input type="submit" class="button submit" name="mainAction" value="#stText.buttons.filter#" />
				</li>
			</ul>
			<div class="clear"></div>
		</cfform>
	</div>
</cfoutput>

<cfif isQuery(external)>
	<div class="extensionlist">

		<cfoutput query="#external#" group="id">
			<cfif !StructKeyExists(existing,external.id)
			and (isnull(external.releaseType) 
				or external.releaseType EQ "" 
				or external.releaseType EQ "all" 
				or external.releaseType EQ request.adminType
			) 
			and (
				session.extFilter.filter2 eq ""
				or doFilter(session.extFilter.filter2,external.name,false)
				or doFilter(session.extFilter.filter2,data.category,false)
				or doFilter(session.extFilter.filter2,info.title,false)
			)
			>
			
				<cfset link="#request.self#?action=#url.action#&action2=detail&id=#external.id#">
				<cfset dn=getDumpNail(external.image,130,50)>
				<div class="extensionthumb">
					<a href="#link#" title="#stText.ext.viewdetails#">
						<div class="extimg">
							<cfif len(dn)>

								<img src="#dn#" alt="#stText.ext.extThumbnail#" />
							</cfif>
						</div>
						<b title="#external.name#">#cut(external.name,30)#</b><br />
						<!------>
						<cfif isDefined("external.price") and external.price GT 0>#external.price# <cfif structKeyExists(external,"currency")>#external.currency#<cfelse>USD</cfif><cfelse>#stText.ext.free#</cfif>
					</a>
				</div>
			</cfif>
		</cfoutput>
		<div class="clear"></div>
	</div>
	
</cfif>



<!--- upload own extension --->
<cfoutput>
	<h2>#stText.ext.uploadExtension#</h2>
	<div class="itemintro">#stText.ext.uploadExtensionDesc#</div>
	<cfif structKeyExists(url, 'noextfile')>
		<div class="error">
			#stText.ext.nofileuploaded#
		</div>
	</cfif>
	<cfif structKeyExists(url, 'addedRe')>
		<div class="error">
			Deployed Lucee Extension, see deploy.log for details.
		</div>
	</cfif>
	<cfform onerror="customError" action="#request.self#?action=#url.action#&action2=upload" method="post" enctype="multipart/form-data">
		<input type="hidden" name="mainAction" value="uploadExt" />
		<table class="tbl maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.ext.extzipfile#</th>
					<td><input type="file" class="txt file" name="extfile" id="extfile" /></td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td>&nbsp;</td>
					<td>
						<input type="submit" class="button submit" value="#stText.ext.upload#" />
					</td>
				</tr>
			</tfoot>
		</table>
	</cfform>
</cfoutput>