// Serialiser.cfc
component {

    public function canSerialize(){
        // logArgs(args=arguments, from=getFunctionCalledName());
        return true;
    }

    public function canDeserialize(){
        // logArgs(args=arguments, from=getFunctionCalledName());
        return true;
    }

    public function serialize(){
        // logArgs(args=arguments, from=getFunctionCalledName());
        return "SERIALISED";
    }

    public function deserialize(){
        // logArgs(args=arguments, from=getFunctionCalledName());
        return "DESERIALISED";
    }

    private function logArgs(required struct args, required string from){
        var dumpFile = getDirectoryFromPath(getCurrentTemplatePath()) & "dump_#from#.html";
        if (fileExists(dumpFile)){
            fileDelete(dumpFile);
        }
        writeDump(var=args, label=from, output=dumpFile, format="html");
    }
}