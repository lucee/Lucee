<cfcomponent extends="Gateway">

	
    <cfset fields=array(
		field("Provider URL","providerURL","tcp://localhost:61616",true,"This is the URL of the JMS provider, that is, the server. You can override a number of configuration parameters using a query string, for example, providerURL=tcp://localhost:61616?jms.redeliveryPolicy.maximumRedeliveries=20","text")
		,field("Initial Context Factory","initialContextFactory","org.apache.activemq.jndi.ActiveMQInitialContextFactory",true,"This is the name of the class used to construct the initial JNDI context that is used to lookup the connection factory and destination.","hidden")
		,field("Connection Factory","connectionFactory","ConnectionFactory",true,"This is generally the name of the class used to construct the factory object that creates connections. For some providers, it is a c-name identifying the resource within some sort of directory, such as LDAP.","hidden")
		,field("Destination Name","destinationName","dynamicQueues/TEST.FOO",true,"required for message consumption. This is the full name of a message destination to which the gateway should subscribe. This is not needed for outbound messages because the destination name is provided for each message. If the destination name is a full LDAP-style c-name, the gateway also constructs a short destination name, which is the base c-name converted to lowercase; everything after the first comma in the full c-name is discarded.","text")
		
		,field("Debug","debug","yes",true,"If [yes], the gateway generates verbose logging. The default is to only log problems.","radio","yes,no")
		,field("Topic","topic","no",true,"If [yes], the gateway expects the destination name to refer to a topic. If [no], the gateway expects the destination name to refer to a queue.","radio","yes,no")
		,field("Outbound Only","outboundOnly","no",true,"If [yes], the gateway does not subscribe to a destination and the following options are ignored because they only apply to message consumption.","radio","yes,no")
		,field("Cachable","cachable","no",true,"If [yes], a single publisher connection is created and used for all outbound messages. This allows you to use vendor-specific settings that support long-lived connections, such as durable publishers. Those settings are provided via the contextProperties setting. If [no], a fresh publisher connection is created for each outbound message that is sent.","radio","yes,no")
		
		,group("Transacted Message Consumption","",2)
		,field("Transacted","transacted","yes",true,"If [yes], a pool of consumers is created that handshake with the CFML code to create JMS transactions on separate sessions within the gateway's connection. CFML code must call commit() or rollback() to complete a transaction. See Transacted Message Consumption, above.","radio","yes,no")
		,field("Pool Size","poolSize","5",true,"This determines the size of the consumer pool for transaction.","text")
		
		,group("Timeout","",3)
		,field("Timeout","transactionTimeout","5",true,"This determines the number of seconds that a thread in the consumer pool waits for CFML code to perform a commit or rollback operation. If a transaction times out under this mechanism, the gateway automatically performs a commit or rollback operation","text")
		,field("Action on timeout","actionOnTimeout","commit",true,"This defines the action that the gateway automatically performs. Acceptable values are rollback and commit.","select","rollback,commit")
		
		
		,group("Security","",2)
		,field("Username","username","",false,"This username is used when the connection is created (i.e., in the call to createTopicConnection() or createQueueConnection()","text")
		,field("Password","password","",false,"This password is used when the connection is created (i.e., in the call to createTopicConnection() or createQueueConnection()","text")
		
	)>
<!---

# uncomment the next line to test message selector filtering:
#selector=MessageNumber > 4
#noLocal=no
# default contextProperties is empty - see below for note on ActiveMQ usage
#contextProperties

# ActiveMQ requires fake JNDI entries to lookup queue / topic names, e.g.,
#contextProperties=queue.localQueueAlias,topic.localTopicAlias
#queue.localQueueAlias=RemoteQueueName
#topic.localTopicAlias=RemoteTopicName

# sendGatewayMessage() could be asked to send messages to topic localTopicAlias or
# queue localQueueAlias and the JNDI lookup will resolve to RemoteTopicName or
# RemoteQueueName respectively.

# ActiveMQ also supports dynamicQueues and dynamicTopics:

#durable=no
#publisherName=uniqueSubscriber
#subscriberName=uniqueSubscriber
--->
	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.extension.gateway.jms.ActiveMQGateway">
    </cffunction>
	<cffunction name="getCFCPath" returntype="string">
    	<cfreturn "">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="no">
    	<cfreturn "Apache ActiveMQ (JMS)">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Apache ActiveMQ is the most popular and powerful open source messaging and Integration Patterns provider.

Apache ActiveMQ is fast, supports many Cross Language Clients and Protocols, comes with easy to use Enterprise Integration Patterns and many advanced features while fully supporting JMS 1.1 and J2EE 1.4. Apache ActiveMQ is released under the Apache 2.0 License.">
    </cffunction>
	<cffunction name="onBeforeUpdate" returntype="void" output="false">
		<cfargument name="cfcPath" required="true" type="string">
		<cfargument name="startupMode" required="true" type="string">
		<cfargument name="custom" required="true" type="struct">
        
        <cfif not IsNumeric(custom.poolSize)>
        	<cfthrow message="Pool Size [#custom.poolSize#] is not a numeric value">
        </cfif>
	</cffunction>
    
	<cffunction name="getListenerCfcMode" returntype="string" output="no">
		<cfreturn "required">
	</cffunction>
</cfcomponent>

