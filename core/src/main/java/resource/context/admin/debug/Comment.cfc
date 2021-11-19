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
			if( isEnabled(arguments.custom,"general") ) {
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

			if(isNull(arguments.debugging.pages)) 
				local.pages=queryNew('id,count,min,max,avg,app,load,query,total,src');
			else local.pages=arguments.debugging.pages;

			var hasQueries=!isNull(arguments.debugging.queries);
			if(!hasQueries) 
				local.queries=queryNew('name,time,sql,src,line,count,datasource,usage,cacheTypes');
			else local.queries=arguments.debugging.queries;

			if(isNull(arguments.debugging.exceptions)) 
				local.exceptions=[];
			else local.exceptions=arguments.debugging.exceptions;

			if(isNull(arguments.debugging.timers)) 
				local.timers=queryNew('label,time,template');
			else local.timers=arguments.debugging.timers;

			if(isNull(arguments.debugging.traces)) 
				local.traces=queryNew('type,category,text,template,line,var,total,trace');
			else local.traces=arguments.debugging.traces;

			if(isNull(arguments.debugging.dumps)) 
				local.dumps=queryNew('output,template,line');
			else local.dumps=arguments.debugging.dumps;

			if(isNull(arguments.debugging.implicitAccess)) 
				local.implicitAccess=queryNew('template,line,scope,count,name');
			else local.implicitAccess=arguments.debugging.implicitAccess;

			if(isNull(arguments.debugging.dumps)) 
				local.dumps=queryNew('output,template,line');
			else local.dumps=arguments.debugging.dumps;

			local.times=arguments.debugging.times;


			
		// Pages
			if(structKeyExists(arguments.custom,"minimal") && arguments.custom.minimal>0) {
				for(var row=pages.recordcount;row>0;row--){
					if(pages.total[row]<custom.minimal*1000)
						queryDeleteRow(pages,row);
				}
			}
			if(pages.recordcount) {
				
				if(hasQueries)local.cols=array('src','count','load','query','app','total');
				else local.cols=array('src','count','load','app','total');

				formatUnits(pages,['load','query','app','total'],arguments.custom.unit);
				print("Pages",cols,pages);
			}
			else {
				var times=arguments.debugging.times;
				var exe=query('application':[times.total-times.query],'query':[times.query],'total':[times.total]);
				var cols=['application','query','total'];
				formatUnits(exe,cols,arguments.custom.unit);
				print("Execution Time",cols,exe);
			}
		// DATABASE
			if(queries.recordcount)
				print("Queries",array('src','line','datasource','name','sql','time','count'),queries);
				
		// TIMER
			 if(timers.recordcount)
				print("Timers",array('template','label','time'),timers);
		
		// TRACING
			 if(traces.recordcount)
				print("Trace Points",array('template','type','category','text','line','action','varname','varvalue','time'),traces);
			
		// EXCEPTION
			if(arrayLen(exceptions)) {
				var qry=queryNew("type,message,detail,template")
				var len=arrayLen(exceptions);
				QueryAddRow(qry,len);
				for(var row=1;row<=len;row++){
					local.sct=exceptions[row];
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
			if(not structKeyExists(arguments.custom,"scopes"))arguments.custom.scopes="";
			if(len(arguments.custom.scopes)) {
			echo("=================================================================================="&NL);
			echo(" SCOPES"&NL);
			echo("=================================================================================="&NL);
			
				for(var i=1;i<=arrayLen(scopes);i++){
					local.name=scopes[i];
					if(!listFindNoCase(arguments.custom.scopes,name)) continue;
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
			var collen=arrayLen(arguments.labels);
			for(;i LTE collen;i=i+1) {
				lengths[i]=len(arguments.labels[i]);
				for(y=1;y LTE arguments.data.recordcount;y=y+1) {
				
					arguments.data[arguments.labels[i]][y]=trim(rereplace(arguments.data[arguments.labels[i]][y],"[[:space:]]+"," ","all"));
				
					tmp=len(arguments.data[arguments.labels[i]][y]);
					if(tmp GT lengths[i])lengths[i]=tmp;
				}
				lengths[i]=lengths[i]+3;
				total=total+lengths[i];
			}
			
			// now wrie out
			writeOutput(NL);
			writeOutput(RepeatString("=",total)&NL);
			writeOutput(ljustify(" "&ucase(arguments.title)&" " ,total));
			writeOutput(NL);
			writeOutput(RepeatString("=",total)&NL);
			for(y=1;y LTE collen;y=y+1) {
				writeOutput(ljustify("| "&uCase(arguments.labels[y])&" " ,lengths[y]));
			}
			writeOutput("|"&NL);
			
			for(i=1;i LTE arguments.data.recordcount;i=i+1) {
				writeOutput(RepeatString("-",total)&NL);
				for(y=1;y LTE collen;y=y+1) {
					writeOutput(ljustify("| "&arguments.data[arguments.labels[y]][i]&" " ,lengths[y]));
				}
				writeOutput("|"&NL);
			}
			writeOutput(RepeatString("=",total)&NL&NL);
		 }   
		
	function formatUnits(query data,array columns, string unit){
		loop query="arguments.data" {
			loop array="#arguments.columns#" index="local.col" {
				arguments.data[col]=formatUnit(arguments.unit,arguments.data[col]);
			}
		}
		writeOutput(RepeatString("=",total)&NL&NL);
 	}   
    
function formatUnits(query data,array columns, string unit){
	loop query="arguments.data" {
    	loop array="#arguments.columns#" index="local.col" {
        	if(listfirst(formatUnit(arguments.unit,arguments.data[col])," ") gt 0)data[col]=formatUnit(arguments.unit,arguments.data[col]);
    		else arguments.data[col]='-';
        }
    }
}
	

function formatUnit(string unit, numeric time ){
	if (arguments.time GTE 100000000)
    	return int(arguments.time/1000000)&" ms";
    else if (arguments.time GTE 10000000)
    	return (int(arguments.time/100000)/10)&" ms";
    else if (arguments.time GTE 1000000)
    	return (int(arguments.time/10000)/100)&" ms";
    else 
    	return (int(arguments.time/1000)/1000)&" ms";
}
}
</cfscript>