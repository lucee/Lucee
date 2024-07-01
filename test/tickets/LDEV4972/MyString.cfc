component {
    variables.text="";
    
    function init(text="") {
        variables.text=text;
    }

    function length() {
        return len(variables.text);
    }
}
component name="Sub" {
    variables.text="";
    
    function init(text) {
        variables.text=text;
    }

    function length() {
        return len(variables.text);
    }
}