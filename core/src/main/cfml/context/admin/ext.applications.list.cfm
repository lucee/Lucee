<cfscript>
	hasAccess=true;
	external=getExternalData(providerURLs,true);
	existing={};

	function getLatestVersion(id) {
		loop query=external {
			if(external.id==arguments.id) {
				if(len(external.OtherVersions)) {
					var latest={'vs':toVersionSortable(external.version),'v':external.version};
					loop array=external.OtherVersions item="local.v" {
						var vs=toVersionSortable(v);
						if(isEmpty(latest.vs) || vs>latest.vs) 
							latest={'vs':vs,'v':v};
					}
					return latest;
				}
				break; 
			}
		}
		return {'vs':"",'v':""};
	}

</cfscript>
<!--- if user declined the agreement, show a msg --->
<cfif structKeyExists(session, "extremoved")>
	<cfoutput>
		<div class="warning">
			#stText.ext.msgafternotagreed#
		</div>
	</cfoutput>
	<cfset structDelete(session, "extremoved", false) />
</cfif>
<cfset extCount=(serverExtensions.recordcount?:0)+extensions.recordcount>
<cfif extensions.recordcount || (!isNull(serverExtensions) && serverExtensions.recordcount)>
	<cfoutput>
		<!--- Installed Applications --->
		<h2>#stText.ext.installed#</h2>
		<div class="itemintro">#stText.ext.installeddesc#</div>

		<!--- Filter --->
		<cfif extCount GT 30>
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
		</cfif>
		<cfparam name="listinstalled" default="0">
		<cfparam name="listnotinstalled" default="0">
		<cfloop list="#request.adminType=="web"?"server,web":"web"#" item="_type">
			<cfset _extensions=_type=="web"?extensions:serverExtensions>
		<cfif _type=="server">
		<div style="text-align:center;background: ##fff;margin:10px 0px 0px 0px;border-radius: 10px;border:1px solid ##bf4f36;">
				<h3 style="color:##bf4f36;margin-top:5px">#stText.ext.installedInServer#</h3>
		</cfif>
		<div<cfif _type=="web"> style="margin-top:10px"<cfelse>  style="margin:0px 0px 4px 0px"</cfif> class="extensionlist">
			<cfloop query=_extensions>
				<cfif _type=="web"><cfset existing[_extensions.id]=true></cfif>
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
					latest=getLatestVersion(_extensions.id);
					hasUpdates=latest.vs GT toVersionSortable(_extensions.version);
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
					</cfscript><div class="extensionthumb">

					

						<a <cfif _type=="web">href="#link#"<cfelse>style="border-color: ##E0E0E0;"</cfif> title="#_extensions.name#
Categories: #arrayToList(cat)# 
Installed version: #_extensions.version#<cfif hasUpdates>
Latest version: #latest.v#</cfif>"><cfif hasUpdates>
       <div class="ribbon-wrapper" <cfif _type=="server">style="border-color:##bf4f36"</cfif>><div class="ribbon" <cfif _type=="server">style="background-color:##bf4f36"</cfif>>UPDATE ME!</div></div>
</cfif>
<cfif _extensions.trial>
       <div class="ribbon-left-wrapper"><div class="ribbon-left" <cfif _type=="server">style="background-color:##bf4f36"</cfif>>TRIAL</div></div>
</cfif>	
							<div class="extimg" id="extimg_#_extensions.id#">
								<cfif len(dn)>
									<img src="#dn#" style="max-width:130px;max-height:50px" alt="#stText.ext.extThumbnail#" />
								</cfif>
							</div>
							<cfset listinstalled = listinstalled+1>
							<span <cfif _type=="server">style="color:##bf4f36"</cfif>>#cut(_extensions.name,40)#<br>
							#_extensions.version#<br />
							</span>
							<span class="comment" <cfif _type=="server">style="color:##bf4f36"</cfif>>
							<cfif hasUpdates>#latest.v#</cfif></span>

						</a>
					</div>
				</cfif>
			</cfloop>
			<div class="clear"></div>
		</div>
	<cfif _type=="server"></div></cfif>
