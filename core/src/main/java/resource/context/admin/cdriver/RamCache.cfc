component extends="Cache" {
	
    fields=[
		group("Time Management",""),
		field("Time to idle in seconds","timeToIdleSeconds","0",true,"Sets the time to idle for an element before it expires. If all fields are set to 0 the element live as long the server live.","time"),
		field("Time to live in seconds","timeToLiveSeconds","0",true,"Sets the timeout to live for an element before it expires. If all fields are set to 0 the element live as long the server live.","time"),
		group("Memory Management",""),
		field("Disable out of memory handling","outOfMemory","false",false,"In case the engine runs out of memory, 
			Lucee will get rid of elements in the Cache in order to survive. In case you wan't to PREVENT this, check this flag.","checkbox","true")
		
	];
    
    public string function getClass() {
    	return "lucee.runtime.cache.ram.RamCache";
    }

    public string function getLabel() {
    	return "RamCache";
    }

    public string function getDescription() {
    	return "Create a Ram Cache (in Memory Cache).";
    }
}