/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.net.rpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.Component;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageServletException;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.rpc.AxisCaster;
import lucee.runtime.net.rpc.TypeMappingUtil;
import lucee.runtime.op.Caster;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.ConfigurationException;
import org.apache.axis.Constants;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.management.ServiceAdmin;
import org.apache.axis.security.servlet.ServletSecurityProvider;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.axis.transport.http.FilterPrintWriter;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.QSWSDLHandler;
import org.apache.axis.transport.http.ServletEndpointContextImpl;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * xdoclet tags are not active yet; keep web.xml in sync.
 * To change the location of the services, change url-pattern in web.xml and
 * set parameter axis.servicesPath in server-config.wsdd. For more information see
 * <a href="http://ws.apache.org/axis/java/reference.html">Axis Reference Guide</a>.
 */
public final class RPCServer{

	//protected static Log log =LogFactory.getLog(RPCServer.class.getName());
    //private static Log tlog =LogFactory.getLog(Constants.TIME_LOG_CATEGORY);
    //private static Log exceptionLog =LogFactory.getLog(Constants.EXCEPTION_LOG_CATEGORY);

    public static final String INIT_PROPERTY_TRANSPORT_NAME ="transport.name";
    public static final String INIT_PROPERTY_USE_SECURITY ="use-servlet-security";
    public static final String INIT_PROPERTY_ENABLE_LIST ="axis.enableListQuery";
    public static final String INIT_PROPERTY_JWS_CLASS_DIR ="axis.jws.servletClassDir";
    public static final String INIT_PROPERTY_DISABLE_SERVICES_LIST ="axis.disableServiceList";
    public static final String INIT_PROPERTY_SERVICES_PATH ="axis.servicesPath";

    private Handler transport;
    private ServletSecurityProvider securityProvider = null;
	private ServletContext context;
	private String webInfPath;
	private String homeDir;
	private AxisServer axisServer;
	
	private Logger log;
	private Logger exceptionLog;
	
	
	private static boolean isDevelopment=false;
	private static boolean isDebug = false;
	private static Map servers=new WeakHashMap();


    /**
     * Initialization method.
     * @throws AxisFault 
     */
    private RPCServer(Config config,ServletContext context) throws AxisFault {
        this.context=context;
        ConfigImpl ci=(ConfigImpl) config;
        
        
        this.log=ci.getLogger("application",true);
        this.exceptionLog=ci.getLogger("exception",true);
        
        
        
        
        
        initQueryStringHandlers();
        ServiceAdmin.setEngine(this.getEngine(), context.getServerInfo());
    	
        webInfPath = context.getRealPath("/WEB-INF");
        homeDir = ReqRspUtil.getRootPath(context);
        
    }
    



    private Log toLog(Logger logger) {
		// make sure we have the class from the same classloader ...
    	return new Log4JLogger(logger);
	}




