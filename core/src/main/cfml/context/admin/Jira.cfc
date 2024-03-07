/**
*  based on the rest api documented here: https://developer.atlassian.com/static/rest/jira/6.1.html#d2e4071
*/
component {
	
	variables.prefix="a2";
	variables.maxInactiveInterval=getPageContext().getRequest().getSession(true).getMaxInactiveInterval();
	/*static {
		static.prefix="a2";
		static.maxInactiveInterval=getPageContext().getRequest().getSession(true).getMaxInactiveInterval();
	}*/
// STATIC

	/* this function creates a singleton for the give set of arguments
	
	public static function getInstance(required string domain, string basePath="/rest/", 
		string username="", string password="", cachedWithin=0,  boolean secure=true) {
		var id=static.prefix&hash(serialize(arguments),"quick");
		
		if(!isNull(static.instances[id])) {
			return static.instances[id];
		}
		return static.instances[id]=new Jira(argumentcollection=arguments);
	}*/


// INSTANCE

	variables.CACHE_KEY="sddddas22eedfecuezdgweu";
	variables.MAX_RECORDS=300;// we use 100 because this is limkited that way by 'jira.search.views.default.max'.


	variables.NL="
";
	variables.EQ="%3D";

	public void function init(required string domain, string basePath="/rest/", 
		string username="", string password="", boolean secure=true) {
		variables.domain=arguments.domain;
		variables.secure=arguments.secure;
		variables.apiPath=arguments.basePath&"api/latest";
		variables.authPath=arguments.basePath&"auth/latest";

		// https://portal.hel.kko.ch/jira/auth/latest/session?login

		// make sure we have the SSL certificate installed
		//if(secure && SSLCertificateList(domain).recordcount==0);
		if(arguments.secure && isNull(variables["installSSL:"&domain])) {
			SSLCertificateInstall(domain);
			variables["installSSL:"&domain]=true;
		}
		variables["installSSL:"&domain]=nullValue();

		// store credentials
		if(!isEmpty(arguments.username)) {
			variables.credentials={
				'username':arguments.username
				,'password':arguments.password
			};
			authIfNecessary();
		}
		
	}
///////////////////////////////////////////////////////////////////////////
//////////////////////////////// ATTACHMENT ///////////////////////////////
///////////////////////////////////////////////////////////////////////////
	/**
	* gets all attachments off an existing issue
	* @issue Id or key of the issue
	*/
	public function getAttachments(required string issue) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/"
			
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			res=arrayStruct2query(data.fields.attachment);
			return isNull(res)?queryNew(""):res;
		}
		else handleNon200(result,path);
	} 

	/**
	* gets a single attachment of an existing issue
	* @id Id of the attachment
	*/
	public function getAttachment(required string id) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/attachment/"
			&arguments.id;
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	} 

	/**
	* deletes a specific atachment
	* @id Id of the attachment
	*/
	public function deleteAttachment(required string id) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/attachment/"
			&arguments.id;
		
		var body={'mimetype':'application/json'};
		var result=_http(path:path,method:"delete",body:body);

		if(result.status_code==200 || result.status_code==204) {
			return;
		}
		else handleNon200(result,path);
	} 

