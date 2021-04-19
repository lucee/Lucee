component {

	public any function init( required String directory, Boolean recursive = false ) {
		variables.recursive = arguments.recursive;
		var file = CreateObject( "java", "java.io.File" ).init( arguments.directory );
		if ( !file.isDirectory() ) {
			throw( "Directory '#arguments.directory#' does not exist or is not a directory" );
		}
		var dir = CreateObject( "java", "java.nio.file.Paths" );
		variables.path = dir.get( arguments.directory, [] );

		variables.watcher = CreateObject( "java", "java.nio.file.FileSystems" ).getDefault().newWatchService();
		variables.events = CreateObject( "java", "java.nio.file.StandardWatchEventKinds" );
		variables.pathKeys =  CreateObject( "java", "java.util.HashMap" ).init();

		path.register( variables.watcher, [
			variables.events.ENTRY_MODIFY,
			variables.events.ENTRY_DELETE,
			variables.events.ENTRY_CREATE
		] );
		return this;
	}

	private void function register( required any path, required Boolean recursive ) {
		var key = arguments.path.register(variables.watcher, [
			variables.events.ENTRY_CREATE,
			variables.events.ENTRY_MODIFY,
			variables.events.ENTRY_DELETE
		]);
		variables.pathKeys.put( key, arguments.path );
		if ( arguments.recursive ) {
			var files = arguments.path.toFile().listFiles();
			for (var file in files) {
				if ( file.isDirectory() ) {
					register( file.toPath(), arguments.recursive );
				}
			}
		}
	}

	public any function poll() {
		//writeLog( text="WatchService.Poll()", file="DirectoryWatcher" );
		var _poll = variables.watcher.poll();
		if ( IsNull (_poll) ){
			return [];
		} else {
			return handleEvents( _poll );
		}
	}

	public void function close() {
		//writeLog( text="WatchService.close()", file="DirectoryWatcher" );
		variables.watcher.close();
		variables.watcher = false;
	}

	private function utcTimeToDate( time ) {
		return dateAdd( "l", arguments.time, 0 );
	}

	private Array function handleEvents( required any key ) {
		//writeLog( text="WatchService.handleEvents", file="DirectoryWatcher" );
		var changes = [];
		var path = variables.pathKeys.get( arguments.key );
		if ( !IsNull( path ) ) {
			for ( var event in arguments.key.pollEvents() ) {
				var kind = event.kind();
				//writeLog( text="#Serialize(kind)#", type="error");
				if ( kind !== variables.events.OVERFLOW ) {
					var relativePath = event.context();
					// in the case of ENTRY_CREATE, ENTRY_DELETE, and ENTRY_MODIFY events the context is a Path
					// that is the relative path between the directory registered with the watch service, and the entry that is created, deleted, or modified.
					var affectedPath = path.resolve( relativePath );
					var file = affectedPath.toFile();

					if ( variables.recursive && kind === variables.events.ENTRY_CREATE ) {
						if ( file.isDirectory() ) {
							register( affectedPath, variables.recursive );
						}
					}
					var action = "";
					if ( kind === variables.events.ENTRY_CREATE )
						action = "add";
					else if ( kind === variables.events.ENTRY_DELETE )
						action = "delete";
					else if ( kind === variables.events.ENTRY_MODIFY )
						action = "change";
					else
						throw "Unsupported event: [#kind.name()#]";

					changes.append( {
						"action" = action,
						"name" = file.getName(),
						"size" = file.length(),
						"dateLastModified" = utcTimeToDate( file.lastModified() ),
						"directory" = GetDirectoryFromPath( file.getAbsolutePath() )
					} );
				}
			}
		}
		return changes;
	}

}