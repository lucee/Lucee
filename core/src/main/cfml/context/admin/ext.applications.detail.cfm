<cfscript>

	function toVersionSortable(required string version) localMode=true {
		version=unwrap(version.trim());
		arr=listToArray(arguments.version,'.');
		
		// OSGi compatible version
		if(arr.len()==4 && isNumeric(arr[1]) && isNumeric(arr[2]) && isNumeric(arr[3])) {
			try{return toOSGiVersion(version).sortable}catch(local.e){};
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


	struct function toOSGiVersion(required string version, boolean ignoreInvalidVersion=false){
		local.arr=listToArray(arguments.version,'.');
		
		if(arr.len()!=4 || !isNumeric(arr[1]) || !isNumeric(arr[2]) || !isNumeric(arr[3])) {
			if(ignoreInvalidVersion) return {};
			throw "version number ["&arguments.version&"] is invalid";
		}
		local.sct={major:arr[1]+0,minor:arr[2]+0,micro:arr[3]+0,qualifier_appendix:"",qualifier_appendix_nbr:100};

		// qualifier has an appendix? (BETA,SNAPSHOT)
		local.qArr=listToArray(arr[4],'-');
		if(qArr.len()==1 && isNumeric(qArr[1])) local.sct.qualifier=qArr[1]+0;
		else if(qArr.len()==2 && isNumeric(qArr[1])) {
			sct.qualifier=qArr[1]+0;
			sct.qualifier_appendix=qArr[2];
			if(sct.qualifier_appendix=="SNAPSHOT")sct.qualifier_appendix_nbr=0;
			else if(sct.qualifier_appendix=="BETA")sct.qualifier_appendix_nbr=50;
			else sct.qualifier_appendix_nbr=75; // every other appendix is better than SNAPSHOT
		}
		else throw "version number ["&arguments.version&"] is invalid";
		sct.pure=
					sct.major
					&"."&sct.minor
					&"."&sct.micro
					&"."&sct.qualifier;
		sct.display=
					sct.pure
					&(sct.qualifier_appendix==""?"":"-"&sct.qualifier_appendix);
		
		sct.sortable=repeatString("0",2-len(sct.major))&sct.major
					&"."&repeatString("0",3-len(sct.minor))&sct.minor
					&"."&repeatString("0",3-len(sct.micro))&sct.micro
					&"."&repeatString("0",4-len(sct.qualifier))&sct.qualifier
					&"."&repeatString("0",3-len(sct.qualifier_appendix_nbr))&sct.qualifier_appendix_nbr;



		return sct;


	}
	function unwrap(String str) {
		str = str.trim();
		if((left(str,1)==chr(8220) || left(str,1)=='"') && (right(str,1)=='"' || right(str,1)==chr(8221)))
			str=mid(str,2,len(str)-2);
		else if(left(str,1)=="'" && right(str,1)=="'")
			str=mid(str,2,len(str)-2);
		return str;
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
				break;
			}
		}
	}


available=getDataByid(url.id,getExternalData(providerURLs));
installed=getDataByid(url.id,extensions);
isInstalled=installed.count() GT 0;


// all version that can be installed

	// Older versions
	if(!isNull(available.older) && !isSimpleValue(available.older)) {
		all=duplicate(available.older);
	}
	else {
		all=[];
	}

	// latest version
	if(!isNull(available.version)) arrayAppend(all,available.version);

	// remove installed
	if(isInstalled)removeFromArray(all,installed.version);
	
	// order
	toOrderedArray(all,true);





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
			<tr>
			<td ><select name="version"  class="large" style="margin-top:8px">
			<cfloop array="#all#" item="v">
				<cfset vs=toVersionSortable(v)>
				<cfif isInstalled>
					<cfset comp=compare(installedVersion,vs)>
					<cfif comp GT 0>
						<cfset btn=stText.ext.downgradeTo>
					<cfelseif comp LT 0>
						<cfset btn=stText.ext.updateTo>
					</cfif>
				<cfelse>
					<cfset btn="">
				</cfif>
					<option value="#v#">#btn# #v#</option>
				
			</cfloop>
		</select> </td>

		<td><input type="submit" class="button submit" name="mainAction" value="#isInstalled?stText.Buttons.upDown:stText.Buttons.install#"></td>
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

		

</cfoutput>


<!---
TODO


<cfif isDefined('app.minCoreVersion') and (app.minCoreVersion GT server.lucee.version)>
				<div class="error">#replace(stText.ext.toSmallVersion,'{version}',app.minCoreVersion,'all')#</div>
			<cfelse>
--->