///////////////////////////////////////////////////////////////////////////
//////////////////////////////// COMMENT //////////////////////////////////
///////////////////////////////////////////////////////////////////////////
	/**
	* gets all comments off an existing issue
	* @issue Id or key of the issue
	* @expand if set to true provides body rendered in HTML
	*/
	public function getComments(required string issue, boolean expand=false) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/comment"
			&(arguments.expand?"?expand":"");
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			data.comments=arrayStruct2query(data.comments);
			return data;
		}
		else handleNon200(result,path);
	} 

	/**
	* gets a single comment of an existing issue
	* @issue Id or key of the issue
	* @id Id of the comment
	* @expand if set to true provides body rendered in HTML
	*/
	public function getComment(required string issue, required string id, boolean expand=false) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/comment/"
			&arguments.id
			&(arguments.expand?"?expand":"");
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	} 

	/**
	* deletes a specific comment
	* @issue Id or key of the issue
	* @id Id of the comment
	*/
	public function deleteComment(required string issue, required string id) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/comment/"
			&arguments.id;
		
		var body={'mimetype':'application/json'};
		var result=_http(path:path,method:"delete",body:body);

		if(result.status_code==200 || result.status_code==204) {
			return;
		}
		else handleNon200(result,path);
	} 

	/**
	* adds a comment to an existing issue
	* @issue Id or key of the issue
	* @comment text to add as a new comment
	* @visibility visibility, need to follow this structure {"type": "role","value": "Administrators"}
	*/
	public function addComment(required string issue, required string comment, struct visibility) {
		// https://developer.atlassian.com/server/jira/platform/jira-rest-api-example-add-comment-8946422/
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/comment";
		
		var body={
			'mimetype':'application/json'
			,'data':{
				"body": arguments.comment
			}
		};

		// visibility
		if(!isNull(arguments.visibility)) {
			validateVisibility(arguments.visibility);
			body.data['visibility']=arguments.visibility;
		}


		var result=_http(path:path,method:"post",body:body);

		if(result.status_code==200 || result.status_code==201) {
			return;
		}
		else handleNon200(result,path);
	}

	/**
	* updates an existing comment of an existing issue
	* @issue Id or key of the issue
	* @issue Id of the comment
	* @comment text to add as a new comment
	* @visibility visibility, need to follow this structure {"type": "role","value": "Administrators"}
	*/
	public function updateComment(required string issue, required string id, required string comment, struct visibility) {
		// https://developer.atlassian.com/server/jira/platform/jira-rest-api-example-add-comment-8946422/
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/comment/"
			&arguments.id;
		
		var body={
			'mimetype':'application/json'
			,'data':{
				"body": arguments.comment
			}
		};

		// visibility
		if(!isNull(arguments.visibility)) {
			validateVisibility(arguments.visibility);
			body.data['visibility']=arguments.visibility;
		}


		var result=_http(path:path,method:"put",body:body);

		if(result.status_code==200 || result.status_code==201) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	}

	private function validateVisibility(required struct visibility) {
		var keys={"type": "","value": ""};
		// TODO
	}

///////////////////////////////////////////////////////////////////////////
//////////////////////////////// WORKLOG //////////////////////////////////
///////////////////////////////////////////////////////////////////////////
	public function getWorklog(required string issue) {
		// ?expand=names gives labels for fields

		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&"/worklog?maxResults=1000";
		
		var result=_http(path);
		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			if(!isNull(data.worklogs) && arrayLen(data.worklogs)) {
				return worklogAsQuery(data.worklogs);
			}
				
			return queryNew("");
		}
		else handleNon200(result,path);
	} 


