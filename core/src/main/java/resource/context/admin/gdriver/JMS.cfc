<cfcomponent extends="Gateway">

	
    <cfset fields=array(
		field("Provider URL","providerURL","",true,"This is the URL of the JMS provider. You can override a number of configuration parameters using a query string, for example, providerURL=tcp://localhost:61616?jms.redeliveryPolicy.maximumRedeliveries=20","text")
		,field("Initial Context Factory","initialContextFactory","",true,"This is the name of the class used to construct the initial JNDI context that is used to lookup the connection factory and destination.","text")
		,field("Connection Factory","connectionFactory","",true,"This is generally the name of the class used to construct the factory object that creates connections. For some providers, it is a c-name identifying the resource within some sort of directory, such as LDAP.","text")
		,field("Destination Name","destinationName","",true,"required for message consumption. This is the full name of a message destination to which the gateway should subscribe. This is not needed for outbound messages because the destination name is provided for each message. If the destination name is a full LDAP-style c-name, the gateway also constructs a short destination name, which is the base c-name converted to lowercase; everything after the first comma in the full c-name is discarded.","text")
		
		,field("Context Properties","contextProperties","",true,"comma separated list of addional context properties.","textarea")
		
		
		
		,field("Debug","debug","yes",true,"If [yes], the gateway generates verbose logging. The default is to only log problems.","radio","yes,no")
		,field("Outbound Only","outboundOnly","no",true,"If [yes], the gateway does not subscribe to a destination.","radio","yes,no")
		,field("Cachable","cachable","no",true,"If [yes], a single publisher connection is created and used for all outbound messages. This allows you to use vendor-specific settings that support long-lived connections, such as durable publishers. Those settings are provided via the contextProperties setting. If [no], a fresh publisher connection is created for each outbound message that is sent.","radio","yes,no")
		
		,group("Transacted Message Consumption","",2)
		,field("Transacted","transacted","no",true,"If [yes], a pool of consumers is created that handshake with the CFML code to create JMS transactions on separate sessions within the gateway's connection. CFML code must call commit() or rollback() to complete a transaction. See Transacted Message Consumption, above.","radio","yes,no")
		,field("Pool Size","poolSize","10",true,"This determines the size of the consumer pool for transaction.","text")
		
		,group("Timeout","",3)
		,field("Timeout","transactionTimeout","60",true,"This determines the number of seconds that a thread in the consumer pool waits for CFML code to perform a commit or rollback operation. If a transaction times out under this mechanism, the gateway automatically performs a commit or rollback operation","text")
		,field("Action on timeout","actionOnTimeout","commit",true,"This defines the action that the gateway automatically performs. Acceptable values are rollback and commit.","select","rollback,commit")
		
		,group("Security","",2)
		,field("Username","username","",false,"This username is used when the connection is created (i.e., in the call to createTopicConnection() or createQueueConnection()","text")
		,field("Password","password","",false,"This password is used when the connection is created (i.e., in the call to createTopicConnection() or createQueueConnection()","text")
		
	)>
<!---
#outboundOnly=no
debug=yes
topic=no
# uncomment the next four lines to test transacted message consumption:
#transacted=yes
#poolSize=5
#transactionTimeout=5
#actionOnTimeout=commit
#cachable=no
#username=
#password=
# uncomment the next line to test message selector filtering:
#selector=MessageNumber > 4
#noLocal=no
# default contextProperties is empty - see below for note on ActiveMQ usage
#contextProperties


# ActiveMQ requires fake JNDI entries to lookup queue / topic names, e.g.,
#contextProperties=queue.localQueueAlias,topic.localTopicAlias
#queue.localQueueAlias=RemoteQueueName
#topic.localTopicAlias=RemoteTopicName
#destinationName=localQueueAlias
# sendGatewayMessage() could be asked to send messages to topic localTopicAlias or
# queue localQueueAlias and the JNDI lookup will resolve to RemoteTopicName or
# RemoteQueueName respectively.

# ActiveMQ also supports dynamicQueues and dynamicTopics:


#durable=no
#publisherName=uniqueSubscriber
#subscriberName=uniqueSubscriber
--->
	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.extension.gateway.jms.JMSGateway">
    </cffunction>
	<cffunction name="getCFCPath" returntype="string">
    	<cfreturn "">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="no">
    	<cfreturn "JMS">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Processing a JMS messages in Lucee">
    </cffunction>
	
	<cffunction name="getListenerCFCDescription" returntype="string" output="no">
    	<cfreturn "Path to a Listener Component with the following function: onIncomingMessage(string msgId (JMS Message id), string originatorID (destination name), string GatewayType (topic or queue), boolean transacted, any msg (text or struct, depending on the message), any jmsmsg (original JMS Message), jmssession (original JMS session))">
    </cffunction>
	
	<cffunction name="onBeforeUpdate" returntype="void" output="false">
		<cfargument name="cfcPath" required="true" type="string">
		<cfargument name="startupMode" required="true" type="string">
		<cfargument name="custom" required="true" type="struct">
        
	</cffunction>
    
    
	<cffunction name="getListenerCfcMode" returntype="string" output="no">
		<cfreturn "required">
	</cffunction>
</cfcomponent>

