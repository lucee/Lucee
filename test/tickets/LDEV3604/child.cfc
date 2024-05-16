component extends="parent" {
    
    function init() {
		super.init();
    }

    function getInstance() {
        return variables.instance;
    }

    function setInstance( newInstance ) {
        super.setInstance( newInstance );
    }

}