<cfsilent>
	<cffunction name="isInstalled">
		<cfreturn 1>
		<cfreturn RandRange(1,0)>
	</cffunction>
	
	<cffunction name="updateAvailable" output="no">
		<cfargument name="data" required="yes" type="struct">
		<cfargument name="extensions" required="yes" type="query">
		<cfset var result=getdataByid(data.id,extensions)>
		
		<cfif result.count()==0><cfreturn false></cfif>
		<cfif data.version LT result.version>
			<cfreturn true>
		</cfif>	
		
		<cfreturn false>
	</cffunction>
	
	
	
			
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
	
	
	
	
	
	
	
<cfscript>

</cfscript>
	<cffunction name="loadCFC" returntype="struct" output="yes">
		<cfargument name="provider" required="yes" type="string">
		<cfset systemOutput("deprecated function call:<print-stack-trace>",true,true)>
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
		loop query="#extensions#" {
			if(extensions.id EQ arguments.id && (rtn.count()==0 || rtn.version LT extensions.version) ) {
				 rtn=queryRowData(extensions,extensions.currentrow);
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
	
	
	<cffunction name="getDumpNailOld" returntype="string" output="no">
		<cfargument name="imgUrl" required="yes" type="string">
		<cfargument name="width" required="yes" type="number" default="80">
		<cfargument name="height" required="yes" type="number" default="40">
		
		<cfreturn "data:image/png;base64,#imgURL#">
		<cfreturn "thumbnail.cfm?img=#urlEncodedFormat(imgUrl)#&width=#width#&height=#height#">
	</cffunction>    
	
	<cffunction name="getDumpNail">
		<cfargument name="src" required="yes" type="string">
		<cfargument name="width" required="yes" type="number" default="80">
		<cfargument name="height" required="yes" type="number" default="40">

		<cfset local.id=hash(src&":"&width&"x"&height)>
		<cfset mimetypes={png:'png',gif:'gif',jpg:'jpeg'}>
	
		<cfif len(src) ==0>
			<cfset ext="gif">
		<cfelse>
		    <cfset ext=listLast(src,'.')>
		    <cfif ext==src>
				<cfset ext="png"><!--- base64 encoded binary --->
			</cfif>
		</cfif>
		
	
	
	<!--- copy and shrink to local dir --->
	<cfset tmpfile=expandPath("{temp-directory}/admin-ext-thumbnails/__"&id&"."&ext)>
	<cfif fileExists(tmpfile)>

		<cffile action="read" file="#tmpfile#" variable="b64">
	<cfelseif len(src) ==0>
		<cfset local.b64=("R0lGODlhMQApAIAAAGZmZgAAACH5BAEAAAAALAAAAAAxACkAAAIshI+py+0Po5y02ouz3rz7D4biSJbmiabqyrbuC8fyTNf2jef6zvf+DwwKeQUAOw==")>
		
	<cfelse>
		<cfif fileExists(src)>
			<cffile action="readbinary" file="#src#" variable="data">
		<!--- base64 encoded binary --->
		<cfelse>
			<cfset data=toBinary(src)>
		</cfif>
		<cfimage action="read" source="#data#" name="img">

		<!--- shrink images if needed --->
		<cfif img.height GT arguments.height or img.width GT arguments.width>
			<cfif img.height GT arguments.height >
				<cfimage action="resize" source="#img#" height="#arguments.height#" name="img">
			</cfif>
			<cfif img.width GT arguments.width>
				<cfimage action="resize" source="#img#" width="#arguments.width#" name="img">
			</cfif>
			<cfset data=toBinary(img)>
		</cfif>
		
		<cftry>
			<cfset local.b64=toBase64(data)>
			<cffile action="write" file="#tmpfile#" output="#b64#" createPath="true">
			<cfcatch><cfrethrow></cfcatch><!--- if it fails because there is no permission --->
		</cftry>
	</cfif>
	<cfreturn "data:image/png;base64,#b64#">
		
	
	</cffunction>

<cfscript>

	/**
	* get information from all available ExtensionProvider defined
	*/
	function getAllExternalData(boolean forceReload=false, numeric timeSpan=60){
		admin 
			action="getRHExtensionProviders"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="local.providers";
		return getExternalData(queryColumnData(providers,"url"),arguments.forceReload,arguments.timeSpan);
	}

	/**
	* get information from specific ExtensionProvider, if a extension is provided by multiple providers only the for the newest (version) is returned
	*/
	function getExternalData(required string[] providers, boolean forceReload=false, numeric timeSpan=60, boolean useLocalProvider=true) {
		var datas={};
		providers.each(parallel:true,closure:function(value){
				var data=getProviderInfo(arguments.value,forceReload,timespan);
				datas[arguments.value]=data;
			});
		var qry=queryNew("id,name,provider,lastModified");

		if(useLocalProvider) {
			admin 
			    action="getLocalExtensions"
			    type="#request.adminType#"
			    password="#session["password"&request.adminType]#"
			    returnVariable="local.locals";

			// add column if necessary
			loop list="#locals.columnlist()#" item="local.k" {
                if(!qry.columnExists(k)) qry.addColumn(k,[]);
            }

			loop query="#locals#" {
				row=qry.addrow();
				qry.setCell("provider","local",row);
				loop list="#locals.columnlist()#" item="local.k" {
            		qry.setCell(k,locals[k],row);
            	}
			}
		}



		loop struct="#datas#" index="local.provider" item="local.data" {
			if(structKeyExists(data,"error")) continue;
			// add missing columns
			loop list="#data.extensions.columnlist()#" item="local.k" {
                if(!qry.ColumnExists(k)) qry.addColumn(k,[]);
            }
			// add Extensions data
			var row=0;
            loop query="#data.extensions#" label="outer"{
            	row=0;
            	// does a row with the same id already exist?
            	loop query="#qry#" label="inner" {
            		// has already record with that id
            		if(qry.id==data.extensions.id) {
            			// current version is older
            			if(qry.version>=data.extensions.version) continue outer;
            			row=qry.currentrow;
            		}
            	}


            	if(row==0)row=qry.addRow();
				qry.setCell("provider",provider,row);
				qry.setCell("lastModified",data.lastModified,row);
            	loop list="#data.extensions.columnlist()#" item="local.k" {
            		qry.setCell(k,data.extensions[k],row);
            	}
            }
    	}
    	if(isQuery(qry)) querySort(query:qry,names:"name,id");
    	return qry;
	}

	function getProvidersInfo(required string[] providers, boolean forceReload=false, numeric timeSpan=60){
		var datas={};
		providers.each(parallel:false,closure:function(value){
				var data=getProviderInfo(arguments.value,forceReload,timespan);
				datas[arguments.value]=data;
			});
    	return datas;
	}

    
	function getProviderInfo(required string provider, boolean forceReload=false, numeric timeSpan=60){
    	
		if(provider=="local" || provider=="") {
			local.provider={};
			provider.meta.title="Local Extension Provider";
			provider.meta.description="Extensions located at: ";
			provider.meta.mode="develop";
			provider.lastModified=now();
			return provider;
		}


    	// request (within request we only try once to load the data)
        if(!forceReload and
			StructKeyExists(request,"rhproviders") and 
			StructKeyExists(request.rhproviders,provider) and
			isStruct(request.rhproviders[provider]))
        		return request.rhproviders[provider];
        // from session 
        if(!forceReload and
        	  StructKeyExists(session,"rhproviders") and 
			  StructKeyExists(session.rhproviders,provider) and 
			  StructKeyExists(session.rhproviders[provider],'lastModified') and
			  DateAdd("s",arguments.timespan,session.rhproviders[provider].lastModified) GT now())
				return session.rhproviders[provider];
		
		try {
			var start=getTickCount();

			// get info from remote
			var uri=provider&"/rest/extension/provider/info";
			//dump("get:"&uri);
			//SystemOutput(uri&"<print-stack-trace>",true,true);

			admin 
				action="getAPIKey"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				returnVariable="apiKey";

			
			http url="#uri#" result="local.http" {
				httpparam type="header" name="accept" value="application/cfml";
				if(!isNull(apikey))httpparam type="url" name="ioid" value="#apikey#";
			}
			if(isNull(http.status_code)){
				session.rhcfcstries[provider]=now(); // set last try
				local.result.error=http.fileContent;
				result.status_code=404;
				result.lastModified=now();
				result.provider=uri;
				return result;
			}
			// sucessfull
			if(http.status_code==200) { // if(isDefined("http.responseheader['Return-Format']"))
				local.result=evaluate(http.fileContent);
				result.lastModified=now();
				result.provider=uri;
				result.status_code=http.status_code;
				session.rhcfcstries[provider]=now();
				session.rhproviders[provider]=result;
		        request.rhproviders[provider]=result;
				return result;
			}
			// failed
			else {
				session.rhcfcstries[provider]=now(); // set last try
				local.result.error=http.fileContent;
				result.status_code=http.status_code;
				result.lastModified=now();
				result.provider=uri;
				return result;
			}
	    }
		catch(e){
			session.rhcfcstries[provider]=now(); // set last try
			result={status_code=500};
			local.result.error=e.message;
			result.exception=e;
			result.lastModified=now();
			result.provider=uri;
			return result;
		}
		return false;
	}


	function downloadFull(required string provider,required string id){
		return _download("full",provider,id);
	}
	function downloadTrial(required string provider,required string id){
		return _download("trial",provider,id);
	}

	function _download(String type,required string provider,required string id){
    		

		var start=getTickCount();

		// get info from remote
		admin 
			action="getAPIKey"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="apiKey";
		
		var uri=provider&"/rest/extension/provider/"&type&"/"&id;

		if(provider=="local") {
			admin 
				action="getLocalExtension"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				id="#id#"
				asBinary=true
				returnVariable="local.ext";
			return local.ext;
		}
		else {
			http url="#uri#" result="local.http" {
				httpparam type="header" name="accept" value="application/cfml";
				if(!isNull(apiKey))httpparam type="url" name="ioid" value="#apikey#";

			}
			if(!isNull(http.status_code) && http.status_code==200) { 
				return http.fileContent;
			}
			throw http.fileContent;
		}
	}

</cfscript>



</cfsilent>