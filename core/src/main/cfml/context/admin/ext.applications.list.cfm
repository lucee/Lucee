<cfscript>
	setting requesttimeout=100000;

	hasAccess=true;
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

	admin
		action="getLocalExtensions"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="LocalExtensions" ;

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
<cfset extCount=extensions.recordcount>
<cfif extensions.recordcount>
	<cfoutput>
		<!--- Installed Applications --->
		<h2>#stText.ext.installed#</h2>
		<div class="itemintro">#stText.ext.installeddesc#</div>

		<!--- Filter --->
		<cfif extCount GT 30 or len(session.extFilter.installed)>
		<div class="filterform">
			<cfformClassic onerror="customError" action="#request.self#" method="get">
				<input type="hidden" name="action" value="#url.action#">
				<ul>
					<li>
						<label for="filter">#stText.search.searchterm#:</label>
						<input type="text" name="filter" id="filter" class="txt" value="#session.extFilter.installed#" />
					</li>
					<li>
						<input type="submit" class="button submit" name="mainAction" value="#stText.buttons.filter#" />
						<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.clearFilter#" />
					</li>
				</ul>
				<div class="clear"></div>
			</cfformClassic>
		</div>
		</cfif>
		<cfparam name="listinstalled" default="0">
		<cfparam name="listnotinstalled" default="0">
		<cfset spev=[]>
		<div style="margin-top:10px" class="extensionlist">
			<cfloop query=extensions>
				<cfset existing[extensions.id]=true>
				<cfif session.extFilter.installed neq "">
					<cftry>
						<cfset prov=getProviderData(extensions.provider)>
						<cfset provTitle=prov.info.title>
						<cfcatch>
							<cfset provTitle="">
						</cfcatch>
					</cftry>
				</cfif>

				<cfset cat=extensions.categories>
				<cfif session.extFilter.installed eq ""
					or doFilter(session.extFilter.installed,extensions.name,false)
					or doFilter(session.extFilter.installed,arrayToList(cat),false)
					or doFilter(session.extFilter.installed,provTitle,false)
				><cfscript>
					arrayAppend(spev, extensions.id&";version="&extensions.version);
					latest=getLatestVersion(extensions.id);
					latestVersion = ( isEmpty( latest.vs ) ) ? extensions.version : latest.vs;
					hasUpdates = toNumeric( REReplace( latestVersion, "[^\d]", "", "all" ) ) GT
								 toNumeric( REReplace( toVersionSortable( extensions.version ), "[^\d]", "", "all" ) );
					link="#request.self#?action=#url.action#&action2=detail&id=#extensions.id#&groupId=#extensions.groupId#&artifactId=#extensions.artifactId#";
					img=extensions.image;
					if(len(img)==0) {
						loop query="#external#"{
							if(external.id==extensions.id) {
								img=external.image;
								break;
							}
						}
					}
					dn=getDumpNail(img,130,50);
					</cfscript><div class="extensionthumb">



						<a href="#link#" title="#extensions.name#
Categories:<cfif isArray(cat)>#arrayToList(cat)#<cfelse>#cat#</cfif>
Installed version: #extensions.version#<cfif hasUpdates>
Latest version: #latest.v#</cfif>"><cfif hasUpdates>
       <div class="ribbon-wrapper"><div class="ribbon">UPDATE ME!</div></div>
</cfif>
<cfif extensions.trial>
       <div class="ribbon-left-wrapper"><div class="ribbon-left">TRIAL</div></div>
</cfif>
							<div class="extimg" id="extimg_#extensions.id#">
								<cfif len(dn)>
									<img src="#dn#" style="max-width:130px;max-height:50px" alt="#stText.ext.extThumbnail#" />
								</cfif>
							</div>
							<cfset listinstalled = listinstalled+1>
							<span class="ext-name">#cut(extensions.name,40)#<br>
							#extensions.version#<br />
							</span>

						</a>
					</div>
				</cfif>
			</cfloop>
			<div class="clear"></div>
		</div>
	</cfoutput>
</cfif>
	<cfif listinstalled eq 0 and extCount gt 30>
		<cfoutput><b>#stText.ext.searchbox# [#session.extFilter.installed#]</b></cfoutput>
	</cfif>
	<cfset renderSysPropEnvVar( "lucee.extensions",arrayToList(spev,","))>

<!---  Not Installed Applications --->
<cfoutput>
	<h2>#stText.ext.notInstalled#</h2>
	<div class="itemintro">#stText.ext.notInstalleddesc#</div>
