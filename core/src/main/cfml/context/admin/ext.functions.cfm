<cfsilent>
	<cffunction name="isInstalled">
		<cfreturn 1>
		<cfreturn RandRange(1,0)>
	</cffunction>

	<cfscript>
		function updateAvailable(required struct data, required query extensions )  output="yes" {
			
			var result=variables.getdataByid( arguments.data.id, arguments.extensions );
			if ( result.count() ==0 )
				return false;
			var sort = queryNew( "v,type,vf,extra", "varchar,varchar,varchar,varchar" );
			var r= 0;
			
			function parseType (v) {
				var filterTypes = {"beta": 1, "snapshot" : 1,"rc" : 1, "alpha": 1};
				var type = ( arguments.v contains "-" ) ? listLast( arguments.v, "-" ) : "";
				var extra = "";
				var vv = arguments.v;
				if ( type == "" ){
					var suffix = listLast( vv,"." );
					if ( len( suffix ) gt 1 and !isNumeric( suffix[ 1 ] ) ){
						extra = suffix; // i.e .jre8, .odbcj8
						vv = listDeleteAt( vv, listlen( vv,"." ), "." );
					}
				} else if ( !structKeyExists( filtertypes, type ) ){
					extra = type & ""; // i.e. -jre8 -jre11
					type = "";
					vv = listFirst( vv, "-" );
				}

				return {
					v: vv,
					type: type,
					extra: extra
				};
			}

			function addVersion(sort, v, installed){
				var meta = parseType( arguments.v );

				if ( arguments.installed.type neq meta.type ) return;
				if ( arguments.installed.extra neq meta.extra ) return;

				var r = queryAddRow( arguments.sort );
				querySetCell( arguments.sort, "v", variables.toVersionSortable( meta.v ), r );
				querySetCell( arguments.sort, "vf", arguments.v, r );
				querySetCell( arguments.sort, "type", meta.type, r);
				querySetCell( arguments.sort, "extra", meta.extra, r);
			}

			var installed = parseType( arguments.data.version );
			loop array=#result.otherVersions# index="local.i" {
				addVersion( sort, i, installed );
			}
			addVersion( sort, result.version, installed );
			querySort(sort, "v", "desc");
			
			if ( sort.recordcount gt 0 ){
				if ( sort.v[ 1 ] GT variables.toVersionSortable( installed.v ) )
					return sort.vf[ 1 ];
			}
			return false;
		}
	</cfscript>

	<cffunction name="doFilter" returntype="string" output="false">
		<cfargument name="filter" required="yes" type="string">
		<cfargument name="value" required="yes" type="string">
		<cfargument name="exact" required="no" type="boolean" default="false">
		<cfset arguments.filter=replace(arguments.filter,'*','',"all")>
		<cfif not len(filter)>
			<cfreturn true>
		</cfif>
		<cfif exact>
			<cfreturn filter EQ value>
		<cfelse>
			<cfreturn FindNoCase(filter,value)>
		</cfif>
	</cffunction>

	<cffunction name="loadCFC" returntype="struct" output="yes">
		<cfargument name="provider" required="yes" type="string">
		<cfreturn createObject('component',"ExtensionProviderProxy").init(arguments.provider)>
	</cffunction>

	<cfset request.loadCFC=loadCFC>

	<cffunction name="getDetail" returntype="struct" output="yes">
		<cfargument name="hashProvider" required="yes" type="string">
		<cfargument name="appId" required="yes" type="string">
		<cfset var detail=struct()>
		<cfset providers=request.providers>
		<cfloop query="providers">
			<cfif hash(providers.url) EQ arguments.hashProvider>
				<cfset detail.provider=loadCFC(providers.url)>
				<cfdump var=#detail.provider#>
				<cfabort>
				<cfset var apps=detail.provider.listApplications()>
				<cfset detail.info=detail.provider.getInfo()>
				<cfset detail.url=providers.url>
				<cfset detail.info.cfc=providers.url>
				<cfloop query="apps">
					<cfif apps.id EQ arguments.appId>
						<cfset detail.app=querySlice(apps,apps.currentrow,1)>
						<cfbreak>
					</cfif>
				</cfloop>
			</cfif>
		</cfloop>
		<!--- installed --->
		<cfloop query="extensions">
			<cfif  hash(extensions.provider) EQ arguments.hashProvider and extensions.id EQ arguments.appId>
				<cfset detail.installed=querySlice(extensions,extensions.currentrow,1)>
				<cfbreak>
			</cfif>
		</cfloop>
		<cfreturn detail>
	</cffunction>

