// Redden.cfc
component {
    public boolean function onStartTag(required struct attributes, required struct caller){
        echo('<span style="color:red">#attributes.message#</span>');
        return true;
    }
