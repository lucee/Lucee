component {
    this.name="ldev-4582-#url.name#";

    logDir = getDirectoryFromPath(getCurrentTemplatePath()) & "logs";

    systemOutput( "mapping: /logs :  #logDir#", true);

    this.mappings = {
        "/logs" : logDir
    }
}