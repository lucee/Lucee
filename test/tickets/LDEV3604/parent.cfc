component {

    variables.instance = {};

    function init() {
		variables.mixins.setInstance = variables.setInstance;
    }

    function setFoo( value ) {
        variables.instance.foo = value;
    }

    function setInstance( newInstance ) {
        variables.instance = newInstance;
    }

    function clone() {
        var clone = new child();
		clone.setInstance = variables.mixins.setInstance;
        clone.setInstance( structCopy( variables.instance ) );
        return clone;
    }
    function cloneWithoutMixin() {
        var clonecfc = new child();
        clonecfc.setInstance( structCopy( variables.instance ) );
        return clonecfc;
    }
}