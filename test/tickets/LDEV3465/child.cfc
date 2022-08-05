component extends="parent"{  
    public string function getStaticVariable(  required string name  ) {
        return static[ arguments.name ];
    }
}