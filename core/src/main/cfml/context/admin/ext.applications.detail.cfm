<cfscript>

	struct function getDataByGav(required string groupId,required string artifactId,required query extensions) {
		var rtn={};
		loop query="#arguments.extensions#" {
			if(arguments.extensions.groupId EQ arguments.groupId && arguments.extensions.artifactId EQ arguments.artifactId && (rtn.count()==0 || rtn.version LT arguments.extensions.version) ) {
				 rtn=queryRowData(arguments.extensions,arguments.extensions.currentrow);
			}
		}
		return rtn;
	}
	struct function getDataBy(required query extensions) {
		if(structKeyExists(url, "id")) {
			return getDataById(url.id,arguments.extensions);
		} else {
			return getDataByGav(url.groupId,url.artifactId,arguments.extensions);
		}
	}

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

	available=getDataBy(external);
	installed=getDataBy(extensions);
	isInstalled=installed.count() GT 0;

// all version that can be installed

	// other Versions
	if(!isNull(available.otherVersions) && !isSimpleValue(available.otherVersions)) {
		all=duplicate(available.otherVersions);
	} else {
		all=[];
	}
		
	// latest version
	if(!isNull(available.version) && !arrayContains(all,available.version)) {
		arrayAppend(all,available.version);
	}

	// remove installed
	if(isInstalled) removeFromArray(all,installed.version);
	
	// order
	toOrderedArray(all,true);
	versionStr = {
		snapShot: [],
		pre_release: [],
		release: []
	};
	if(len(all)){
		for(versions in all ){
			if(FindNoCase("SNAPSHOT", versions) || FindNoCase("SNAPHOT", versions)){  // checks SNAPHOT too due to LDEV-3876
				arrayprepend(versionStr.snapShot, versions)
			}else if(FindNoCase("ALPHA", versions) || FindNoCase("BETA", versions) || FindNoCase("RC", versions)){
				arrayprepend(versionStr.pre_release, versions);
			}else{
				arrayprepend(versionStr.release, versions);
			}
		}
	}
	if (arrayLen(versionStr.release) gt 0)
		ext_status="Released";
	else if (arrayLen(versionStr.pre_release))
		ext_status="Pre Release";
	else if (arrayLen(versionStr.snapshot))
		ext_status="Snapshot";
	else 
		ext_status="Not Available";

</cfscript>



<cfset isInstalled=installed.count() GT 0><!--- if there are records it is installed --->


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
<cfif structCount(app) eq 0>
	<cfheader statuscode="404">
	<cfthrow message="Extension [#url.groupId#:#url.artifactId#] not found">
