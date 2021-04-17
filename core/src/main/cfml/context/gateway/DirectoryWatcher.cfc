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

	variables.logFileName = "directoryWatcher";
	variables.state = "stopped";
	variables.allChangesHandler = false;
	variables.warningTimeout = 1000; // ms
	variables.validConfig = false;

	public void function init(required string id, required struct config, component listener) output=false {
		var cfcatch = "";
		try {
			variables.id = arguments.id;
			variables.config = arguments.config;
			variables.useNIOWatcher = arguments.config.useNIOWatcher ?: false;
			variables.verboseLogging = arguments.config.verboseLogging ?: false;
			if ( len( arguments.listener ) == 0 ) {
				logger( "init Listener is not a component", "Error" );
				return;
			}
			logger( "init [#GetComponentMetaData( arguments.listener ).path#], useNIOWatcher: #variables.useNIOWatcher#" );
			variables.listener = arguments.listener;

			if ( structKeyExists( variables.config, "warningTimeout" ) )
				variables.warningTimeout = arguments.config.warningTimeout;

			if ( !StructKeyExists( variables.config, "recurse" ) )
				variables.config.recurse = false;

			variables._filter = cleanExtensions( variables.config.extensions );

			variables.eventHandlers = {};
			loop list="addFunction,changeFunction,deleteFunction,changesFunction" item="local.f" {
				if ( structKeyExists(arguments.config, f) && len( trim( arguments.config[ f ] ) ) gt 0 )
					variables.eventHandlers[ replaceNoCase( f, "function", "" ) ] = trim( arguments.config[ f ] );
			}
			if ( eventHandlers.count() eq 0){
				logger ( text="No event handlers configured!", type="error" );
				structDelete( variables, "listener" ); // nothing to do
				return;
			}

			if ( structKeyExists( variables.eventHandlers, "changes" ) )
				variables.allChangesHandler = true; // the listener has an onChanges method

			if ( variables.verboseLogging ){ // logout config
				loop collection=#variables# item="local.v"{
					if ( ! isCustomFunction( variables[v] ) )
						logger (v & ": " & serializeJson( variables[v] ));
				}
			}

			try {
				/*  check if the directory actually exists */
				if ( !DirectoryExists( variables.config.directory ) ) {
					logger( "Directory [#variables.config.directory#] does not exist or is not a directory", "Error" );
					setState( "stopped" );
					return;
				}
			} catch (any cfcatch) {
				logger( "poll Directory [#variables.config.directory#] DirectoryExists threw #cfcatch.message# #cfcatch.stacktrace#", "Error" );
				setState( "stopped" );
				return;
			}

			variables.validConfig = true;

		} catch (any cfcatch) {
			_handleError( cfcatch, "init" );
		}
	}

	// this is a loop which runs in a background thread, sleeping between intervals
	public void function start() output=false localmode=true{
		if ( !variables.validConfig ) {
			setState( "stopped" );
			logger( "Start(), invalid config, aborting" , "warning" );
			return;
		}

		logger( "Start(), watching Directory [#variables.config.directory#]" );

		// the loop sleeps every 500ms, so it wakes up so it can be interrupted, for shutdown etc
		var sleepStep = ( variables.config.interval < 500 ) ? variables.config.interval : 500; // ms
		var startTime = -1;
		try {
			while ( getState() EQ "stopping" ){
				logger( "waiting to stop, " & sleepStep );
				sleep( sleepStep + 1 );
			}

			setState( "running" );

			if ( variables.useNIOWatcher ){
				try {
					closeWatcher();
					// not supported on MacOS
					variables.watcher = new WatchService( variables.config.directory, variables.config.recurse );
				} catch (e){
					_handleError( cfcatch, "useNIOWatcher" );
					variables.useNIOWatcher = false; // fall back to cfdirectory
				}
			}
			if ( !variables.useNIOWatcher ){
				// may fall back to this  if WatchService not available, so no else if
				local.files = loadFiles( variables.config.directory, variables.config.recurse, variables._filter );
			}

		} catch (any cfcatch) {
			_handleError(cfcatch, "startup");
		}
		//  first execution
		while ( getState() EQ "running"){

			if ( startTime eq -1 ) {
				//  don't compare during first run, nothing will have changed at the first execution
				startTime = getTickCount();
			} else {
				logger( "Polling for changes" );
				startTime = getTickCount();
				if ( variables.allChangesHandler ) {
					local.allChanges = {
						add: [],
						change: [],
						delete: []
					};
				}
				if ( variables.useNIOWatcher ){
					// file system event driven, low overhead
					try {
						var events = variables.watcher.poll();
						for ( var event in events ){
							triggerEventHandler( event.action, event );
							if ( variables.allChangesHandler )
								local.allChanges[ event.action ].append( event );
						}
					} catch ( e ){
						_handleError( cfcatch, "pollEvents()" );
						//variables.watcher.close();
						break;
					}
				} else {
					// old skool cfdirectory, slower
					try {
						var coll = compareFiles( files, config.directory, config.recurse, variables._filter );
						files = coll.data; // stash for the next cycle, to compare against
						for ( var name in coll.diff ) {
							triggerEventHandler( coll.diff[ name ].action, coll.diff[ name ] );
							if ( variables.allChangesHandler )
								local.allChanges[ coll.diff[ name ].action ].append( coll.diff[ name ] );
						}
					} catch ( e ) {
						_handleError( cfcatch, "compareFiles()" );
					}
				}
				if ( variables.allChangesHandler
							&& ( local.allChanges.add.len() || local.allChanges.change.len() || local.allChanges.delete.len() ) ){
					triggerEventHandler( "changes", local.allChanges );
				}
				logger( "polled" );
			}
			if ( getState() != "running" ) {
				break;
			}

			if ( startTime gt 0 ){
				//  large directories can take a while and involve heavy io
				var executionTime = getTickCount() - startTime;
				if ( variables.warningTimeout gt 0 and executionTime gt variables.warningTimeout )
					logger( "Polling Directory [#variables.config.directory#] took #(executionTime)#ms", "warning" );
			}

			//  sleep until the next run, but cut it into half seconds, so we can stop the gateway
			for ( var i = 0 ; i <= variables.config.interval ; i =  i + sleepStep ) {
				logger( "Sleeping for: " & sleepStep & " ( " & i & " / " & variables.config.interval & " )");
				sleep( sleepStep );
				// i = i + sleepStep;
				if ( getState() != "running" ) {
					break;
				}
			}
			//  some extra sleeping if
			if ( variables.config.interval mod sleepStep && getState() == "running" ) {
				logger( "extra sleeping", "debug" );
				sleep( (variables.config.interval mod sleepStep) );
			}
		}
		closeWatcher();
		setState( "stopped" );
	}

	public any function triggerEventHandler ( required string method, struct file ){
		if ( structKeyExists( variables.eventHandlers, arguments.method ) )
			variables.listener[ variables.eventHandlers[ arguments.method ] ]( arguments.file );
		else
			logger("No handler found for #method#", "warning");
	}

	private struct function loadFiles( required string directory, boolean recurse="#false#", string fileFilter="*" ) output=false {
		var cfcatch = "";
		try {
			var dir = getFiles( arguments.directory, arguments.recurse, arguments.fileFilter );
			var files = [=];
			loop query="dir" {
				files[ dir.directory & server.separator.file & dir.name ] = createFileInfo( dir );
			}
			return files;
		} catch (any cfcatch) {
			_handleError( cfcatch, "loadFiles" );
			return {};
		}
	}

	private query function getFiles(required string directory, boolean recurse="false", string fileFilter="*") output=false {
		var cfcatch = "";
		try {
			var qDir = "";
			directory filter=arguments.fileFilter directory=arguments.directory recurse=arguments.recurse name="qDir", type="file", action="list";
			return qDir;
		} catch ( any cfcatch ) {
			_handleError( cfcatch, "getFiles" );
		}
	}

	private struct function compareFiles( required struct last, required string directory, boolean recurse="false", string fileFilter="*" ) output=false {
		var cfcatch = "";
		try {
			logger( "polling Directory [#variables.config.directory#]" );
			var dir = getFiles( arguments.directory, arguments.recurse, arguments.fileFilter );
			var sct = {};
			var diff = {};
			var name = "";
			var tmp = "";
			//  check for new and changed files

			loop query="dir" {
				name= dir.directory & server.separator.file & dir.name;
				// populate the struct with all currently found files/directories
				sct[ name ] = createFileInfo(dir);
					// file existed already
				if ( StructKeyExists( arguments.last, name ) ){
					// date last modified has changed?
					if ( dir.dateLastModified NEQ arguments.last[ name ].dateLastModified ) {
						tmp = createFileInfo( dir );
						tmp.action = "change";
						diff[ name ] = tmp;
					}
					// new file
				} else {
					tmp = createFileInfo( dir );
					tmp.action = "add";
					diff[ name ] = tmp;
				}
			}
			//  check if files are deleted
			for ( name in last ) {
				if ( !StructKeyExists( sct, name ) ) {
					last[ name ].action = "delete";
					diff[ name ] = last[ name ];
				}
			}
			return {
				data: sct,
				diff: diff
			};
		} catch ( e ) {
			_handleError( cfcatch, "compareFiles" );
		}
	}

	private struct function createFileInfo( required query dir ) output=false {
		return {
			"name" = dir.name,
			"size" = dir.size,
			"dateLastModified" = dir.dateLastModified,
			"directory" = dir.directory,
			"id" = variables.id
		};
	}

	public void function closeWatcher() output=false {
		if ( structKeyExists( variables, "watcher" ) ) {
			variables.watcher.close();
			structDelete( variables, "watcher" );
		}
	}

	public void function stop() output=false {
		logger( "stop()" );
		if ( getState() eq "running" and getState() neq "stopping" )
			variables.setState( "stopping" );
		else {
			closeWatcher();
			variables.setState( "stopped" );
		}
	}

	public void function restart() output=false {
		logger( "restart()" );
		stop(); // this just sets a flag
		start();
	}

	public void function setState( required string newState ) output=false {
		logger( "Change state to [#arguments.newState#]" );
		switch ( arguments.newState ){
			case "stopping":
			case "running":
			case "stopped":
				variables.state = arguments.newState;
				break;
			default:
				throw (message="Unknown state: #arguments.newState#");
		}
	}

	public string function getState() output=false {
		return variables.state;
	}

	public string function sendMessage( struct data ) output=false {
		return "sendGatewayMessage() has !been implemented for the event gateway [DirectoryWatcher]. "
			& "If you want to modify it, please edit the following CFC:"& expandpath("./") & "DirectoryWatcher.cfc";
	}

	private string function cleanExtensions( required string extensions ) output=false {
		//  replace the commas and optional trailing spaces with pipes ("|"), because that's the delimiter cfdirectory works with.
		return reReplace( trim( arguments.extensions ), " *, *", "|", "all" );
	}

	private void function _handleError( required catchData, string functionName="unknown" ) output=false {
		logger( "ERROR in function #arguments.functionName#: #arguments.catchData.message#"
			& "#arguments.catchData.detail# #arguments.catchData.stacktrace# ", "error" );
	}

	private void function logger( required string text, required string type="information" ) output=false {
		if ( arguments.type == "information" && !variables.verboseLogging )
			return;
		local.stack = variables.verboseLogging ? ListLAst(ListGetAt(CallStackGet('string'),2,";"),"/\") : "";
		writeLog (
			text="[#variables.id#, #getState()#] #arguments.text# #local.stack#",
			file=variables.logFileName,
			type=arguments.type
		);
	}
}