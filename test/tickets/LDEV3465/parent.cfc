component {
    static {
        parentStatic = "static_variable_from_Parent";
    }
    public string function getDirectStaticVariable( required string name ) {
        return static[ arguments.name ];
    }
    public static function anotherStaticMethod() {
        return "From_another_static_method";
    }
    public static function staticMethod() {
        return anotherStaticMethod();
    }
}