</cfif>
<cfset lasProvider=(app.groupId?:"")=="org.lucee" >
<cfoutput>
	<!--- title and description --->
	<div class="modheader">
		<h2>#app.name# (<cfif isInstalled>#stText.ext.installed#<cfelse>#stText.ext.notInstalled#</cfif>)</h2>
				
		<cfif !lasProvider>
		<div class="warning" style="color:##C93">#stText.ext.providerWarning#</div>
		</cfif>

		<cfset ESAPIExtension = getDataByid('37C61C0A-5D7E-4256-8572639BE0CF5838',extensions)>
		<cfif structCount(ESAPIExtension) && toVersionSortable(ESAPIExtension.version) GTE toVersionSortable('2.2.4.5')>
			<cftry>
				#sanitizehtml(replace(trim(app.description),chr(10),"<br />","all"),'FORMATTING')#
				<cfcatch>
					#replace(trim(app.description),chr(10),"<br />","all")#
				</cfcatch>
			</cftry>
			
		<cfelse>
			#replace(replace(trim(app.description),'<','&lt;',"all"), chr(10),"<br />","all")#
		</cfif>
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
							<img class="ext-logo" width="400" src="#dn#" alt="#stText.ext.extThumbnail#" />
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
							<cfelse>
								<tr>
									<th scope="row">#stText.ext.availableVersion#</th>
									<td>#arrayToList(all,', ')#</td>
								</tr>
							</cfif>
						
							<!--- category --->
							<cfif !isNull(app.category) && len(trim(app.category))>
								<tr>
									<th scope="row">#stText.ext.category#</th>
									<td>#app.category#</td>
								</tr>
							<cfelseif structKeyExists(installed, "categories") and arrayLen(installed.categories)>
								<tr>
									<th scope="row">#stText.ext.category#</th>
									<td>#arrayToList(installed.categories,', ')#</td>
								</tr>
							</cfif>
							<!--- author --->
							<cfif !isNull(app.author) && len(trim(app.author))>
								<tr>
									<th scope="row">#stText.ext.author#</th>
									<td>#app.author#</td>
								</tr>
							</cfif>
							
							<!--- maven --->
							<cfset hasMaven=false>
							<cfif !isNull(app.groupId) && len(trim(app.groupId))>
								<cfset hasMaven=true>
								<tr>
									<th scope="row">Maven</th>
									<td>
										<a href="https://mvnrepository.com/artifact/#app.groupId#/#app.artifactId#" target="_blank" rel="noopener"><!---
										--->#app.groupId# » #app.artifactId#</a>
									</td>
								</tr>
							</cfif>
							<!--- id --->
							<cfif structKeyExists(app, "lastModified") or structKeyExists(app, "buildDate")>
							<tr>
								<th scope="row">Last Modified</th>
								<td>#dateFormat(app.lastModified?:app.buildDate,"long")#</td>
							</tr>	
							</cfif>
							<!--- id --->
							<cfif not hasMaven>
							<tr>
								<th scope="row">Id</th>
								<td>#app.id#</td>
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
							<!--- extension urls --->
							<cfloop list="projectUrl,sourceUrl,documentionUrl" item="u">							
								<cfif !isNull(app[u]) && len(trim(app[u]))>
									<tr>
										<th scope="row">#stText.ext[u]#</th>
										<td><a href="#app[u]#" target="_blank" rel="noopener">#app.u#</a></td>
									</tr>
								</cfif>
							</cfloop>
							
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
	<div class="msg"></div>
	<h2>#isInstalled?stText.ext.upDown:stText.ext.install#</h2>
	#isInstalled?stText.ext.upDownDesc:stText.ext.installDesc#
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" id="versionForm" method="post">
			<input type="hidden" name="id" value="#app.id?:""#">
			<input type="hidden" name="groupId" value="#app.groupId?:""#">
			<input type="hidden" name="artifactId" value="#app.artifactId?:""#">
			<input type="hidden" name="versionInstalled" value="#app.version#">
			<input type="hidden" name="mainAction_" value="#isInstalled?stText.Buttons.upDown:stText.Buttons.install#">
			<input type="hidden" name="provider" value="#isNull(app.provider)?"":app.provider#">
			
		<table class="maintbl autowidth version-selector">
		<tbody>
		<cfset types="Release,Pre_Release,SnapShot">
		<cfif arrayLen(all)>
			<tr><td style="padding-left:12px;">
				<cfset count = 1>
			<cfloop list="#types#" index="key">
				<span><input 
							<cfif count EQ 1>class="bl button" <cfelseif count EQ 3> class="br button" <cfelse> class="bm button" </cfif>  
							style="width:180px"
							name="changeConnection" id="btn_#UcFirst(Lcase(key))#" 
							value="#stText.services.update.short[key]# (#arraylen(versionStr[key])#)" 
							onclick="enableVersion('#UcFirst(Lcase(key))#');"  
							type="button"> </span>

					<cfset count++>
			</cfloop></td>
			</tr>
			<tr id="grpConnection">
				<td>
					<select name="version" id="versions" class="large" style="margin:8px;width:372px">
						<option value="">-- select the version --</option>
						<cfloop list="#types#" index="key">
							<cfif arrayLen(versionStr[key])>
								<optgroup class="td_#UcFirst(Lcase(key))#" label="#stText.services.update.short[key]#">
									<cfset options="">
										<cfscript>
										arraySort(
											versionStr[key],
											function (e1, e2) {
												e1=toVersionSortable(e1);
												e2=toVersionSortable(e2);
												e1 = toNumeric(REReplace(e1.replace(".", "", "all"), "[a-zA-Z-]", "", "all"));
												e2 = toNumeric(REReplace(e2.replace(".", "", "all"), "[a-zA-Z-]", "", "all"));
												if(e1 > e2) return 1;
												else return -1;
											}
										);
										loop array=versionStr[key] item="v"{
											vs=toVersionSortable(v);
											btn="";
											if(isInstalled) {
												installedVersion = toNumeric(REReplace(installedVersion.replace(".", "", "all"), "[a-zA-Z-]", "", "all"));
    											vs = toNumeric(REReplace(vs.replace(".", "", "all"), "[a-zA-Z-]", "", "all"));
    											btn = (installedVersion > vs) ? stText.ext.downgradeTo : stText.ext.updateTo;
											}
											options='<option value="#v#" class="td_#UcFirst(Lcase(key))#" >#btn# #v#</option>'&options;
										}
										writeOutput(options);
										</cfscript>
								</optgroup>
							</cfif>
						</cfloop>
					</select>
					<input type="button" class="button" onclick="versionSelected(this, version)"  value="#isInstalled?stText.Buttons.upDown:stText.Buttons.install#">
				</td>
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
		enableVersion(version, "intial");
		$("##btn_"+version).addClass("btn");
	});

	function enableVersion(v, i){
		if(i== 'intial'){
			$("##grpConnection").find('optgroup' ).each(function(index) {
				var xx = $(this).attr('class');
				window[xx] = $("."+xx).detach();
				if("td_"+v == xx){
					$("##versions").append(window[xx]);
				}
		  		$(".btn").removeClass('btn');
		  		$("##btn_"+v).addClass("btn");
			});
		} else {
			if($( "##btn_"+v).hasClass( "btn" )){
				window[v] = $(".td_"+v).detach();
				$("##btn_"+v).removeClass('btn');
			} else {
				$("##versions").append(window["td_"+v]);
				$("##btn_"+v).addClass('btn');
			}
			if(!$('##btn_Release').hasClass( "btn" ) && !$('##btn_Pre_release').hasClass( "btn" ) && !$('##btn_Snapshot').hasClass( "btn" )){
				$("##versions").append(window["td_"+v]);
				$("##btn_"+v).addClass('btn');
			}
		}
	}
	function versionSelected(v, i){
		var version = $("##versions").val();
		if(version == "")
			$( ".msg" ).empty().append( "<div class='error'>Please Choose any version</div>" );
		else
			$( "##versionForm" ).submit();
	}
	</script>	
</cfhtmlbody>

</cfoutput>
<!---
TODO


<cfif isDefined('app.minCoreVersion') and (app.minCoreVersion GT server.lucee.version)>
				<div class="error">#replace(stText.ext.toSmallVersion,'{version}',app.minCoreVersion,'all')#</div>
			<cfelse>
--->