///////////////////////////////////////////////////////////////////////////
//////////////////////////////// ISSUE ////////////////////////////////////
///////////////////////////////////////////////////////////////////////////
	public function getIssue(required string issue, boolean changelog=false) {
		// ?expand=names gives labels for fields

		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&('?maxResults=10000')
			&(changelog?'&expand=changelog':'')
			;
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			data.fields.comment.comments=arrayStruct2query(data.fields.comment.comments);
			if(!isNull(data.changelog.histories)) {
				data.changelog=arrayStruct2query(data.changelog.histories);
			}


			return data;
		}
		else handleNon200(result,path);
	} 


	public function createIssue(required string project, required string issueType,required string summary, string description="", struct fields={}) {
		// https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/";
		

		local.fields={
			"project": {"key": arguments.project},
			"issuetype": {"name": arguments.issueType},
			"summary": arguments.summary,
			"description": arguments.description
		};

		_createUpdateissue(local.fields,arguments.fields);

		//  "customfield_11050" : "Value that we're putting into a Free Text Field."

		var body={
			'mimetype':'application/json'
			,'data':{
				"fields": local.fields
			}
		};


		var result=_http(path:path,method:"post",body:body);

		if(result.status_code==200 || result.status_code==201) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	} 


	public function updateIssue(required string issue, string summary, string description, struct fields={}) {
		// https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue;
		
		local.fields={};

		if(!isNull(arguments.summary)) local.fields['summary']= arguments.summary;
		if(!isNull(arguments.description)) local.fields['description']= arguments.description;

		_createUpdateissue(local.fields,arguments.fields);
		
		var body={
			'mimetype':'application/json'
			,'data':{
				"fields": local.fields
			}
		};

		var result=_http(path:path,method:"put",body:body);

		if(result.status_code==200 || result.status_code==204) {
			return;
		}
		else handleNon200(result,path);
	} 

	private function _createUpdateissue(fields, customFields) {
		loop struct=arguments.customFields index="local.k" item="local.v" {
			arguments.fields[k]=v;
		}
	}

	public function deleteIssue(required string issue,boolean deleteSubtasks=false) {
		// /rest/api/2/issue/{issueIdOrKey}?deleteSubtasks
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&arguments.issue
			&(deleteSubtasks?"?deleteSubtasks":"");
		
		var body={'mimetype':'application/json'};


		var result=_http(path:path,method:"delete",body:body);

		if(result.status_code==200 || result.status_code==204) {
			return ;
		}
		else handleNon200(result,path);
	} 


	public function listIssues(string project="", array fields=[], string versionFrom="", string versionTo="", includeFrom=true, includeTo=true, array stati=[]) {
		local.hasFrom=false;
		local.hasTo=false;
		if(!isEmpty(arguments.versionFrom)) {
			local.versionFrom=toVersionSortable(arguments.versionFrom);
			local.hasFrom=true;
		}	
		if(!isEmpty(arguments.versionTo)) {
			local.versionTo=toVersionSortable(arguments.versionTo);
			local.hasTo=true;
		}

		var data =_listIssues(project:arguments.project, fields:arguments.fields, stati:arguments.stati);
		var issues=data.issues;
		var colNames=queryColumnArray(issues);
		var filtered=queryNew(colNames);
		queryAddColumn(filtered, "version", []);
		queryAddColumn(filtered, "versionSorted", []);
		loop query=issues {
			loop array=issues.fixVersions item="local.raw" {
				try {
					local.v=toVersionSortable(raw);
					if((!hasFrom || (includeFrom && v>=versionFrom ) || (!includeFrom && v>versionFrom )  ) && (!hasTo ||  (includeTo && v<=versionTo ) ||  (!includeTo && v<versionTo )  )) {
						var row=queryAddRow(filtered);
						querySetCell(filtered, "version", raw,row);
						querySetCell(filtered, "versionSorted", v,row);
						loop array=colNames item="local.col" {
							querySetCell(filtered, col, queryGetCell(issues, col,issues.currentrow),row);
						}
						break;
					}
				}
				catch(e) {
				}
			}
		}

		querySort(filtered,"versionSorted");


		return filtered;
	}

	private function _listIssues(string project="", array fields=[], array stati=[]) {
		var start=getTickCount();
		// if we have a cached we check when it has changed the last time;
		var last=lastChanged(project);
		var key=CACHE_KEY&":"&project&":"&arrayToList(fields)&":"&arrayToList(stati);
		
		if(!isNull(static[key]) && static[key].lastChange==local.last)
			return static[key];
		
		var count=0;
		var startAt=0;
		var strFields=arrayLen(fields)?","&arrayToList(fields):"";
		var qry=queryNew("id,key,summary,self,type,created,updated,priority,status,fixVersions"&strFields);
		do {
			var data=_list(project,startAt,variables.MAX_RECORDS,"issuetype,created,updated,priority,status,summary,fixVersions"&strFields,stati);
			// fill query
			loop array=data.issues item="local.issue" label="outer" {

				fixVersions=[];
				var use=false;
				loop array=issue.fields.fixVersions item="local.fvData" {
					arrayAppend(fixVersions,fvData.name);
				}

				var row=queryAddRow(qry);
				querySetCell(qry,"id",issue.id);
				querySetCell(qry,"key",issue.key);
				querySetCell(qry,"self",issue.self);
				querySetCell(qry,"type",issue.fields.issuetype.name);
				querySetCell(qry,"priority",issue.fields.priority.name);
				querySetCell(qry,"status",issue.fields.status.name);
				querySetCell(qry,"summary",issue.fields.summary);
				querySetCell(qry,"fixVersions",fixVersions);
				
				var created=issue.fields.created;
				querySetCell(qry,"created",isEmpty(created)?"":parseDateTime(created));
				
				var updated=issue.fields.updated;
				querySetCell(qry,"updated",isEmpty(updated)?"":parseDateTime(updated));
				if(arrayLen(fields)) {
					loop array=fields item="local.field" {
						querySetCell(qry,field,issue.fields[field]);
					}
				}
				
			}

			startAt+=data.maxResults;
			if(count++>1000) break;
		}
		while(data.total> data.startAt+data.maxResults);
		static[key]['lastChange']=local.last;
		static[key]['issues']=qry;
		return static[key];
	}


	private function _list(required string project, required numeric startAt, 
		required numeric max, string fields, array stati=[] flush=false) {

		var jql=isEmpty(arguments.project)?'':'project = '&arguments.project;
		if(arrayLen(stati)) jql&=" AND status in ("&arrayToList(stati,", ")&")";
		var jql&=' ORDER BY key DESC';
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/search"
			&"?maxResults="&max 
			&"&startAt="&arguments.startAt
			&"&fields="&arguments.fields
			&"&jql="&jql
		;
		var result=_http(path);
		

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	}






