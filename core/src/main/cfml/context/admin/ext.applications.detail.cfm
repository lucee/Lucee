<cfscript>



	function toOrderedArray(array arr, boolean desc=false) {
		arraySort(arr,function(l,r) {
			if(desc) {
				local.tmp=l;
				l=r;
				r=tmp;
			}
			return compare(toVersionSortable(l),toVersionSortable(r));
			});
		return arr;
	}

	function removeFromArray(arr,value) {
		local.value=toVersionSortable(arguments.value);
		loop array=arr index="local.i" item="local.v" {
			if(toVersionSortable(v)==value) {
				arrayDeleteAt(arr,i);
			}
		}
	}


available=getDataByid(url.id,getExternalData(providerURLs));
installed=getDataByid(url.id,extensions);
isInstalled=installed.count() GT 0;


// all version that can be installed

	// other Versions
	if(!isNull(available.otherVersions) && !isSimpleValue(available.otherVersions)) {
		all=duplicate(available.otherVersions);
	}
	else {
		all=[];
	}
		
	// latest version
	if(!isNull(available.version) && !arrayContains(all,available.version)) {
		arrayAppend(all,available.version);
	}

	// order
	toOrderedArray(all,true);
	versionStr = {};
	versionStr.snapShot = [];
	versionStr.pre_release = [];
	versionStr.release = [];
	if(len(all)){
		for(versions in all ){
			if(FindNoCase("SNAPSHOT", versions)){
				arrayAppend(versionStr.snapShot, versions)
			}else if(FindNoCase("ALPHA", versions) || FindNoCase("BETA", versions) || FindNoCase("RC", versions)){
				arrayAppend(versionStr.pre_release, versions);
			}else{
				arrayAppend(versionStr.release, versions);
			}
		}
	}

	if(isInstalled) {
		releaseUpgrade = [];
		releaseDownGrade = [];
		Vs = toVersionSortable(installed.version);
		for(vrsn in versionStr.release){
			if(vs LT toVersionSortable(vrsn)){
				arrayAppend(releaseUpgrade, vrsn);
			} else{
				arrayAppend(releaseDownGrade, vrsn);
			}
		}
		for(type in versionStr){
			removeFromArray(versionStr[type],installed.version)
		}
	}
</cfscript>


<!--- get informatioj to the provider of this extension --->
<cfif !isNull(available.provider)>
	<cfset provider=getProviderInfo(available.provider).meta>
</cfif>

<cfset isInstalled=installed.count() GT 0><!--- if there are records it is installed --->
<cfset isServerInstalled=false>
<cfif !isNull(serverExtensions)>
	<cfset serverInstalled=getDataByid(url.id,serverExtensions)>
	<cfset isServerInstalled=serverInstalled.count()>
</cfif>


<cfset hasExternalInfo=available.count() GT 0>

<cfset hasUpdate=false>
<cfif isInstalled && hasExternalInfo>
	<cfset app=available>
	<cfset hasUpdate=installed.version LT available.version>
<cfelseif hasExternalInfo>
	<cfset app=available>
<cfelse>
	<cfset app=installed>
</cfif>

<cfoutput>
	<!--- title and description --->
	<div class="modheader">
		<h2>#app.name# (<cfif isInstalled>#stText.ext.installed#<cfelseif isServerInstalled>#stText.ext.installedServer#<cfelse>#stText.ext.notInstalled#</cfif>)</h2>
		<cfif !isInstalled && isServerInstalled><div class="error">#stText.ext.installedServerDesc#</div></cfif>

		#replace(replace(trim(app.description),'<','&lt;',"all"), chr(10),"<br />","all")#
		<br /><br />
	</div>

	
					
	<table class="contentlayout">
		<tbody>
			<tr>
				<!--- image --->
				<td valign="top" style="width:200px;">
					<cfif !isNull(app.image)>
						<cfset dn=getDumpNail(app.image,400,400)>
						<div style="width:100%;overflow:auto;">
							<img src="#dn#" alt="#stText.ext.extThumbnail#" />
						</div>
					</cfif>
				</td>
				<td valign="top">
					<table class="maintbl">
						<tbody>
							<!--- Extension Version --->
							<cfif isInstalled>
								<tr>
									<th scope="row">#stText.ext.installedVersion#</th>
									<td>#installed.version#</td>
								</tr>
								<cfif arrayLen(all)>
								<tr>
									<th scope="row">#stText.ext.availableVersion#</th>
									<td>#arrayToList(all,', ')#</td>
								</tr>
								</cfif>
								<tr>
									<th scope="row">Type</th>
									<td>#installed.trial?"Trial":"Full"# Version</td>
								</tr>

							<cfelse>
								<tr>
									<th scope="row">#stText.ext.availableVersion#</th>
									<td>#arrayToList(all,', ')#</td>
								</tr>
							</cfif>
							
							<!--- price --->
							<cfif !isNull(available.price) && len(trim(available.price))>
								<tr>
									<th scope="row">#stText.ext.price#</th>
									<td><cfif available.price GT 0>#available.price# <cfif !isNull(available.currency)>#available.currency#<cfelse>USD</cfif><cfelse>#stText.ext.free#</cfif></td>
								</tr>
							</cfif>
							<!--- category --->
							<cfif !isNull(available.category) && len(trim(available.category))>
								<tr>
									<th scope="row">#stText.ext.category#</th>
									<td>#available.category#</td>
								</tr>
							</cfif>
							<!--- author --->
							<cfif !isNull(available.author) && len(trim(available.author))>
								<tr>
									<th scope="row">#stText.ext.author#</th>
									<td>#available.author#</td>
								</tr>
							</cfif>
							<!--- created --->
							<cfif !isNull(available.created) && len(trim(available.created))>
								<tr>
									<th scope="row">#stText.ext.created#</th>
									<td>#LSDateFormat(available.created)#</td>
								</tr>
							</cfif>
							<!--- id --->
							<tr>
								<th scope="row">Id</th>
								<td>#app.id#</td>
							</tr>
							
							<!--- provider --->
							<cfif !isNull(provider.title) && len(trim(provider.title))>
								<tr>
									<th scope="row">#stText.ext.provider#</th>
									<td><cfif !isNull(provider.url)><a href="#provider.url#" target="_blank"></cfif>#provider.title#<cfif !isNull(provider.url)></a></cfif></td>
								</tr>
							</cfif>
							<!--- bundles --->
							<cfset stText.ext.reqbundles="Required Bundles (Jars)">
							<cfif isInstalled && !isNull(installed.bundles) && installed.bundles.recordcount()>
								<tr>
									<th scope="row">#stText.ext.reqbundles#</th>
									<td>
										<cfloop query="#installed.bundles#">
											- #installed.bundles.name# (#installed.bundles.version#)<br />
										</cfloop>
									</td>
								</tr>
							</cfif>
							
						</tbody>
					</table>
				</td>
			</tr>
		</tbody>
	</table>
	<br />

