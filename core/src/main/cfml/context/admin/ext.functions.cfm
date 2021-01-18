<cfsilent>
	<cffunction name="isInstalled">
		<cfreturn 1>
		<cfreturn RandRange(1,0)>
	</cffunction>

	<cffunction name="updateAvailable" output="no">
		<cfargument name="data" required="yes" type="struct">
		<cfargument name="extensions" required="yes" type="query">
		<cfset var result=variables.getdataByid(arguments.data.id,arguments.extensions)>

		<cfif result.count()==0><cfreturn false></cfif>
		<cfif arguments.data.version LT result.version>
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

	<cffunction name="getDumpNail" localmode=true>
		<cfargument name="src" required="yes" type="string">
		<cfargument name="width" required="yes" type="number" default="80">
		<cfargument name="height" required="yes" type="number" default="40">
		<cfset local.empty=("R0lGODlhMQApAIAAAGZmZgAAACH5BAEAAAAALAAAAAAxACkAAAIshI+py+0Po5y02ouz3rz7D4biSJbmiabqyrbuC8fyTNf2jef6zvf+DwwKeQUAOw==")>
		

		<!--- no image passed in --->
		<cfif len(arguments.src) EQ 0>
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
			<cfset cache=true>
			<cfset serversideDN=true>

			<!--- copy and shrink to local dir --->
			<cfset local.tmpdir=expandPath("{temp-directory}/thumbnails/")>
			<cfif !directoryExists(tmpdir)>
				<cfset directoryCreate(tmpdir)>
			</cfif>
			<cfset local.tmpfile=tmpdir&"__"&id&"."&ext>
			<cfset local.fileName = id&"."&ext>

			<!--- already in cache --->
			<cfif cache && fileExists(tmpfile)>
				<cfreturn "data:image/png;base64,#toBase64(fileReadBinary(tmpfile))#">
			</cfif>

			
			<cfif len(arguments.src)<500 && (isValid("URL", arguments.src) || fileExists(arguments.src))>
				<cfset local.data=fileReadBinary(arguments.src)>
			<cfelse>
				<cfset local.data=toBinary(src)>
			</cfif>
			
			<!--- is the image extension installed? --->
			<cfif serversideDN && extensionExists("B737ABC4-D43F-4D91-8E8E973E37C40D1B")> 
				<cfset local.img=imageRead(data)>
				<!--- shrink images if needed --->
				<cfif  (img.width*img.height) GT 1000000 && (img.height GT arguments.height or img.width GT arguments.width)>
					<cfif img.height GT arguments.height >
						<cfset imageResize(img,"",arguments.height)>
					</cfif>
					<cfif img.width GT arguments.width>
						<cfset imageResize(img,arguments.width,"")>
					</cfif>
					<!--- we go this way to influence the quality of the image --->
					<cfset imagewrite(image:img,destination:tmpfile)>
					<cfset local.b64=toBase64(fileReadBinary(tmpfile))>
				</cfif>
			</cfif>	

			<cfif isNull(local.b64)>
				<cfset local.b64=toBase64(data)>
			</cfif>
				

			<cfcatch>
			<cfset systemOutput(cfcatch,1,1)>
				<cfset local.b64=local.empty>
			</cfcatch>
		</cftry>

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
	* get information from specific ExtensionProvider, if an extension is provided by multiple providers only the for the newest (version) is returned
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
			    returnVariable="local.locals" ;
			// add column if necessary
			loop list="#locals.columnlist()#" item="local.k" {
                if(!qry.columnExists(k)) qry.addColumn(k,[]);
            }
            qry.addColumn('otherVersions',[]);

			loop query="#locals#" {
				var row=qry.addrow();
				qry.setCell("provider","local",row);
				loop list="#locals.columnlist()#" item="local.k" {
            		qry.setCell(k,locals[k],row);
            	}
			}
		}

		querySort(query:qry,names:"id");
		local.lastId="";
		for(var row=qry.recordcount;row>=1;row--)  {
			if(qry.id[row]==lastId) {
				if(toVersionSortable(qry.version[row])<toVersionSortable(qry.version[row+1])) {
					local.older=qry.version[row];

					loop array=qry.columnArray() item="local.col" {
						qry[col][row]=qry[col][row+1];
					}
				}
				else
					local.older=qry.version[row+1];

				local.ov=qry.otherVersions[row+1];
				if (isSimpleValue(ov) || isNull(ov))
					qry.otherVersions[row]=[older];
				else {
					arrayAppend(ov,older);
					qry.otherVersions[row]=ov;
				}
				qry.deleteRow(row+1);
			}


			lastId=qry.id[row];
		}

		/* output just for testing
		var q=duplicate(qry);
    	loop list=q.columnlist item="local.le" {
    		if(le=='name' || le=='id' || le=='version' || le=='otherVersions') continue;
	    	q.deleteColumn(le);
	    }
	    dump(q);*/

		loop struct="#datas#" index="local.provider" item="local.data" {
			if(structKeyExists(data,"error")) continue;

			// rename older to otherVersions

			if(queryColumnExists(data.extensions,"older") || !queryColumnExists(data.extensions,"otherVersions")) {
				data.extensions.addColumn("otherVersions",data.extensions.columnData('older'));
				data.extensions.deleteColumn("older");
				//QuerySetColumn(data.extensions,"older","otherVersions");
			}

			// add missing columns
			loop list="#data.extensions.columnlist()#" item="local.k" {
                if(!qry.ColumnExists(k)) qry.addColumn(k,[]);
            }

			// add Extensions data
			var row=0;

            loop query="#data.extensions#" label="outer"{
            	row=0;
            	// does a row with the same id already exist?
            	local.localNewer=false;
            	loop query="#qry#" label="inner" {
            		// has already record with that id
            		if(qry.id==data.extensions.id) {

            			// current version is older
            			row=qry.currentrow;
            			if(qry.version>data.extensions.version) {
            				// local data is newer
							//localNewer=true;
            				//continue outer;
            			}
            		}
            	}

            	// merge
            	if(row>0) {
					qry.setCell("provider",provider,row);
					qry.setCell("lastModified",data.lastModified,row);
					if(variables.toVersionSortable(qry.version[row])<variables.toVersionSortable(data.extensions.version)) {
						local.v=qry.version[row];
						loop list="#data.extensions.columnlist()#" item="local.k" {
							if(k=='otherVersions') continue;
		            		qry.setCell(k,data.extensions[k],row);
		            	}
		            	if(isSimpleValue(qry.otherVersions[row]) || isNull(qry.otherVersions[row])) qry.otherVersions[row]=[v];
		            	else arrayAppend(qry.otherVersions[row],v);
					}
					else {
						if(isSimpleValue(qry.otherVersions[row]) || isNull(qry.otherVersions[row])) qry.otherVersions[row]=[data.extensions.version];
						else arrayAppend(qry.otherVersions[row],data.extensions.version);
					}

					local.locals=qry.otherVersions[row];
					local.externals=data.extensions.otherVersions;
					if(isArray(locals) && locals.len() && isArray(externals) && externals.len()) {
						loop array=locals item="local.lv" {
							local.lvs=variables.toVersionSortable(lv);
							local.exists=false;
							for(var i=externals.len();i>=1;i--) {
								if(variables.toVersionSortable(externals[i])==lvs) exists=true;
							}

							if(!exists) {
								arrayAppend(externals,lv);
							}
						}
						qry.otherVersions[row]=externals;
					}
					else if(isArray(locals) && locals.len() ) {
						qry.otherVersions[row]=locals;
					}
					else {
						qry.otherVersions[row]=externals;
					}


            	}
				else {
					row=qry.addRow();
					qry.setCell("provider",provider,row);
					qry.setCell("lastModified",data.lastModified,row);
	            	loop list="#data.extensions.columnlist()#" item="local.k" {
		            	qry.setCell(k,data.extensions[k],row);
		            }
	        	}
            }
    	}
    	if(isQuery(qry)) querySort(query:qry,names:"name,id");

    	/* output just for testing
		var q=duplicate(qry);
    	loop list=q.columnlist item="local.le" {
    		if(le=='name' || le=='id' || le=='version' || le=='older' || le=='otherVersions') continue;
	    	q.deleteColumn(le);
	    }
		dump(q);*/

    	return qry;
	}

	function getProvidersInfo(required string[] providers, boolean forceReload=false, numeric timeSpan=60,parallel=false){
		var datas={};
		providers.each(parallel:arguments.parallel,closure:function(value){
				var data=getProviderInfo(arguments.value,forceReload,timespan);
				datas[arguments.value]=data;
			});
    	return datas;
	}



	function getProviderInfoAsync(required string provider){
		thread args=arguments {
			getProviderInfo(args.provider, true, 60, 50);
		}
	}


	function getProviderInfo(required string provider, boolean forceReload=false, numeric timeSpan=60, timeout=10){
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
			
			// get info from remote
			var uri=provider&"/rest/extension/provider/info";

			admin
				action="getAPIKey"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				returnVariable="apiKey";

			var start=getTickCount();

			try{
				http url="#uri#?type=all&coreVersion=#server.lucee.version#" cachedWithin=createTimespan(0,0,5,0) result="local.http" timeout=arguments.timeout {
					httpparam type="header" name="accept" value="application/json";
					if(!isNull(apikey))httpparam type="url" name="ioid" value="#apikey#";
				}
			}
			catch(e) {
				// call it in background
				getProviderInfoAsync(arguments.provider);
			}

			if(isNull(http.status_code)){
				session.rhcfcstries[provider]=now(); // set last try
				local.result.error=http.fileContent?:'';
				result.status_code=404;
				result.lastModified=now();
				result.provider=uri;
				return result;
			}

			// sucessfull
			if(http.status_code==200) { // if(isDefined("http.responseheader['Return-Format']"))
				local.result=deserializeJson(http.fileContent,false);
				
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


	function downloadFull(required string provider,required string id, string version=""){
		return _download("full",provider,id,version);
	}
	function downloadTrial(required string provider,required string id, string version=""){
		return _download("trial",provider,id,version);
	}

	function _download(String type,required string provider,required string id, string version=""){

		var start=getTickCount();

		// get info from remote
		admin
			action="getAPIKey"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="apiKey";

		var uri=provider&"/rest/extension/provider/"&type&"/"&id;

		if(provider=="local") { // TODO use version from argument scope
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
			http url="#uri#?type=all&coreVersion=#server.lucee.version##len(arguments.version)?'&version='&arguments.version:''#" result="local.http"  cachedWithin=createTimespan(0,0,5,0) {
				httpparam type="header" name="accept" value="application/cfml";
				if(!isNull(apiKey))httpparam type="url" name="ioid" value="#apikey#";

			}
			
			if(!isNull(http.status_code) && http.status_code==200) {
				return http.fileContent;
			}
			throw "Error: Download extension returned #encodeForHtml(http.status_code)# for #encodeForHtml(uri)#";
		}
	}


	function toVersionSortable(required string version) localMode=true {
		version=variables.unwrap(arguments.version.trim());
		arr=listToArray(arguments.version,'.');

		// OSGi compatible version
		if(arr.len()==4 && isNumeric(arr[1]) && isNumeric(arr[2]) && isNumeric(arr[3])) {
			try{return variables.toOSGiVersion(version).sortable}catch(local.e){};
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
		}else if(qArr.len()==3 && isNumeric(qArr[1])) {
			sct.qualifier=qArr[1]+0;
			sct.qualifier_appendix1=qArr[2];
			sct.qualifier_appendix2=qArr[3];
			if(sct.qualifier_appendix1 =="ALPHA" || sct.qualifier_appendix2 == 'SNAPSHOT' )sct.qualifier_appendix_nbr=25;
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
		local.str = arguments.str.trim();
		if((left(str,1)==chr(8220) || left(str,1)=='"') && (right(str,1)=='"' || right(str,1)==chr(8221)))
			str=mid(str,2,len(str)-2);
		else if(left(str,1)=="'" && right(str,1)=="'")
			str=mid(str,2,len(str)-2);
		return str;
	}
</cfscript>



</cfsilent>