///////////////////////////////////////////////////////////////////////////
//////////////////////////////// OTHERS ////////////////////////////////////
///////////////////////////////////////////////////////////////////////////
	

	public function field() {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/field/";
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return arrayStruct2query(data); 
			return data;
		}
		else handleNon200(result,path);
	}

	public datetime function lastChanged(string project="") {
			var data=_list(project:project,startAt:0,max:1,fields:"created,updated");
			if(data.total==0) return createDateTime(2009,4,13,17,06,0);// no issues we return a date in the far past
			var fields=data.issues[1].fields;
			var lastChange=isEmpty(fields.updated)?parseDateTime(fields.created):parseDateTime(fields.updated);

			return lastChange;
	}

	public function createMeta(projectKeys,projectIds,issuetypeIds) {

		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/createmeta";

			var qs=[];

			if(!isNull(arguments.projectKeys)) arrayAppend(qs,"projectKeys="&arguments.projectKeys);
			if(!isNull(arguments.projectIds)) arrayAppend(qs,"projectIds="&arguments.projectIds);
			if(!isNull(arguments.issuetypeIds)) arrayAppend(qs,"issuetypeIds="&arguments.issuetypeIds);
			if(!isNull(arguments.issuetypeNames)) arrayAppend(qs,"issuetypeNames="&arguments.issuetypeNames);


			if(arrayLen(qs)) {
				path&="?"&arrayToList(qs,'&');
			}
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	}

	public function editmeta(issue) {

		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issue/"
			&issue
			&"/editmeta";
		
		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	} 

	public function listProjects() {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/project";

		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);

			return arrayStruct2query(data); 
		}
		else handleNon200(result,path);
	}

	public function status() {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/status";

		var result=_http(path);
		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path);
	}
	
	public function dashboard() {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/dashboard";

		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else handleNon200(result,path); 
	}
	
	public function getIssueLinkTypes() {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/issueLinkType";

		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			data=arrayStruct2query(data.issueLinkTypes);
			return data;
		}
		else handleNon200(result,path); 
	}

	
	
	public function hasCredentials() {
		return !isNull(variables.credentials.username);
	}

	public function auth() {

		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.authPath
			&"/session"; 

			///rest/auth/1/session

		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else {
			handleNon200(result,path);
		}
	}

	public function user(required string username) {
		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.apiPath
			&"/user?username="&arguments.username;

		var result=_http(path);

		if(result.status_code==200) {
			var data=deserializeJson(result.fileContent);
			return data;
		}
		else {
			handleNon200(result,path);
		}
	}



