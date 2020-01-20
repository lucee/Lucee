<cfscript>
component extends="Debug" {
	NL="
";
	fields=array(
		
		
		
		group("Custom Debugging Output","Define what is outputted",3)

		


		,field("General Debug Information ","general",true,false,
				"Select this option to show general information about this request.","checkbox")
		
		,field("Unit","unit","millisecond",true,"the unit used to display the execution time.","select","millisecond,microsecond,nanosecond")
		
		,field("Minimal Execution Time","minimal","0",true,
				{_appendix:"microseconds",_bottom:"Execution times for templates, includes, modules, custom tags, and component method calls. Outputs only templates taking longer than the time (in microseconds) defined above."},"text40")
		
		
		
		,field("Scope Variables","scopes","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL",true,"Select this option to show the content of the corresponding Scope.","checkbox","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL")
		
		
	);
    
    /**
	* return the title of this debug type
	*/
	function getLabel() {
		return "Comment";
	}
	
	/**
	* return the description of this debug type
	*/
	function getDescription() {
		return "Outputs the debugging information as HTML Comment, only visible inside the HTML Source Code.";
	}
	
	/**
	* return the unique identifier for this debug type
	*/
	function getId() {
		return "lucee-comment";
	}
	
	string function readDebug(struct custom, struct debugging, string context){
		output(argumentcollection=arguments);
	}	
	
	/**
	* validates settings done by the user
	* @param custom settings done by the user to validate
	*/
	function onBeforeUpdate(struct custom) {
		
	}	
	
	/**
	* output the debugging information
	* @param custom settings done by the user
	*/
	function output(struct custom, struct debugging, string context="web") {
		var NL=variables.NL;
		if (not StructKeyExists(arguments.custom, "unit"))
		 	arguments.custom["unit"] = "millisecond";
		writeOutput("<!--"&NL);
 		echo("=================================================================================="&NL);
        echo("=========================== LUCEE DEBUGGING INFORMATION =========================="&NL);
 		echo("=================================================================================="&NL&NL);		
	// GENERAL
		if( isEnabled(custom,"general") ) {
			echo(server.coldfusion.productname);
			if(StructKeyExists(server.lucee,'versionName'))
				echo('('&server.lucee.versionName&')');
			
			echo(" "&ucFirst(server.coldfusion.productlevel));
			echo(" "&server.lucee.version);
			echo(' (CFML Version '&server.ColdFusion.ProductVersion&')');
			echo(NL);
			
			echo("Template: #htmlEditFormat(cgi.SCRIPT_NAME)# (#htmlEditFormat(getBaseTemplatePath())#)");
			echo(NL);
			
			echo("Time Stamp: #LSDateFormat(now())# #LSTimeFormat(now())#");
			echo(NL);
			
			echo("Time Zone: #getTimeZone()#");
			echo(NL);
			
			echo("Locale: #ucFirst(getLocale())#");
			echo(NL);
			
			echo("User Agent: #cgi.http_user_agent#");
			echo(NL);
			
			echo("Remote IP: #cgi.remote_addr#");
			echo(NL);
			
			echo("Host Name: #cgi.server_name#");
			echo(NL);
			
			if(StructKeyExists(server.os,"archModel") and StructKeyExists(server.java,"archModel")) {
				echo("Architecture: ");
				if(server.os.archModel NEQ server.os.archModel)
					echo("OS #server.os.archModel#bit/JRE #server.java.archModel#bit");
				else 
					echo("#server.os.archModel#bit");
				echo(NL);
			}
 		}
		
	// Pages
		var pages=duplicate(debugging.pages);
        if(structKeyExists(custom,"minimal") && custom.minimal>0) {
            for(var row=pages.recordcount;row>0;row--){
                if(pages.total[row]<custom.minimal*1000)
                    queryDeleteRow(pages,row);
            }
		}
        formatUnits(pages,['load','query','app','total'],custom.unit);
		print("Pages",array('src','count','load','query','app','total'),pages);
	 	
	// DATABASE
		if(debugging.queries.recordcount)
			print("Queries",array('src','line','datasource','name','sql','time','count'),debugging.queries);
			
	// TIMER
	 	if(debugging.timers.recordcount)
			print("Timers",array('template','label','time'),debugging.timers);
	
	// TRACING
	 	if(debugging.traces.recordcount)
			print("Trace Points",array('template','type','category','text','line','action','varname','varvalue','time'),debugging.traces);
		
	// EXCEPTION
		if(arrayLen(debugging.exceptions)) {
			var qry=queryNew("type,message,detail,template")
			var len=arrayLen(debugging.exceptions);
			QueryAddRow(qry,len);
			for(var row=1;row<=len;row++){
				local.sct=debugging.exceptions[row];
				QuerySetCell(qry,"type",sct.type,row);
				QuerySetCell(qry,"message",sct.message,row);
				QuerySetCell(qry,"detail",sct.detail,row);
				QuerySetCell(qry,"template",sct.tagcontext[1].template&":"&sct.tagcontext[1].line,row);
			}
			//dump(qry);
			print("Caught Exceptions",array('type','message','detail','template'),qry);
		}
        
        
	// SCOPES   
     	local.scopes=["Application","CGI","Client","Cookie","Form","Request","Server","Session","URL"];
		if(not structKeyExists(custom,"scopes"))custom.scopes="";
		if(len(custom.scopes)) {
        echo("=================================================================================="&NL);
        echo(" SCOPES"&NL);
        echo("=================================================================================="&NL);
        
            for(var i=1;i<=arrayLen(scopes);i++){
            	local.name=scopes[i];
                if(!listFindNoCase(custom.scopes,name)) continue;
            	var doPrint=true;
				try{
					local.scp=evaluate(name);
   				}
                catch(any e){
                	doPrint=false;
                }
                
                if(doPrint and structCount(scp)) {
                	echo(uCase(name)&" SCOPE"&NL);
                    var keys=structKeyArray(scp);
                    for(var y=1;y<=arrayLen(keys);y++){
                    	local.key=keys[y];
                    	echo("- "&key&"=");
                        if(IsSimpleValue(scp[key]))				echo(htmlEditFormat(scp[key]));
						else if(isArray(scp[key]))				echo('Array (#arrayLen(scp[key])#)');
						else if(isValid('component',scp[key]))	echo('Component (#GetMetaData(scp[key]).name#)');
						else if(isStruct(scp[key]))				echo('Struct (#StructCount(scp[key])#)');
						else if(IsQuery(scp[key]))				echo('Query (#scp[key].recordcount#)');
						else {
                        	echo('Complex type');
						}
                        echo(NL);
                    }
                }
                
            }
        }	
		writeOutput(NL& "-->");
	}
    
    
 	
	
	private function print(string title,array labels, query data) {
		var NL=variables.NL;
		// get maxlength of columns
		var lengths=array();
		var i=1;
		var y=1;
		var tmp=0;
		var total=1;
		var collen=arrayLen(labels);
		for(;i LTE collen;i=i+1) {
			lengths[i]=len(labels[i]);
			for(y=1;y LTE data.recordcount;y=y+1) {
			
				data[labels[i]][y]=trim(rereplace(data[labels[i]][y],"[[:space:]]+"," ","all"));
			
				tmp=len(data[labels[i]][y]);
				if(tmp GT lengths[i])lengths[i]=tmp;
			}
			lengths[i]=lengths[i]+3;
			total=total+lengths[i];
		}
		
		// now wrie out
		writeOutput(NL);
		writeOutput(RepeatString("=",total)&NL);
		writeOutput(ljustify(" "&ucase(title)&" " ,total));
		writeOutput(NL);
		writeOutput(RepeatString("=",total)&NL);
		for(y=1;y LTE collen;y=y+1) {
			writeOutput(ljustify("| "&uCase(labels[y])&" " ,lengths[y]));
		}
		writeOutput("|"&NL);
		
		for(i=1;i LTE data.recordcount;i=i+1) {
			writeOutput(RepeatString("-",total)&NL);
			for(y=1;y LTE collen;y=y+1) {
				writeOutput(ljustify("| "&data[labels[y]][i]&" " ,lengths[y]));
			}
			writeOutput("|"&NL);
		}
		writeOutput(RepeatString("=",total)&NL&NL);
 	}   
    
function formatUnits(query data,array columns, string unit){
	loop query="data" {
    	loop array="#columns#" index="local.col" {
        	if(listfirst(formatUnit(unit,data[col])," ") gt 0)data[col]=formatUnit(unit,data[col]);
    		else data[col]='-';
        }
    }
}
	

function formatUnit(string unit, numeric time ){
	if (time GTE 100000000)
    	return int(time/1000000)&" ms";
    else if (time GTE 10000000)
    	return (int(time/100000)/10)&" ms";
    else if (time GTE 1000000)
    	return (int(time/10000)/100)&" ms";
    else 
    	return (int(time/1000)/1000)&" ms";
}
}
</cfscript>

// if(unit EQ "millisecond")
//    return int(time/1000000)&" ms";
//  else if(unit EQ "microsecond")
//    return int(time/1000)&" #chr(181)#s";
//  else
//    return int(time)&" ns";