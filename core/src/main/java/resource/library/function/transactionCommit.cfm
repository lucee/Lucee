<cfscript>
    /**
     * commits a pending transaction
     */
    public void function TransactionCommit() {
        transaction action="commit";
    }
</cfscript>