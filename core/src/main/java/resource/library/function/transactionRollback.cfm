<cfscript>
    /**
     * rolls back a pending transaction
     * @savepoint name of the savepoint to roll back to, if not set, simpy roles back to to the latest savepoint set without a name and if no savepoint is set, it roles back the complete transaction.
     */
    public void function TransactionRollBack(string savepoint="") {
        transaction action="rollback" savepoint=arguments.savepoint;
    }
</cfscript>