///////////////////////////////////////////////////////////////////////////
//////////////////////////////// HELPERS //////////////////////////////////
///////////////////////////////////////////////////////////////////////////


	private function authIfNecessary() { 
		// authenticate
		if(isNull(variables.credentials.username)) return;

		var path= (variables.secure?"https":"http")
			&"://"
			&variables.domain
			&variables.authPath
			&"/session?login";

		var body={
			'mimetype':'application/json'
			,'data':{'username':variables.credentials.username,'password':variables.credentials.password}
		};
		
		var result=_http(path,"post",body,false);

		if(result.status_code==200) {
			return variables.credentials.auth=deserializeJson(result.fileContent);
		}
		else {
			handleNon200(result,path);
		}
	}

	private function _http(required string path, required string method="get",struct body, boolean auth=true) {
		if(auth && !isNull(variables.lastAccess) && dateAdd("s", variables.maxInactiveInterval, variables.lastAccess)<=now()) {
			authIfNecessary();
		}

		//systemOutput("http:"&path,1,1);
		systemOutput("http:"&path,1,1);//&"<print-stack-trace>"

		if(isNull(arguments.body) || structCount(arguments.body)==0) {
			http 
				url=path 
				result="local.result" 
				method=arguments.method {
				if(!isNull(variables.credentials.auth.session)) {
					httpparam type="cookie" 
						name=variables.credentials.auth.session.name
						value=variables.credentials.auth.session.value;
				}
			}
		}
		else {
			http 
				url=path 
				result="local.result" 
				method=arguments.method {
					if(!isNull(variables.credentials.auth.session)) {
						httpparam type="cookie" 
							name=variables.credentials.auth.session.name
							value=variables.credentials.auth.session.value;
					}
					if(!isNull(body.data)) {
						httpparam
						    type="body"
						    value=serializeJson(body.data)
						    mimetype=body.mimetype;
					}
					httpparam
					    type="header"
					    name="Content-Type"
					    value=body.mimetype;
			}
		}
		variables.lastAccess=now();
			
		
		return result;
	}
	
	private function throwException(required string raw,required string path) {
		try {
			var data=deserializeJson(raw);
			cfthrow(message=data.errorMessages[1], detail=path&"
				"&raw);
		}
		catch(e) {
			cfthrow(message=path&"
				"&raw);
		}
	}

	private function handleNon200(result,path) {
		if(result.status_code==0) {
			throw "was not able to connect to "&path;
		}
		else {
			local.detail="URL:"&variables.NL&path&variables.NL&variables.NL&"Response Headers:";
			loop struct=result.responseheader index="local.k" item="local.v" {
				if(!isSimpleValue(v)) {
					if(isArray(v))v=arrayToList(v,',');
					else v=serialize(v);
				}
				detail&=variables.NL&k&" = "&v;
			} 

			var exp={};
			try {
				var data=deserializeJson(result.fileContent);
				exp.message=data.errorMessages[1];
				exp.detail=detail;
				
			}
			catch(e) {
				exp.message=path&"
					"&result.fileContent;
				exp.detail=detail;
			}
			cfthrow(message=exp.message, detail=exp.detail);

		}
	}

	private function improveType(data) { 
		if(!isSimpleValue(data)) return data;
		/*try {
			data=parseDateTime(data);
		}
		catch(e) {}*/
		return data;
	}

	public function worklogAsQuery(data) {

		var qry=arrayStruct2query(data);
		queryDeleteColumn(qry,"issueId");
		loop query=qry {
			// author
			tmp=qry.author;
			qry.author=tmp.key?:tmp.name;
			//updateAuthor
			tmp=qry.updateAuthor;
			qry.updateAuthor=tmp.key?:tmp.name;
		}
		return qry;
	}
	

	public function arrayStruct2query(data) { 
		if(isNull(data) || arrayLen(data)==0) return ;

		var columns=structKeyList(data[1]);
		var arrColumns=listToArray(columns);
		var qry=queryNew(columns);
		loop array=data item="local.rowData" {
			var row=queryAddRow(qry);
			loop array=structKeyArray(rowData) item="local.col" {
				if(!queryColumnExists(qry,col))queryAddColumn(qry,col);
				querySetCell(qry,col,rowData[col],row);
			}
		}
		return qry;
	}

	private function toVersionSortable(string version){
		local.arr=listToArray(arguments.version,'.');
		
		if(arr.len()!=4 || !isNumeric(arr[1]) || !isNumeric(arr[2]) || !isNumeric(arr[3])) {
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
		else {
			sct.qualifier=qArr[1]+0;
			sct.qualifier_appendix_nbr=75;
		}


		return 		repeatString("0",2-len(sct.major))&sct.major
					&"."&repeatString("0",3-len(sct.minor))&sct.minor
					&"."&repeatString("0",3-len(sct.micro))&sct.micro
					&"."&repeatString("0",4-len(sct.qualifier))&sct.qualifier
					&"."&repeatString("0",3-len(sct.qualifier_appendix_nbr))&sct.qualifier_appendix_nbr;
	}


}