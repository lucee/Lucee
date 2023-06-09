component {
    this.name="ldev1718test";
    tmp = getTempDirectory();
    isWindows =find("Windows", server.os.name );
	root = isWindows ? "c:\" : "/";
    if (isWindows){
        folder = listGetAt( getCurrentTemplatePath(), 2, ":/\" );
    } else {
        folder = listFirst( getCurrentTemplatePath(), "/\" );
    }
    testTempFolder = "ldev1718test-" & createUniqueID();
    request.testTempFolder = folder;

    directoryCreate(tmp & testTempFolder);
    mappingDir = tmp & testTempFolder & "/" & createUniqueID();
    directoryCreate( mappingDir );
    this.mappings = {
        "/#folder#": mappingDir // add a mapping which matches the current directory top level name
    }; 
}