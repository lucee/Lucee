/**
 * Functional Interface that maps to java.util.function.Function
 * See https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html
 */
component{ 

    /**
     * Constructor
     *
     * @f The lambda or closure to be used in the <code>apply()</code> method
     */
    function init( required f ){
        variables.target = arguments.f;
        return this;
    }

    /**
     * Represents a function that accepts one argument and produces a result. 
     */
    function apply( t ){
        return variables.target( t );
    }

    function andThen( after ){}

    function compose( before ){}

    function identity(){}

}