</cfloop>
	</cfoutput>
</cfif>
	<cfif listinstalled eq 0 and extCount gt 30>
		<cfoutput><b>#stText.ext.searchbox# [#session.extFilter.filter#]</b></cfoutput>
	</cfif>

<!---  Not Installed Applications --->
<cfoutput>
	<h2>#stText.ext.notInstalled#</h2>
	<div class="itemintro">#stText.ext.notInstalleddesc#</div>
<cfif external.recordcount eq extensions.recordcount>
	<cfset app_error.message = #stText.services.update.installExtns#>
	<cfset printerror(app_error)>
<cfelseif external.recordcount lt extensions.recordcount>
	<cfset app_error.message = #stText.services.update.chkInternet#>
	<cfset printerror(app_error)>
<cfelse>

<cfscript>
	existingIds = structKeyArray(existing);
	unInstalledExt=external;

	for(row=unInstalledExt.recordcount;row>=1;row--) {

		rt = unInstalledExt.releaseType[row];
		id = unInstalledExt.id[row];
		// not for this admin type
		if(!isnull(rt) and !isEmpty(rt) and rt != "all" and rt != request.adminType) {
			queryDeleteRow(unINstalledExt,row);
		}
		// remove if already installed
		if(arrayFindNoCase(existingIds,id)) {
			queryDeleteRow(unINstalledExt,row);
		}
	}

</cfscript>


<!--- FILTER --->
	<cfif unInstalledExt.recordcount GT 30>

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
</cfif>

<cfscript>
	VersionStr = {
		'pre_release':queryNew(unInstalledExt.columnlist),
		'snapshot':queryNew(unInstalledExt.columnlist),
		'release':queryNew(unInstalledExt.columnlist)
	};

	loop query=unInstalledExt {
		if(findNoCase("-ALPHA",unInstalledExt.version) || findNoCase("-BETA",unInstalledExt.version) || findNoCase("-RC",unInstalledExt.version)) 
			addRow(unInstalledExt,VersionStr.pre_release,unInstalledExt.currentrow);
		else if(findNoCase("-SNAPSHOT",unInstalledExt.version)) 
			addRow(unInstalledExt,VersionStr.snapshot,unInstalledExt.currentrow);
		else
			addRow(unInstalledExt,VersionStr.release,unInstalledExt.currentrow);
	}

	function addRow(src,trg,srcRow) {
		var trgRow=queryAddRow(arguments.trg);
		loop array=queryColumnArray(arguments.src) item="local.col" {
			querySetCell(arguments.trg,col,queryGetCell(arguments.src,col,arguments.srcRow),trgRow);
		}
	}

		private function toVersionSortable(required string version) localMode=true {
		version=unwrap(version.trim());
		arr=listToArray(arguments.version,'.');
		
		// OSGi compatible version
		if(arr.len()==4 && isNumeric(arr[1]) && isNumeric(arr[2]) && isNumeric(arr[3])) {
			try{ return toOSGiVersion(version).sortable; }catch(local.e){};
		}

		rtn="";
		loop array=arr index="i" item="v" {
			if(len(v)<5)
			 rtn&="."&repeatString("0",5-len(v))&v;
			else
				rtn&="."&v;
		} 
		return 	rtn;
	}


</cfscript>