	/**
     * Process GET requests. This includes handoff of pseudo-SOAP requests
     *
     * @param request request in
     * @param response request out
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response, Component component) throws ServletException {
        PrintWriter writer = new FilterPrintWriter(response);
        
        try {
			if (!doGet(request, response, writer,component)) {
				ReqRspUtil.setContentType(response,"text/html; charset=utf-8");
				writer.println("<html><h1>"+lucee.runtime.config.Constants.NAME+" Webservice</h1>");
                writer.println(Messages.getMessage("reachedServlet00"));
                writer.println("<p>" + Messages.getMessage("transportName00","<b>http</b>"));
                writer.println("</html>");
			}
		} 
        catch (Throwable e) {
        	if(e instanceof InvocationTargetException)
        		e= ((InvocationTargetException)e).getTargetException();
        	if(e instanceof PageException)
            	throw new PageServletException((PageException)e);
        	throw new ServletException(e);
		}
    }

    /**
     * routine called whenever an axis fault is caught; where they
     * are logged and any other business. The method may modify the fault
     * in the process
     * @param fault what went wrong.
     */
    private void processAxisFault(AxisFault fault) {
        //log the fault
        Element runtimeException = fault.lookupFaultDetail(
                Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        if (runtimeException != null) {
        	
            exceptionLog.info(Messages.getMessage("axisFault00"), fault);
            
            //strip runtime details
            fault.removeFaultDetail(Constants.
                                    QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        } else if(exceptionLog.isDebugEnabled()){
            exceptionLog.debug(Messages.getMessage("axisFault00"), fault);
        }
        //dev systems only give fault dumps
        //if (!isDevelopment()) {
            //strip out the stack trace
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_STACKTRACE);
        //}
    }

    /**
     * log any exception to our output log, at our chosen level
     * @param e what went wrong
     */
    private void logException(Throwable e) {
    	exceptionLog.info(Messages.getMessage("exception00"), e);
    }

    /**
     * Process a POST to the servlet by handing it off to the Axis Engine.
     * Here is where SOAP messages are received
     * @param req posted request
     * @param res respose
     * @throws ServletException trouble
     * @throws IOException different trouble
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res, Component component) throws
            ServletException, IOException {
        long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        String soapAction = null;
        MessageContext msgContext = null;
        
        Message responseMsg = null;
        String contentType = null;
        InputStream is=null;
        try {
            AxisEngine engine = getEngine();

            if (engine == null) {
                // !!! should return a SOAP fault...
                ServletException se =
                        new ServletException(Messages.getMessage("noEngine00"));
                log.debug("No Engine!", se);
                throw se;
            }

            res.setBufferSize(1024 * 8); // provide performance boost.

            /** get message context w/ various properties set
             */
            msgContext = createMessageContext(engine, req, res,component);
            ComponentController.set(msgContext);
        	
            // ? OK to move this to 'getMessageContext',
            // ? where it would also be picked up for 'doGet()' ?
            if (securityProvider != null) {
                if (isDebug) {
                    log.debug("securityProvider:" + securityProvider);
                }
                msgContext.setProperty(MessageContext.SECURITY_PROVIDER,
                                       securityProvider);
            }

            is=req.getInputStream();
            Message requestMsg =
                    new Message(is,
                                false,
                                req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE),
                                req.getHeader(HTTPConstants.
                                              HEADER_CONTENT_LOCATION));
            // Transfer HTTP headers to MIME headers for request message.
            MimeHeaders requestMimeHeaders = requestMsg.getMimeHeaders();
            for (Enumeration e = req.getHeaderNames(); e.hasMoreElements(); ) {
                String headerName = (String) e.nextElement();
                for (Enumeration f = req.getHeaders(headerName);
                                     f.hasMoreElements(); ) {
                    String headerValue = (String) f.nextElement();
                    requestMimeHeaders.addHeader(headerName, headerValue);
                }
            }

            if (isDebug) {
                log.debug("Request Message:" + requestMsg);

                /* Set the request(incoming) message field in the context */
                /**********************************************************/
            }
            msgContext.setRequestMessage(requestMsg);
            String url = HttpUtils.getRequestURL(req).toString().toLowerCase();
            msgContext.setProperty(MessageContext.TRANS_URL, url);
            // put character encoding of request to message context
            // in order to reuse it during the whole process.
            String requestEncoding;
            try {
                requestEncoding = (String) requestMsg.getProperty(SOAPMessage.
                        CHARACTER_SET_ENCODING);
                if (requestEncoding != null) {
                    msgContext.setProperty(SOAPMessage.CHARACTER_SET_ENCODING,
                                           requestEncoding);
                }
            } catch (SOAPException e1) {
            	
            }