<cfscript>
	/**
	* returns the row matching the given id from given extesnion query, if there is more record than once for given id (data from different extension provider), the data with the newest version is returned
	*/
	struct function getDataById(required string id,required query extensions){
		var rtn={};
		loop query="#arguments.extensions#" {
			if(arguments.extensions.id EQ arguments.id && (rtn.count()==0 || rtn.version LT arguments.extensions.version) ) {
				 rtn=queryRowData(arguments.extensions,arguments.extensions.currentrow);
			}
		}
		return rtn;
	}


</cfscript>
	<cffunction name="getInstalledById" returntype="struct" output="yes">
		<cfreturn tmp>
	</cffunction>

	<cffunction name="getDownloadDetails" returntype="struct" output="yes">
		<cfargument name="hashProvider" required="yes" type="string">
		<cfargument name="type" required="yes" type="string">
		<cfargument name="serverId" required="yes" type="string">
		<cfargument name="webId" required="yes" type="string">
		<cfargument name="appId" required="yes" type="string">
		<cfargument name="addional" required="no" type="struct">
		<cfset providers=request.providers>
		<cfloop query="providers">
			<cfif hash(providers.url) EQ arguments.hashProvider>
				<cfset detail.provider= request.loadCFC(providers.url)>
				<cfreturn detail.provider.getDownloadDetails(type,serverId,webId,appId,addional)>
			</cfif>
		</cfloop>
		<cfreturn struct()>
	</cffunction>

	<cfset request.getDownloadDetails=getDownloadDetails>

	<cffunction name="getDetailFromExtension" returntype="struct" output="yes">
		<cfargument name="hashProvider" required="yes" type="string">
		<cfargument name="appId" required="yes" type="string">
		<cfset var detail=struct()>
		<cfset detail.installed=false>
		<cfloop query="extensions">
			<cfif hash(extensions.provider) EQ arguments.hashProvider and  extensions.id EQ arguments.appId>
				<cfset detail.info.title="">
				<cfset detail.url=extensions.provider>
				<cfset detail.info.cfc=extensions.provider>
				<cfset detail.app=querySlice(extensions,extensions.currentrow,1)>
				<cfset detail.installed=true>
				<cfbreak>
			</cfif>
		</cfloop>

		<!--- installed --->
		<cfloop query="extensions">
			<cfif  hash(extensions.provider) EQ arguments.hashProvider and extensions.id EQ arguments.appId>
				<cfset detail.installed=querySlice(extensions,extensions.currentrow,1)>
				<cfbreak>
			</cfif>
		</cfloop>
		<cfreturn detail>
	</cffunction>

	<cffunction name="getDumpNail" localmode=true output="false">
		<cfargument name="src" required="yes" type="string">
		<cfargument name="width" required="yes" type="number" default="80">
		<cfargument name="height" required="yes" type="number" default="40">
		<cfset local.empty=("R0lGODlhMQApAIAAAGZmZgAAACH5BAEAAAAALAAAAAAxACkAAAIshI+py+0Po5y02ouz3rz7D4biSJbmiabqyrbuC8fyTNf2jef6zvf+DwwKeQUAOw==")>
		

		<!--- no image passed in --->
		<cfif len(arguments.src) EQ 0>
			<cfreturn "data:image/png;base64,#empty#">
		</cfif>

		<!--- extension metadata sometimes embeds huge base64 logos; skip before decode/imageRead --->
		<cfset local.maxEmbeddedSrcLen = 350000>
		<cfset local.maxLogoBytes = 524288>
		<cfif !isValid("URL", arguments.src) && !fileExists(arguments.src) && len(arguments.src) GT local.maxEmbeddedSrcLen>
			<cfreturn "data:image/png;base64,#empty#">
		</cfif>

		<cftry>
			<cfset local.id=hash(arguments.src&":"&arguments.width&"-"&arguments.height)>
			<cfset mimetypes={png:'png',gif:'gif',jpg:'jpeg'}>

			<cfif len(arguments.src) ==0>
				<cfset ext="gif">
			<cfelse>
			    <cfset ext=listLast(arguments.src,'.')>
			    <cfif ext==arguments.src>
					<cfset ext="png"><!--- base64 encoded binary --->
				</cfif>
			</cfif>
			<cfif !StructKeyExists(mimetypes, ext)>
				<cfset ext="png">
			</cfif>

			<cfset cache=true>
			<cfset serversideDN=true>

			<!--- copy and shrink to local dir --->
			<cfset local.tmpdir=expandPath("{temp-directory}/thumbnails/")>
			<cfif !directoryExists(tmpdir)>
				<cfset directoryCreate(tmpdir)>
			</cfif>
			<cfset local.tmpfile = tmpdir & "/extLogo__" & id & "." & ext>
			<cfset local.fileName = id&"."&ext>

			<!--- already in cache 
				TODO cache busting?????
			--->
			<cfif cache && fileExists(tmpfile)>
				<cfreturn "data:image/png;base64,#toBase64(fileReadBinary(tmpfile))#">
			</cfif>
			
			<cfif (isValid("URL", arguments.src)) || fileExists(arguments.src)>
				<!--- fetching from an url can be slow, over 1s --->
				<cfset local.data=FileReadBinary(arguments.src)>
			<cfelse>
				<cfset local.data=toBinary(arguments.src)>
			</cfif>

			<cfif len(local.data) GT local.maxLogoBytes>
				<cfreturn "data:image/png;base64,#empty#">
			</cfif>
			
			<!--- is the image extension installed? --->
			<cfif serversideDN && extensionExists("B03E92E1-F2F3-4380-981922D0BDFEF2B8")> 
				<cfif isImage(data)>
					<cfset local.img=imageRead(data)>
					<!--- shrink images if needed --->
					<cfif img.height GT arguments.height || img.width GT arguments.width>
						<cfif img.height GT arguments.height >
							<cfset imageResize(img,"",arguments.height)>
						</cfif>
						<cfif img.width GT arguments.width>
							<cfset imageResize(img,arguments.width,"")>
						</cfif>
					</cfif>
					<!--- we go this way to influence the quality of the image 
						and cache the local file

					--->
					<cfset imagewrite(image:img,destination:tmpfile)>
					<cfset local.b64=toBase64(fileReadBinary(tmpfile))>
				</cfif>
			</cfif>	

			<cfif isNull(local.b64) && isBinary(data)>
				<!--- cache it anyway as it's a slow download --->
				<cfset FileWrite(tmpfile, data)>
				<cfset local.b64=toBase64(data)>
			</cfif>				

			<cfcatch>
				<cflog text="Error parsing extension logo, #cfcatch.message#, src length #len(arguments.src)#" type="error">
				<cfset local.b64=local.empty>
			</cfcatch>
		</cftry>

		<cfreturn "data:image/png;base64,#b64#">

	</cffunction>

