<!--- 
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 --->
 component output="no" extends="DirectoryWatcher" {

	variables.logFileName = "DirectoryWatcher";
	variables.state="stopped";

	public void function init  (required string id, required struct config, component listener) output="no" {
		var cfcatch = "";
		try {
			variables.id=arguments.id;
			variables.config=arguments.config;
			if (len(arguments.listener) eq 0){
				cflog (text="init #variables.id# Listener is not a component", 
					type="Error", file="#variables.logFileName#");
				return;
			}			
			cflog (text="init #variables.id# [#GetComponentMetaData(arguments.listener).path#]", 
				type="information", file="#variables.logFileName#");
			variables.listener=arguments.listener;                
		} catch (any) {
			 _handleError(cfcatch, "init");
		}
	}
	
	public void function start() output="no" {		
		try {			
			var sleepStep = iif(variables.config.interval lt 500, 'variables.config.interval', de(500));
			var i= - 1;
			var cfcatch = "" ;
			var startTime = getTickCount();
			if (not StructKeyExists(variables, "listener")){
				setState("stopped");
				return;
			}	
			while (variables.state EQ "stopping"){
				sleep(10);
			}
			setState("running");
			variables._filter = cleanExtensions(variables.config.extensions);

			log text="start Directory[#variables.config.directory#]";

			variables.methods = {
				ENTRY_CREATE: config.addFunction,
				ENTRY_MODIFY: config.changeFunction,
				ENTRY_DELETE: config.deleteFunction
			};

			try {
				// check if the directory actually exists see https://luceeserver.atlassian.net/browse/LDEV-1767 
				if ( not DirectoryExists(variables.config.directory) )
					log text="start #variables.id# Directory [#variables.config.directory#] does not exist or is not a directory" 
						type="Error";				
			} catch (any){
				log text="poll Directory [#variables.config.directory#] DirectoryExists threw #cfcatch.message# #cfcatch.stacktrace#" 	
					type="Error";
				setState("stopped");
				return;				
			}
			if (not StructKeyExists(variables.config,"recurse"))
				variables.config.recurse = false;                
		} catch (any) {
			_handleError(cfcatch, "start");
		}		
		// first execution 		
		i = 0;
		variables.watcher = new WatchService(variables.config.directory, variables.config.recurse, variables.methods);
		
		while (variables.state EQ "running"){			
		//	if (startTime eq -77){				
				startTime = getTickCount();
				if (variables.state NEQ "running")
					break;
				var poll = variables.watcher.poll();		
				if (!IsNull(poll)){				
					cflog (text="not null", type="Information", file="#variables.logFileName#");						
				

					dump(poll); // work in progess
					abort;
					
					var events = poll.pollEvents();
					dump(events);
					for (var event in events) {
						var method = variables.methods[event.type];
						variables.listener[method](event.file);
					}
				} else {
					cflog (text="null", type="Information", file="#variables.logFileName#");												
				}					
				
				// large directories can take a while and involve heavy io
				var executionTime = getTickCount() - startTime;
				var warningTimeout = 1000;
				if (structKeyExists(variables.config, "warningTimeout") )
					warningTimeout = variables.config.warningTimeout;
				//if (warningTimeout gt 0 and executionTime gt warningTimeout)
					log text="poll #variables.id# Directory [#variables.config.directory#] took #(executionTime)#ms";
//              } else {
				log text="next poll in #variables.config.interval#ms #sleepStep#";					
//			}		
			
			startTime = -1; // trigger poll next time					
		
			// sleep untill the next run, but cut it into half seconds, so we can stop the gateway                
			for (var i=sleepStep; i lt variables.config.interval; i=i+sleepStep){
				sleep(sleepStep);
				if (variables.state neq "running")
					break;				    
			}
			<!--- some extra sleeping if --->
			if (variables.config.interval mod sleepStep and variables.state eq "running")
				sleep((variables.config.interval mod sleepStep));			    
		}
		variables.watcher.close();
		setState("stopped")
	}
}