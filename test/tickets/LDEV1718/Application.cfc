component {
    this.name="ldev1718test";
    tmp = getTempDirectory();
    isWindows =find("Windows", server.os.name );
	root = isWindows ? "c:\" : "/";
    if (isWindows){
        folder = listGetAt(getCurrentTemplatePath(), 2, ":/\");
    } else {
        folder = listFirst(getCurrentTemplatePath(), "/\");
    }
    dump(folder);
    testTempFolder = "ldev1718test-" & createUniqueID();
    request.testTempFolder = folder;

    directoryCreate(tmp & testTempFolder);
    mapping = tmp & testTempFolder & "/" & folder;
    directoryCreate(testTempFolder & "/" & folder);
    dump({
        "#folder#": mapping // add a mapping which matches the current root directory
    });
    this.mappings = {
        "#folder#": mapping // add a mapping which matches the current root directory
    }; 
}