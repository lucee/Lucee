component {

	public any function init(required String directory, Boolean recursive = false ) {

		var file = CreateObject("java", "java.io.File").init(arguments.directory);
		if (!file.isDirectory()) {
			throw("Directory '#arguments.directory#' does not exist or is not a directory");
		}
		var dir = CreateObject("java", "java.nio.file.Paths");
		var path = dir.get(arguments.directory,[]);


		variables.watcher = CreateObject("java", "java.nio.file.FileSystems").getDefault().newWatchService();
		variables.events = CreateObject("java", "java.nio.file.StandardWatchEventKinds");
		
		path.register(variables.watcher, [variables.events.ENTRY_MODIFY, variables.events.ENTRY_DELETE, variables.events.ENTRY_CREATE] );		
		return variables.watcher;
	}

	public any function poll() {
		var _poll = variables.watcher.poll();		
		dump(_poll);
		if (IsNull(_poll))
			return [];
		else	
			return handleEvents(_poll);
	}

	public any function take() {
		return variables.watcher.take();
	}

	public void function close() {
		variables.watcher.close();
	}

	private Array function handleEvents(required Any key) {

		var events = []
		if (!IsNull(arguments.key)) {
			var path = variables.keys.get(arguments.key)
			if (!IsNull(path)) {
				for (var event in arguments.key.pollEvents()) {
					var kind = event.kind()
					if (kind neq variables.events.OVERFLOW) {
						var relativePath = event.context()
						// in the case of ENTRY_CREATE, ENTRY_DELETE, and ENTRY_MODIFY events the context is a Path that is the relative path between the directory registered with the watch service, and the entry that is created, deleted, or modified.
						var affectedPath = path.resolve(relativePath)
						var file = affectedPath.toFile()

						if (variables.recursive && kind eq variables.events.ENTRY_CREATE) {
							if (file.isDirectory()) {
								register(affectedPath, variables.recursive)
							}
						}

						events.append({type: kind.name(), file: file});
					}
				}

				var valid = arguments.key.reset()
				if (!valid) {
					variables.keys.remove(arguments.key);
				}
			}
		}

		return events;
	}

	private void function register(required Any path, required Boolean recursive) {

		var key = arguments.path.register(variables.watcher, [
			variables.events.ENTRY_CREATE, 
			variables.events.ENTRY_MODIFY, 
			variables.events.ENTRY_DELETE
		]);
		variables.keys.put(key, arguments.path);	

		if (arguments.recursive) {
			var files = arguments.path.toFile().listFiles();
			for (var file in files) {
				if (file.isDirectory()) {
					register(file.toPath(), arguments.recursive);
				}
			}
		}

	}

}