<!--- Install different versions --->
<cfif arrayLen(all) || isInstalled>
<cfscript>

if(isInstalled) installedVersion=toVersionSortable(installed.version);



</cfscript>
	<h2>#isInstalled?stText.ext.upDown:stText.ext.install#</h2>
	#isInstalled?stText.ext.upDownDesc:stText.ext.installDesc#
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="id" value="#url.id#">
			<input type="hidden" name="provider" value="#isNull(available.provider)?"":available.provider#">
			
		<table class="maintbl autowidth">
		<tbody>
		<cfif arrayLen(all)>
			<tr id="grpConnection">
				<cfset count = 1>
				<cfloop list="Release,Pre_Release,SnapShot" index="key">
					<span><input <cfif count EQ 1>
							class="lftBtn button" <cfelseif count EQ 3> class="br button" <cfelse> class="mdlBtn button" </cfif>  name="changeConnection" id="btn_#UcFirst(Lcase(key))#" value="#stText.services.update.short[key]# <cfif isInstalled && key EQ 'Release'> (#arrayLen(releaseUpgrade)#)<cfelse>(#arraylen(versionStr[key])#)</cfif>" onclick="enableVersion('#UcFirst(Lcase(key))#');"  type="button"></span>
					<cfif arrayLen(versionStr[key])>
						<td class="td_#UcFirst(Lcase(key))#" >
							<select name="version" id="td_#UcFirst(Lcase(key))#"  class="large" style="margin-top:8px">
								<cfloop array="#versionStr[key]#" item="v">
									<cfset vs=toVersionSortable(v)>
									<cfset btn="">
									<cfif isInstalled>
										<cfset comp=compare(installedVersion,vs)>
										<cfif comp GT 0>
											<cfset btn=stText.ext.downgradeTo>
										<cfelseif comp LT 0>
											<cfset btn=stText.ext.updateTo>
										</cfif>
									</cfif>
									<option value="#v#" >#btn# #v#</option>
								</cfloop>
							</select>
						</td>
						<td class="td_#UcFirst(Lcase(key))#"><input type="submit" class="button submit" name="mainAction" value="#isInstalled?stText.Buttons.upDown:stText.Buttons.install#"></td>
					<cfelse>
						<td class="td_#UcFirst(Lcase(key))#">
							<div>#replace(stText.ext.detail.noUpdateDesc,"{type}","<b>#stText.services.update.short[key]#</b>")#</div>
						</td>
					</cfif>
					<cfset count = count + 1>
				</cfloop>
			</tr>
		</cfif>
		<cfif isInstalled>
		<tr>
		<td colspan="2"><input type="submit" style="width:100%" class="button submit" name="mainAction" value="#stText.Buttons.uninstall#"></td>
		</tr>
		</cfif>

		</tbody>
		</table>

		</cfformclassic>
</cfif>

	<!--- Update --->

		

<cfhtmlbody>
<script type="text/javascript">
	$(document).ready(function(){
		var version = 'Release';
		enableVersion(version);
		$("##btn_"+version).addClass("btn");
	});

	function enableVersion(v){
		$("##grpConnection").find('td').each(function(index) {
			var xx = $(this).attr('class');
			$('.'+xx).show();
			$('##'+xx).prop('disabled', false);
			if("td_"+v != xx){
				$('.'+xx).hide();
				$('##'+xx).prop('disabled', true);
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
<!---
TODO


<cfif isDefined('app.minCoreVersion') and (app.minCoreVersion GT server.lucee.version)>
				<div class="error">#replace(stText.ext.toSmallVersion,'{version}',app.minCoreVersion,'all')#</div>
			<cfelse>
--->