<cfscript>

	function getExtensionGroups() {
		cfadmin(
			action="getExtensionGroups",
			type="#request.adminType#",
			password="#session["password"&request.adminType]#",
			returnVariable="local.groupIds");
			
		return groupIds;
	}
	
	function getLuceeExtensions(required array groupIds) cachedwithin="#createTimeSpan(0,1,0,0)#" {
		var names=[];
		var extensions=[:];
		var qry=queryNew("id,name,groupId,artifactId,version,lastModified,description,otherVersions,image");
		var prefix="t"&createUUID();
		loop array=groupIds item="local.groupId" {
			try{
				var artifacts=luceeExtension(groupId);
				if(arrayLen(artifacts) == 0) {
					continue;
				}
			}
			catch(any ex) {
				continue;
			}

			loop array=artifacts index="local.i" item="local.artifactId" {
				var name=prefix&"_"&groupId&"_"&artifactId;
				arrayAppend(names, name);
				thread name=name extensions=extensions groupId=groupId artifactId=artifactId {
					extensions[artifactId]["groupId"]=groupId;
					var versions=luceeExtension(groupId,artifactId);
					extensions[artifactId]["versions"]=versions;
					if(len(versions)) {
						for(local.v=len(versions);local.v > 0;local.v--) {
							try{
								extensions[artifactId]["last"]=luceeExtension(groupId,artifactId,versions[v],true);
								break;
							}
							catch(any e) {}
						}
					}
				}
			}
		}
		thread action="join" name=arrayToList(names);

			
		loop struct=extensions key="local.artifactId" item="local.data" {
			if(isNull(data.last) || structCount(data.last)==0) continue;
			var row=queryAddRow(qry);
			querySetCell(qry,"groupId",data.groupId ?: groupId);
			querySetCell(qry,"artifactId",artifactId);
			querySetCell(qry,"id",data.last.metadata.id?:"");
			querySetCell(qry,"name",data.last.metadata.name?:"");
			querySetCell(qry,"description",data.last.metadata.description?:"");
			querySetCell(qry,"image",data.last.metadata.image?:"");
			querySetCell(qry,"lastModified",data.last.metadata.buildDate?:data.last.lastModified);
			querySetCell(qry,"version",data.last.version?:"");
			querySetCell(qry,"otherVersions",data.versions);
			
		}
		return qry;
	}

	function toVersionsSorted(required array versions) localMode=true {
		var sorted = queryNew("ver,sort");
		loop array=arguments.versions item="local.v"{
			row = queryAddRow(sorted);
			querySetCell(sorted, "ver", v, row);
			querySetCell(sorted, "sort", toVersionSortable(v), row);
		}
		QuerySort(sorted, 'sort', 'desc');
		var result = structNew("linked");
		loop query=sorted {
			result[sorted.sort] = sorted.ver;
		}
		return result;
	}
	
	function toVersionSortable(version) localMode=true {
		version=variables.unwrap(arguments.version.trim());
		arr=listToArray(arguments.version,'.');

		// OSGi compatible version
		if(arr.len()==4 && isNumeric(arr[1]) && isNumeric(arr[2]) && isNumeric(arr[3])) {
			try{
				osgiVersion = variables.toOSGiVersion(version);
				if (structCount(osgiVersion)) {
					return variables.toOSGiVersion(version).sortable;
				}
			}
			catch(local.e){
				//systemOutput(version & " " & e.message, true);
			};
		}


		rtn="";
		for (i = 1; i <= arrayLen(arr); i++) {
			v = REReplace(arr[i], "[a-zA-Z-]", "", "all");
			paddingLength = (i == 4) ? 4 : 5;
			v = (len(v) < paddingLength) ? repeatString("0", paddingLength - len(v)) & v : v;
			rtn &= (i == 1 ? "" : ".") & v;
		}
		if (arrayLen(arr) <= 3) {
			rtn &= ".0000";
		}
		return 	rtn;
	}

	function toOSGiVersion(version, ignoreInvalidVersion) localmode=true {
		var arr=listToArray(arguments.version,'.');

		if(arr.len()!=4 || !isNumeric(arr[1]) || !isNumeric(arr[2]) || !isNumeric(arr[3])) {
			if(ignoreInvalidVersion?: false) return {};
			return {};
			//throw "version number ["&arguments.version&"] is invalid";
		}
		var sct={major:arr[1]+0,minor:arr[2]+0,micro:arr[3]+0,qualifier_appendix:"",qualifier_appendix_nbr:100};

		// qualifier has an appendix? (BETA,SNAPSHOT)
		var qArr=listToArray(arr[4],'-');
		if(qArr.len()==1 && isNumeric(qArr[1])) local.sct.qualifier=qArr[1]+0;
		else if(qArr.len()==2 && isNumeric(qArr[1])) {
			sct.qualifier=qArr[1]+0;
			sct.qualifier_appendix=qArr[2];
			if(sct.qualifier_appendix=="SNAPSHOT")sct.qualifier_appendix_nbr=0;
			else if(sct.qualifier_appendix=="BETA")sct.qualifier_appendix_nbr=50;
			else sct.qualifier_appendix_nbr=75; // every other appendix is better than SNAPSHOT
		}else if(qArr.len()==3 && isNumeric(qArr[1])) {
			sct.qualifier=qArr[1]+0;
			sct.qualifier_appendix1=qArr[2];
			sct.qualifier_appendix2=qArr[3];
			if(sct.qualifier_appendix1 =="ALPHA" || sct.qualifier_appendix2 == 'SNAPSHOT' )sct.qualifier_appendix_nbr=25;
			else sct.qualifier_appendix_nbr=75; // every other appendix is better than SNAPSHOT
		}
		else return {}; // throw "version number ["&arguments.version&"] is invalid";
		sct.pure=
					sct.major
					&"."&sct.minor
					&"."&sct.micro
					&"."&sct.qualifier;
		sct.display=
					sct.pure
					&(sct.qualifier_appendix==""?"":"-"&sct.qualifier_appendix);
		sct.sortable=repeatString("0",5-len(sct.major))&sct.major
					&"."&repeatString("0",5-len(sct.minor))&sct.minor
					&"."&repeatString("0",5-len(sct.micro))&sct.micro
					&"."&repeatString("0",4-len(sct.qualifier))&sct.qualifier
					& #sct.keyExists("qualifier_appendix1") && isNumeric(sct.qualifier_appendix1)? "."&repeatString("0",4-len(sct.qualifier_appendix1))&sct.qualifier_appendix1  : ""#
					&"."&repeatString("0",3-len(sct.qualifier_appendix_nbr))&sct.qualifier_appendix_nbr;
		return sct;
	}

	function unwrap(str) {
		local.str = arguments.str.trim();
		if((left(str,1)==chr(8220) || left(str,1)=='"') && (right(str,1)=='"' || right(str,1)==chr(8221)))
			str=mid(str,2,len(str)-2);
		else if(left(str,1)=="'" && right(str,1)=="'")
			str=mid(str,2,len(str)-2);
		return str;
	}
</cfscript>



</cfsilent>
