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

<cfif extensions.recordcount || (!isNull(serverExtensions) && serverExtensions.recordcount)>
	<cfoutput>
		<!--- Installed Applications --->
		<h2>#stText.ext.installed#</h2>
		<div class="itemintro">#stText.ext.installeddesc#</div>

		<div class="filterform">
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
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
			</cfformClassic>
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
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
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
		</cfformClassic>
	</div><br>
</cfoutput>
	<cfset existingIds = structKeyList(existing)>
	<cfquery name="unInstalledExt" dbtype="query">
		Select * from external where 1=1
		<cfif ListLen(existingIds)>
			AND id not in (<cfqueryparam value="#existingIds#" list="true">)
		</cfif>
	</cfquery>
	<cfset VersionStr = {}>
	<cfquery dbtype="query" name="VersionStr.Pre_Release">
		Select * from unInstalledExt where version LIKE '%ALPHA%' OR version LIKE '%BETA%' OR version LIKE '%RC%'
	</cfquery>
	<cfquery dbtype="query" name="VersionStr.SnapShot">
		Select * from unInstalledExt where version LIKE '%SnapShot%'
	</cfquery>
	<cfset id = "">
	<cfif VersionStr.Pre_Release.recordcount>
		<cfset id = ListAppend(id,valueList(VersionStr.Pre_Release.id))>
	</cfif>
	<cfif VersionStr.SnapShot.recordcount>
		<cfset id = ListAppend(id,valueList(VersionStr.SnapShot.id))>
	</cfif>
	<cfquery dbtype="query" name="VersionStr.release">
		Select * from unInstalledExt where 1=1
		<cfif ListLen(id)>
			AND id not in (<cfqueryparam value="#id#" list="true">)
		</cfif>
	</cfquery>
<cfoutput>

 <cfif isQuery(external)>
	<cfset hiddenFormContents = "" >
	<cfset count = 1>
	<cfloop list="Release,Pre_Release,SnapShot" index="key">
		<span><input <cfif count EQ 1>
		class="bl button" <cfelseif count EQ 3> class="br button" <cfelse> class="bm button" </cfif>  name="changeConnection" id="btn_#UcFirst(Lcase(key))#" value="#stText.services.update.short[key]# (#versionStr[key].RecordCount#)" onclick="enableVersion('#UcFirst(Lcase(key))#');"  type="button"></span>
		<cfsavecontent variable="tmpContent">
			<div id="div_#UcFirst(Lcase(key))#" class="topBottomSpace">
				<cfif versionStr[key].RecordCount>
					<cfloop query="#versionStr[key]#" group="id">
						<cfif !StructKeyExists(existing,versionStr[key].id)
						and (isnull(external.releaseType) 
							or versionStr[key].releaseType EQ "" 
							or versionStr[key].releaseType EQ "all" 
							or versionStr[key].releaseType EQ request.adminType
						) 
						and (
							session.extFilter.filter2 eq ""
							or doFilter(session.extFilter.filter2,versionStr[key].name,false)
							or doFilter(session.extFilter.filter2,versionStr[key].category,false)
							or doFilter(session.extFilter.filter2,info.title?:'',false)
						)
						>
								<cfset link="#request.self#?action=#url.action#&action2=detail&id=#versionStr[key].id#">
								<cfset dn=getDumpNail(versionStr[key].image,130,50)>
								<div class="extensionthumb">
									<a href="#link#" title="#stText.ext.viewdetails#">
										<div class="extimg">
											<cfif len(dn)>

												<img src="#dn#" alt="#stText.ext.extThumbnail#" />
											</cfif>
										</div>
										<b title="#versionStr[key].name#">#cut(versionStr[key].name,30)#</b><br />
										<!------>
										<cfif structKeyExists(versionStr[key],"price") and versionStr[key].price GT 0>#versionStr[key].price# <cfif structKeyExists(versionStr[key],"currency")>#versionStr[key].currency#<cfelse>USD</cfif><cfelse>#stText.ext.free#</cfif>
									</a>
								</div>
							</cfif>
					</cfloop>
				<cfelse>
					<div>
						#replace(stText.ext.noUpdateDesc,"{type}","<b>#stText.services.update.short[key]#</b>")#
					</div>
				</cfif>
			</div>
			</cfsavecontent>
			<cfset hiddenFormContents &= tmpContent>
			<cfset count = count+1>
	</cfloop>

	<div id="extList" class="extensionlist">
		#hiddenFormContents#
		<div class="clear"></div>
	</div>
	
</cfif>



<!--- upload own extension --->

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
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=upload" method="post" enctype="multipart/form-data">
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
	</cfformClassic>

<cfhtmlbody>
<script type="text/javascript">
	$(document).ready(function(){
		var version = 'Release';
		enableVersion(version);
		$("##btn_"+version).addClass("btn");
	});

	function enableVersion(v){
		$("##extList").find('div').each(function(index) {
			var xx = $(this).attr('id');
			$('##'+xx).show();
			if("div_"+v != xx){
				$('##'+xx).hide();
			}
			});
  		$(".btn").removeClass('btn');
  		$("##btn_"+v).addClass("btn");
	}
	</script>
	<style>
		.btn {
			color:white;
			background-color:##CC0000;
		}
	</style>
	</cfhtmlbody>
</cfoutput>