<cfset noneLasCounter=0>
 <cfif isQuery(external)>
	<cfset hiddenFormContents = "" >
	<cfset count = 1>

	<cfloop list="Release,Pre_Release,SnapShot" index="key">
		<span><input 
			<cfif count EQ 1>class="bl button" <cfelseif count EQ 3> class="br button" <cfelse> class="bm button" </cfif>
			style="width:180px"
			name="changeConnection" 
			id="btn_#UcFirst(Lcase(key))#" 
			value="#stText.services.update.short[key]# (#versionStr[key].RecordCount#)" 
			onclick="enableVersion('#UcFirst(Lcase(key))#');"  
			type="button"></span>
		<cfsavecontent variable="tmpContent">
			<div id="div_#UcFirst(Lcase(key))#" >

				<cfloop query="#versionStr[key]#" group="id">
					<cfif  (
						session.extFilter.filter2 eq ""
						or doFilter(session.extFilter.filter2,versionStr[key].name,false)
						or doFilter(session.extFilter.filter2,versionStr[key].category,false)
						or doFilter(session.extFilter.filter2,info.title?:'',false)
					)
					>
							<cfset link="#request.self#?action=#url.action#&action2=detail&id=#versionStr[key].id#">
							<cfset dn=getDumpNail(versionStr[key].image,130,50)>
							<div class="extensionthumb">
								<cfset lasProvider=(versionStr[key].provider?:"")=="local" || findNoCase("lucee.org",versionStr[key].provider) GT 0>
								<cfif not lasProvider><cfset noneLasCounter++></cfif>
								<a <cfif not lasProvider> style="border-color: ###(lasProvider?'9C9':'FC6')#;"</cfif> href="#link#" title="#stText.ext.viewdetails#">
									<div class="extimg">
										<cfif len(dn)>

											 <img src="#dn#" style="max-width:130px;max-height:50px"  alt="#stText.ext.extThumbnail#" />
										</cfif>
									</div>
									<cfset listnotinstalled = listnotinstalled+1>
									<b title="#versionStr[key].name#">#cut(versionStr[key].name,30)#</b><br />
									<!------>
									<cfif structKeyExists(versionStr[key],"price") and versionStr[key].price GT 0>#versionStr[key].price# <cfif structKeyExists(versionStr[key],"currency")>#versionStr[key].currency#<cfelse>USD</cfif><cfelse>#stText.ext.free#</cfif>
								</a>
							</div>
						</cfif>
				</cfloop>
			</div>
			</cfsavecontent>
			<cfset hiddenFormContents &= tmpContent>
			<cfset count = count+1>
	</cfloop>

	<div id="extList" class="extensionlist topBottomSpace">
		#hiddenFormContents#
		<div class="clear"></div>
	</div>
	
</cfif>
	<cfif listnotinstalled eq 0 and unInstalledExt.recordcount gt 30>
		<b>#stText.ext.searchbox# [#session.extFilter.filter2#]</b>
	</cfif>

<cfif noneLasCounter>
	<div class="message" style="border-color: ##FC6;color:##C93;">
		Extensions with a yellow border are not provided by the Lucee Association Switzerland and do not neccessarily follow our guidelines. These extensions are not reviewed by the Lucee Association Switzerland.
	</div>
</cfif>
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
		enableVersion(version, "intial");
		$("##btn_"+version).addClass("btn");
	});

	function enableVersion(v, i){
		$("##extList").find('div').each(function(index) {
			var xx = $(this).attr('id');
			if(i== 'intial'){
				$('##'+xx).show();
				if("div_"+v != xx){
					$('##'+xx).hide();
				}
				$(".btn").removeClass('btn');
				$("##btn_"+v).addClass("btn");
			} else {
				if("div_"+v == xx){
					if($('##'+xx).is(':visible')){
						$('##'+xx).hide();
						$("##btn_"+v).removeClass('btn');
					} else {
						$('##'+xx).show();
						$("##btn_"+v).addClass("btn");
					}
					if(!$('##div_Release').is(':visible') && !$('##div_Pre_release').is(':visible') && !$('##div_Snapshot').is(':visible')){
						$('##'+xx).show();
						$("##btn_"+v).addClass("btn")
					}
				}
			}
		});
	}
	</script>
	</cfhtmlbody>
</cfoutput>
<cfif structKeyExists(request, "refresh") && request.refresh EQ true>
	<script type="text/javascript">
		location.reload(); 
	</script>
</cfif>
