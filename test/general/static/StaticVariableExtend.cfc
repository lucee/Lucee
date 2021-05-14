// LDEV-3465
component extends="StaticVariableBase" {

    function test() {
        return variables.static.foo;
    }

}