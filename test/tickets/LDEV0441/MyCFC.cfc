component {
    function init( required string mixin ) {
        include arguments.mixin;
        this.foo = variables.foo;
    }
}