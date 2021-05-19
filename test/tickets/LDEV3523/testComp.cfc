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
        var prefixNullVar = nullValue(); // to do create Null value
        return isNull(local.prefixNullVar)? nullValue() : local.prefixNullVar;
    }
}