<cfif external.recordcount eq extensions.recordcount>
	<cfset app_error.message = #stText.services.update.installExtns#>
	<cfset printerror(app_error)>
<cfelseif external.recordcount lt extensions.recordcount OR external.recordcount eq LocalExtensions.recordcount>
	<cfset app_error.message = #stText.services.update.chkInternet#>
	<cfset printerror(app_error)>
<cfelse>

<cfscript>
	existingIds = structKeyArray(existing);
	unInstalledExt=external;

	for(row=unInstalledExt.recordcount;row>=1;row--) {

		id = unInstalledExt.id[row];
		// not for this admin type
		
		// remove if already installed
		if(arrayFindNoCase(existingIds,id)) {
			queryDeleteRow(unINstalledExt,row);
		}
	}

</cfscript>


<!--- FILTER --->
	<cfif unInstalledExt.recordcount GT 30 or len(session.extFilter.available)>

	<div class="filterform">
		<cfformClassic onerror="customError" action="#request.self#" method="get">
			<input type="hidden" name="action" value="#url.action#">
			<ul>
				<li>
					<label for="filter2">#stText.search.searchterm#:</label>
					<input type="text" name="filter2" id="filter2" class="txt" value="#session.extFilter.available#" />
				</li>
				<li>
					<input type="submit" class="button submit" name="mainAction" value="#stText.buttons.filter#" />
					<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.clearFilter#" />
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
		versions = duplicate(unInstalledExt.otherVersions);
		if(isSimpleValue(versions ?: "") && isEmpty(versions))  versions=[];
		ArrayPrepend(versions, unInstalledExt.version);
		t = { snap: 0, pre: 0, rel: 0 };
		loop array=versions item="variables.v" {
			if(findNoCase("-ALPHA", v) || findNoCase("-BETA", v) || findNoCase("-RC", v)) {
				t.pre++;
			} else if(findNoCase("-SNAPSHOT", v)) {
				t.snap++;
			} else {
				t.rel++;
			}
		}
		if ( t.rel > 0 )
			addRow( unInstalledExt, VersionStr.release, unInstalledExt.currentrow );
		else if ( t.pre > 0 )
			addRow( unInstalledExt, VersionStr.pre_release, unInstalledExt.currentrow );
		else
			addRow( unInstalledExt, VersionStr.snapshot, unInstalledExt.currentrow );
	}

	function addRow(src,trg,srcRow) {
		var trgRow=queryAddRow(arguments.trg);
		loop array=queryColumnArray(arguments.src) item="local.col" {
			querySetCell(arguments.trg,col,queryGetCell(arguments.src,col,arguments.srcRow),trgRow);
		}
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
						session.extFilter.available eq ""
						or doFilter(session.extFilter.available,versionStr[key].name,false)
						or doFilter(session.extFilter.available,versionStr[key].category,false)
						or doFilter(session.extFilter.available,info.title?:'',false)
					)
					>
							<cfset link="#request.self#?action=#url.action#&action2=detail&id=#versionStr[key].id#&groupId=#versionStr[key].groupId#&artifactId=#versionStr[key].artifactId#">
							<cfset dn=getDumpNail(versionStr[key].image,130,50)>
							<div class="extensionthumb">
								<cfset lasProvider=(versionStr[key].groupId?:"")=="org.lucee">
								<cfif not lasProvider><cfset noneLasCounter++></cfif>
								<a <cfif not lasProvider> style="border-color: ###(lasProvider?'9C9':'FC6')#;"</cfif> href="#link#" title="#stText.ext.viewdetails#">
									<div class="extimg">
										<cfif len(dn)>
											 <img src="#dn#" style="max-width:130px;max-height:50px"  alt="#stText.ext.extThumbnail#" />
										</cfif>
									</div>
									<cfset listnotinstalled = listnotinstalled+1>
									<cfset name=versionStr[key].name>
									<cfif isEmpty(name)>
										<cfset name=versionStr[key].artifactId>
									</cfif>
									<b title="#versionStr[key].name#">#cut(name,30)#</b><br />
									<!------>
									#versionStr[key].groupId?:""#
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
		<b>#stText.ext.searchbox# [#session.extFilter.available#]</b>
	</cfif>

<cfif noneLasCounter>
	<div class="warning focus">
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
	<style>
		.btn {
			color:white;
			background-color:##CC0000;
		}
	</style>
	</cfhtmlbody>
</cfoutput>
<cfif structKeyExists(request, "refresh") && request.refresh EQ true>
	<script type="text/javascript">
		location.reload();
	</script>
</cfif>
