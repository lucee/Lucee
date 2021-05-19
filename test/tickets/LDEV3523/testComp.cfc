component {
    public any function testFunc(){
        var nullVar = nullValue(); // to do create Null value
        return isNull(nullVar)? nullValue() : nullVar;
    }
    public any function testFormScopeFunc(){
        var FormNullVar = nullValue(); // to do create Null value
        return isNull(FormNullVar)? nullValue() : FormNullVar;
    }
    public any function testScopeWithPrefix(){
        var NullVar = nullValue(); // to do create Null value
        return isNull(local.NullVar)? nullValue() : local.NullVar;
    }
}