/*  
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
 */
component output="no" {
	variables.logFileName = "DirectoryWatcher";
	variables.state="stopped";

	public void function init(required string id, required struct config, component listener) output=false {
		var cfcatch = "";
		try {
			variables.id=arguments.id;
			variables.config=arguments.config;
			if ( len(arguments.listener) == 0 ) {
				log text="init Listener is not a component" type="Error";
				return;
			}
			log text="init [#GetComponentMetaData(arguments.listener).path#]" type="information";
			variables.listener=arguments.listener;
		} catch (any cfcatch) {
			_handleError(cfcatch, "init");
		}
	}

	public void function start() output=false {
		var sleepStep = iif(variables.config.interval < 500, 'variables.config.interval', de(500));
		var i=-1;
		var cfcatch = "";
		var startTime = getTickCount();
		if ( !StructKeyExists(variables, "listener") ) {
			setState("stopped");
			return;
		}
		try {
			while (variables.state EQ "stopping"){
				sleep(10);
			}			
		
			setState("running");
			variables._filter = cleanExtensions(variables.config.extensions);

			log text="start Directory[#variables.config.directory#]";

			var funcNames={add:config.addFunction, change:config.changeFunction, delete:config.deleteFunction};
			try {
				/*  check if the directory actually exists

					https://luceeserver.atlassian.net/browse/LDEV-1767

					*/
				if ( !DirectoryExists(variables.config.directory) ) {
					log text="start Directory [#variables.config.directory#] does not exist or is not a directory" type="Error";
				}
			} catch (any cfcatch) {
				log text="poll Directory [#variables.config.directory#] DirectoryExists threw #cfcatch.message# #cfcatch.stacktrace#" type="Error";
				setState("stopped");
				return;
			}
			if ( !StructKeyExists(variables.config,"recurse") ) {
				variables.config.recurse=false;
			}
			var files=loadFiles(variables.config.directory, variables.config.recurse, variables._filter);
		} catch (any cfcatch) {
			_handleError(cfcatch, "start");
		}
		//  first execution 

		while (variables.state EQ "running"){
			if ( startTime == -1 ) {
				//  don't compare during first run, nothing will have changed at start 
				startTime = getTickCount();
				try {
					var coll=compareFiles(files,funcNames,config.directory, config.recurse, variables._filter);
					files=coll.data;
					var name="";
					var funcName="";
				} catch (any cfcatch) {
					_handleError(cfcatch, "start");
				}
				for ( name in coll.diff ) {
					try {
						funcName=coll.diff[name].action;
						if ( len(funcName) ) {
							variables.listener[funcName](coll.diff[name]);
						}
					} catch (any cfcatch) {
						_handleError(cfcatch, "start");
					}
				}
			}
			if ( variables.state != "running" ) {
				break;
			}
			//  large directories can take a while and involve heavy io 

			var executionTime = getTickCount() - startTime;
			var warningTimeout = 1000;
			if (structKeyExists(variables.config, "warningTimeout") )
				warningTimeout = variables.config.warningTimeout;
			if (warningTimeout gt 0 and executionTime gt warningTimeout)
				log text="poll Directory [#variables.config.directory#] took #(executionTime)#ms";
			startTime = -1;
			//  sleep untill the next run, but cut it into half seconds, so we can stop the gateway 
			for ( i=sleepStep ; i<=variables.config.interval ; i+#sleepStep# ) {
				sleep(sleepStep);
				if ( variables.state != "running" ) {
					break;
				}
			}
			//  some extra sleeping if 
			if ( variables.config.interval mod sleepStep && variables.state == "running" ) {
				sleep((variables.config.interval mod sleepStep));
			}
		}
		setState("stopped");
	}

	private struct function loadFiles(required string directory, boolean recurse="#false#", string fileFilter="*") output=false {
		var cfcatch = "";
		try {
			var dir = getFiles(arguments.directory, arguments.recurse, arguments.fileFilter);
			var sct={};	

			loop query="dir"{
				sct[dir.directory&server.separator.file&dir.name] = createElement(dir);
			}
			
			return sct;
		} catch (any cfcatch) {
			_handleError(cfcatch, "loadFiles");
		}
	}

	private query function getFiles(required string directory, boolean recurse="false", string fileFilter="*") output=false {
		var cfcatch = "";
		try {
			var qDir = "";
			cfdirectory filter=arguments.fileFilter directory=arguments.directory 
				recurse=arguments.recurse name="qDir", type="file", action="list";
			return qDir;
		} catch (any cfcatch) {
			_handleError(cfcatch, "getFiles");
		}
	}

	private struct function compareFiles(required struct last, required struct funcNames, 
			required string directory, boolean recurse="false", string fileFilter="*") output=false {
		var cfcatch = "";
		try {
			var dir = getFiles(arguments.directory, arguments.recurse, arguments.fileFilter);
			var sct={};
			var diff={};
			var name="";
			var tmp="";
			//  check for new and changed files 

			loop query="dir" {
				name=dir.directory & server.separator.file & dir.name;
				// populate the struct with all currently found files/directories
				sct[name]=createElement(dir);
					// file existed already 
				if ( StructKeyExists(arguments.last,name) ){
					// date last modified has changed? 
					if ( dir.dateLastModified NEQ arguments.last[name].dateLastModified) {
						tmp = createElement(dir);
						tmp.action = arguments.funcNames.change;
						diff[name] = tmp;
					}
					// new file 
				else {
					tmp=createElement(dir);
					tmp.action=funcNames.add;
					diff[name]=tmp;
				}
			}
			//  check if files are deleted 
			for ( name in last ) {
				if ( !StructKeyExists(sct,name) ) {
					last[name].action=funcNames.delete;
					diff[name]=last[name];
				}
			}
			return {data:sct,diff:diff};
		} catch (any cfcatch) {
			_handleError(cfcatch, "compareFiles");
		}
	}

	private struct function createElement(required query dir) output=false {
		return {dateLastModified:dir.dateLastModified, size:dir.size, name:dir.name, directory:dir.directory,id:variables.id};
	}

	public void function stop() output=false {
		log text="stop ";
		variables.setState("stopping");
	}

	public void function restart() output=false {
		log text="restart #arguments.newState#"; 
		if (state EQ "running")
			stop();
		start();
	}

	public void function setState(required string newState) output=false {
		log text="poll #arguments.newState#"; 
		switch (arguments.newState){
			case "stopping":					
			case "running":
			case "stopped":
				variables.state=arguments.newState;
				break;
			default:
				throw (message="Unknown state: #arguments.newState#");
		}
	}

	public string function getState() output=false {
		return variables.state;
	}

	public string function sendMessage(struct data) output=false {
		return "sendGatewayMessage() has !been implemented for the event gateway [DirectoryWatcher]. " 
			& "If you want to modify it, please edit the following CFC:"& expandpath("./") & "DirectoryWatcher.cfc";
	}

	private string function cleanExtensions(required string extensions) output=false {
		//  replace the commas and optional trailing spaces with pipes ("|"), because that's the delimiter cfdirectory works with. 
		return rereplace(trim(arguments.extensions), " *, *", "|", "all");
	}

	private void function _handleError(required catchData, string functionName="unknown") output=false {
		log type="error" text="restart Function #arguments.functionName#: #arguments.catchData.message#" 
			& "#arguments.catchData.detail# #arguments.catchData.stacktrace# ";
	}

	private void function log(required string text, required string type="information") output=false {
		cflog ( text="[#variables.id#] #arguments.text#", file=variables.logFileName, type=arguments.type );
	}
}