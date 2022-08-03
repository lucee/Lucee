<cfscript>
    /**
     * Saves a specific state within a transaction
     * @savepoint name of the savepoint to set, this argument is optional, if  ot set it creates a unnamed savepoint.
     */
    public void function TransactionSetsavepoint(string savepoint="") {
        transaction action="Setsavepoint" savepoint=arguments.savepoint;
    }
</cfscript>