            try {
                /**
                 * Save the SOAPAction header in the MessageContext bag.
                 * This will be used to tell the Axis Engine which service
                 * is being invoked.  This will save us the trouble of
                 * having to parse the Request message - although we will
                 * need to double-check later on that the SOAPAction header
                 * does in fact match the URI in the body.
                 */
                // (is this last stmt true??? (I don't think so - Glen))
                /********************************************************/
                soapAction = getSoapAction(req);
                if (soapAction != null) {
                    msgContext.setUseSOAPAction(true);
                    msgContext.setSOAPActionURI(soapAction);
                }

                // Create a Session wrapper for the HTTP session.
                // These can/should be pooled at some point.
                // (Sam is Watching! :-)
                msgContext.setSession(new AxisHttpSession(req));

                if (log.isDebugEnabled()) {
                    t1 = System.currentTimeMillis();
                }
                /* Invoke the Axis engine... */
                /*****************************/
                if (isDebug) {
                    log.debug("Invoking Axis Engine.");
                    //here we run the message by the engine
                }
                
                engine.invoke(msgContext);
                if (isDebug) {
                    log.debug("Return from Axis Engine.");
                }
                if(log.isDebugEnabled())
                	t2 = System.currentTimeMillis();
                
                responseMsg = msgContext.getResponseMessage();

                // We used to throw exceptions on null response messages.
                // They are actually OK in certain situations (asynchronous
                // services), so fall through here and return an ACCEPTED
                // status code below.  Might want to install a configurable
                // error check for this later.
            } catch (AxisFault fault) {
            	
                //log and sanitize
                processAxisFault(fault);
                configureResponseFromAxisFault(res, fault);
                responseMsg = msgContext.getResponseMessage();
                if (responseMsg == null) {
                    responseMsg = new Message(fault);
                    ((org.apache.axis.SOAPPart) responseMsg.getSOAPPart()).
                            getMessage().setMessageContext(msgContext);
                }
            } catch (Throwable t) {
            	if(t instanceof InvocationTargetException)
            		t=((InvocationTargetException)t).getTargetException();
            	// Exception
            	if(t instanceof Exception) {
            		Exception e=(Exception) t;
	            	//other exceptions are internal trouble
	                responseMsg = msgContext.getResponseMessage();
	                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	                responseMsg = convertExceptionToAxisFault(e, responseMsg);
	                ((org.apache.axis.SOAPPart) responseMsg.getSOAPPart()).
	                        getMessage().setMessageContext(msgContext);
	                
            	}
            	// throwable
            	else {
                	logException(t);
                    //other exceptions are internal trouble
                    responseMsg = msgContext.getResponseMessage();
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responseMsg = new Message(new AxisFault(t.toString(),t));
                    ((org.apache.axis.SOAPPart) responseMsg.getSOAPPart()).
                            getMessage().setMessageContext(msgContext);
            	}
            } 
        } catch (AxisFault fault) {
        	
            processAxisFault(fault);
            configureResponseFromAxisFault(res, fault);
            responseMsg = msgContext.getResponseMessage();
            if (responseMsg == null) {
                responseMsg = new Message(fault);
                ((org.apache.axis.SOAPPart) responseMsg.getSOAPPart()).
                        getMessage().setMessageContext(msgContext);
            }
        }
        finally {
        	IOUtil.closeEL(is);
        }

        if (log.isDebugEnabled()) {
            t3 = System.currentTimeMillis();
        }

        /* Send response back along the wire...  */
        /***********************************/
        if (responseMsg != null) {
            // Transfer MIME headers to HTTP headers for response message.
            MimeHeaders responseMimeHeaders = responseMsg.getMimeHeaders();
            for (Iterator i = responseMimeHeaders.getAllHeaders(); i.hasNext(); ) {
                MimeHeader responseMimeHeader = (MimeHeader) i.next();
                res.addHeader(responseMimeHeader.getName(),
                              responseMimeHeader.getValue());
            }
            // synchronize the character encoding of request and response
            String responseEncoding = (String) msgContext.getProperty(
                    SOAPMessage.CHARACTER_SET_ENCODING);
            if (responseEncoding != null) {
                try {
                    responseMsg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING,
                                            responseEncoding);
                } catch (SOAPException e) {
                }
            }
            //determine content type from message response
            contentType = responseMsg.getContentType(msgContext.
                    getSOAPConstants());
            sendResponse(contentType, res, responseMsg);
        } else {
            // No content, so just indicate accepted
            res.setStatus(202);
        }
        
        if (isDebug) {
            log.debug("Response sent.");
            log.debug("Exit: doPost()");
        }
        if (log.isDebugEnabled()) {
            t4 = System.currentTimeMillis();
            log.debug("axisServlet.doPost: " + soapAction +
                       " pre=" + (t1 - t0) +
                       " invoke=" + (t2 - t1) +
                       " post=" + (t3 - t2) +
                       " send=" + (t4 - t3) +
                       " " + msgContext.getTargetService() + "." +
                       ((msgContext.getOperation() == null) ?
                        "" : msgContext.getOperation().getName()));
        }

    }

    /**
     * Configure the servlet response status code and maybe other headers
     * from the fault info.
     * @param response response to configure
     * @param fault what went wrong
     */
    private void configureResponseFromAxisFault(HttpServletResponse response,
                                                AxisFault fault) {
        // then get the status code
        // It's been suggested that a lack of SOAPAction
        // should produce some other error code (in the 400s)...
        int status = getHttpServletResponseStatus(fault);
        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"AXIS\"");
        }
        response.setStatus(status);
    }

    /**
     * turn any Exception into an AxisFault, log it, set the response
     * status code according to what the specifications say and
     * return a response message for posting. This will be the response
     * message passed in if non-null; one generated from the fault otherwise.
     *
     * @param exception what went wrong
     * @param responseMsg what response we have (if any)
     * @return a response message to send to the user
     */
    private Message convertExceptionToAxisFault(Exception exception,
                                                Message responseMsg) {
        logException(exception);
        if (responseMsg == null) {
            AxisFault fault = AxisFault.makeFault(exception);
            processAxisFault(fault);
            responseMsg = new Message(fault);
        }
        return responseMsg;
    }

    /**
     * Extract information from AxisFault and map it to a HTTP Status code.
     *
     * @param af Axis Fault
     * @return HTTP Status code.
     */
    private int getHttpServletResponseStatus(AxisFault af) {
        // subclasses... --Glen
        return af.getFaultCode().getLocalPart().startsWith("Server.Unauth")
                ? HttpServletResponse.SC_UNAUTHORIZED
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        // This will raise a 401 for both
        // "Unauthenticated" & "Unauthorized"...
    }

    /**
     * write a message to the response, set appropriate headers for content
     * type..etc.
     * @param res   response
     * @param responseMsg message to write
     * @throws AxisFault
     * @throws IOException if the response stream can not be written to
     */
    private void sendResponse(String contentType,
                              HttpServletResponse res,
                              Message responseMsg) throws AxisFault,
            IOException {
        if (responseMsg == null) {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            if (isDebug) {
                log.debug("NO AXIS MESSAGE TO RETURN!");
            }
        } else {
            if (isDebug) {
                log.debug("Returned Content-Type:" + contentType);
            }

            try {
            	ReqRspUtil.setContentType(res,contentType);
				
                responseMsg.writeTo(res.getOutputStream());
            } catch (SOAPException e) {
                logException(e);
            }
        }

        if (!res.isCommitted()) {
            res.flushBuffer(); // Force it right now.
        }
    }

    /**
     * Place the Request message in the MessagContext object - notice
     * that we just leave it as a 'ServletRequest' object and let the
     * Message processing routine convert it - we don't do it since we
     * don't know how it's going to be used - perhaps it might not
     * even need to be parsed.
     * @return a message context
     */
    private MessageContext createMessageContext(AxisEngine engine, HttpServletRequest req, HttpServletResponse res, Component component) {
        MessageContext msgContext = new MessageContext(engine);

        String requestPath = getRequestPath(req);

        if (isDebug) {
            log.debug("MessageContext:" + msgContext);
            log.debug("HEADER_CONTENT_TYPE:" +
                      req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE));
            log.debug("HEADER_CONTENT_LOCATION:" +
                      req.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION));
            log.debug("Constants.MC_HOME_DIR:" + String.valueOf(homeDir));
            log.debug("Constants.MC_RELATIVE_PATH:" + requestPath);
            log.debug("HTTPConstants.MC_HTTP_SERVLETLOCATION:" +String.valueOf(webInfPath));
            log.debug("HTTPConstants.MC_HTTP_SERVLETPATHINFO:" +req.getPathInfo());
            log.debug("HTTPConstants.HEADER_AUTHORIZATION:" +req.getHeader(HTTPConstants.HEADER_AUTHORIZATION));
            log.debug("Constants.MC_REMOTE_ADDR:" + req.getRemoteAddr());
            log.debug("configPath:" + String.valueOf(webInfPath));
        }

        /* Set the Transport */
        /*********************/
        msgContext.setTransportName("http");

        /* Save some HTTP specific info in the bag in case someone needs it */
        /********************************************************************/
        //msgContext.setProperty(Constants.MC_JWS_CLASSDIR, jwsClassDir);
        msgContext.setProperty(Constants.MC_HOME_DIR, homeDir);
        msgContext.setProperty(Constants.MC_RELATIVE_PATH, requestPath);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLET, this);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, req);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, res);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETLOCATION,webInfPath);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO,req.getPathInfo());
        msgContext.setProperty(HTTPConstants.HEADER_AUTHORIZATION,req.getHeader(HTTPConstants.HEADER_AUTHORIZATION));
        msgContext.setProperty(lucee.runtime.net.rpc.server.Constants.COMPONENT, component);
        msgContext.setProperty(Constants.MC_REMOTE_ADDR, req.getRemoteAddr());
        
        // Set up a javax.xml.rpc.server.ServletEndpointContext
        ServletEndpointContextImpl sec = new ServletEndpointContextImpl();

        msgContext.setProperty(Constants.MC_SERVLET_ENDPOINT_CONTEXT, sec);
        /* Save the real path */
        /**********************/
        String realpath = context.getRealPath(requestPath);

        if (realpath != null) {
            msgContext.setProperty(Constants.MC_REALPATH, realpath);
        }

        msgContext.setProperty(Constants.MC_CONFIGPATH, webInfPath);

        return msgContext;
    }

    /**
     * Extract the SOAPAction header.
     * if SOAPAction is null then we'll we be forced to scan the body for it.
     * if SOAPAction is "" then use the URL
     * @param req incoming request
     * @return the action
     * @throws AxisFault
     */
    private String getSoapAction(HttpServletRequest req) throws AxisFault {
        String soapAction = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (soapAction == null) {
            String contentType = req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            if(contentType != null) {
                int index = contentType.indexOf("action");
                if(index != -1){
                    soapAction = contentType.substring(index + 7);
                }
            }
        }

        if (isDebug) {
            log.debug("HEADER_SOAP_ACTION:" + soapAction);

            /**
             * Technically, if we don't find this header, we should probably fault.
             * It's required in the SOAP HTTP binding.
             */
        }
        if (soapAction == null) {
            AxisFault af = new AxisFault("Client.NoSOAPAction",
                                         Messages.getMessage("noHeader00",
                    "SOAPAction"),
                                         null, null);

            exceptionLog.error(Messages.getMessage("genFault00"), af);

            throw af;
        }
        // the SOAP 1.1 spec & WS-I 1.0 says:
        // soapaction    = "SOAPAction" ":" [ <"> URI-reference <"> ]
        // some implementations leave off the quotes
        // we strip them if they are present
        if (soapAction.startsWith("\"") && soapAction.endsWith("\"")
            && soapAction.length() >= 2) {
            int end = soapAction.length() - 1;
            soapAction = soapAction.substring(1, end);
        }

        if (soapAction.length() == 0) {
            soapAction = req.getContextPath(); // Is this right?

        }
        return soapAction;
    }


    /**
     * Initialize a Handler for the transport defined in the Axis server config.
     * This includes optionally filling in query string handlers.
     */

    public void initQueryStringHandlers() {
            this.transport = new SimpleTargetedChain();
            this.transport.setOption("qs.list","org.apache.axis.transport.http.QSListHandler");
            this.transport.setOption("qs.method","org.apache.axis.transport.http.QSMethodHandler");
            this.transport.setOption("qs.wsdl","org.apache.axis.transport.http.QSWSDLHandler");
            
    }
    
    private boolean doGet(HttpServletRequest request,HttpServletResponse response,PrintWriter writer,Component component) throws AxisFault, ClassException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException  {

        String path = request.getServletPath();
        String queryString = request.getQueryString();
        
        AxisEngine engine = getEngine();
        
        Iterator i = this.transport.getOptions().keySet().iterator();

        if (queryString == null) {
            return false;
        }

        String servletURI = request.getContextPath() + path;
        String reqURI = request.getRequestURI();
        
        // service name
        String serviceName;
        if (servletURI.length() + 1 < reqURI.length()) {
            serviceName = reqURI.substring(servletURI.length() + 1);
        } else {
            serviceName = "";
        } 
        
        while (i.hasNext()) {
            String queryHandler = (String) i.next();
            if (queryHandler.startsWith("qs.")) {
                // Only attempt to match the query string with transport
                // parameters prefixed with "qs:".

                String handlerName = queryHandler.substring
                                     (queryHandler.indexOf(".") + 1).
                                     toLowerCase();
                // Determine the name of the plugin to invoke by using all text
                // in the query string up to the first occurence of &, =, or the
                // whole string if neither is present.

                int length = 0;
                boolean firstParamFound = false;

                while (firstParamFound == false && length < queryString.length()) {
                    char ch = queryString.charAt(length++);

                    if (ch == '&' || ch == '=') {
                        firstParamFound = true;

                        --length;
                    }
                }

                if (length < queryString.length()) {
                    queryString = queryString.substring(0, length);
                }

                if (queryString.toLowerCase().equals(handlerName) == true) {
                    // Query string matches a defined query string handler name.

                    // If the defined class name for this query string handler is blank,
                    // just return (the handler is "turned off" in effect).

                    if (this.transport.getOption(queryHandler).equals("")) {
                        return false;
                    }

                        // Attempt to dynamically load the query string handler
                        // and its "invoke" method.

                        MessageContext msgContext = createMessageContext(engine,request, response,component);
                        msgContext.setProperty(MessageContext.TRANS_URL, HttpUtils.getRequestURL(request).toString().toLowerCase());
                        //msgContext.setProperty(MessageContext.TRANS_URL, "http://DefaultNamespace");
                        msgContext.setProperty(HTTPConstants.PLUGIN_SERVICE_NAME, serviceName);
                        msgContext.setProperty(HTTPConstants.PLUGIN_NAME,handlerName);
                        msgContext.setProperty(HTTPConstants.PLUGIN_IS_DEVELOPMENT,Caster.toBoolean(isDevelopment));
                        msgContext.setProperty(HTTPConstants.PLUGIN_ENABLE_LIST,Boolean.FALSE);
                        msgContext.setProperty(HTTPConstants.PLUGIN_ENGINE,engine);
                        msgContext.setProperty(HTTPConstants.PLUGIN_WRITER,writer);
                        msgContext.setProperty(HTTPConstants.PLUGIN_LOG, toLog(log));
                        msgContext.setProperty(HTTPConstants.PLUGIN_EXCEPTION_LOG,toLog(exceptionLog));
                        
                        
                        String handlerClassName = (String) this.transport.getOption(queryHandler);
                        if("org.apache.axis.transport.http.QSWSDLHandler".equals(handlerClassName)){
                        	print.e("direct:"+handlerClassName);
                        	QSWSDLHandler handler=new QSWSDLHandler();
                        	handler.invoke(msgContext);
                        }
                        else {
                        	print.e("reflection:"+handlerClassName);
                        	// Invoke the plugin.
                        	Class plugin=ClassUtil.loadClass((String)this.transport.getOption(queryHandler));
                        	Method pluginMethod = plugin.getDeclaredMethod("invoke", new Class[] {msgContext.getClass()});
                        	pluginMethod.invoke(ClassUtil.loadInstance(plugin),new Object[] {msgContext});
                        }
                        
                        writer.close();

                        return true;
                    
                }
            }
        }

        return false;
    }
    

    /**
     * getRequestPath a returns request path for web service padded with
     * request.getPathInfo for web services served from /services directory.
     * This is a required to support serving .jws web services from /services
     * URL. See AXIS-843 for more information.
     *
     * @param request HttpServletRequest
     * @return String
     */
    private static String getRequestPath(HttpServletRequest request) {
        return request.getServletPath() + ((request.getPathInfo() != null) ?
                                           request.getPathInfo() : "");
    }
    

    /**
     * get the engine for this Server from cache or context
     * @return
     * @throws AxisFault 
     */
    public AxisServer getEngine() throws AxisFault {
        if (axisServer == null) {
            synchronized (context) {
            	Map environment = new HashMap();
                environment.put(AxisEngine.ENV_SERVLET_CONTEXT, context);
                axisServer = AxisServer.getServer(environment);
                axisServer.setName("LuceeServer");
            }
            
            // add Component Handler
            try {
				SimpleChain sc=(SimpleChain) axisServer.getGlobalRequest();
				sc.addHandler(new ComponentHandler());	
			}
            catch (ConfigurationException e) {
				throw AxisFault.makeFault(e);
			}
            TypeMappingUtil.registerDefaults(axisServer.getTypeMappingRegistry());
            
        }
        return axisServer;
    }
    
	public static RPCServer getInstance(int id, Config config,ServletContext servletContext) throws AxisFault {
		RPCServer server=(RPCServer) servers.get(Caster.toString(id));
		if(server==null){
			servers.put(Caster.toString(id), server=new RPCServer(config,servletContext));
		}
		return server;
	}




	public void registerTypeMapping(Class clazz) {
		String fullname = clazz.getName();//,name,packages;
		//registerTypeMapping(clazz, new QName(AxisCaster.getRequestNameSpace(),fullname));
		registerTypeMapping(clazz, new QName(AxisCaster.getRequestDefaultNameSpace(),fullname));
	}
	
	private void registerTypeMapping(Class clazz,QName qname) {
		
		org.apache.axis.encoding.TypeMapping tm = TypeMappingUtil.getServerTypeMapping(axisServer);
		TypeMappingUtil.registerBeanTypeMapping(tm,clazz, qname);
	}
}