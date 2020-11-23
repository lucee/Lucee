/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.HTMLEntities;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.component.StaticStruct;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.converter.BinaryConverter;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JavaConverter;
import lucee.runtime.converter.ScriptConverter;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.converter.XMLConverter;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;
import lucee.runtime.op.Decision;
import lucee.runtime.rest.RestUtil;
import lucee.runtime.rest.Result;
import lucee.runtime.rest.path.Path;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StructUtil;
import lucee.runtime.type.util.UDFUtil;

/**
 * A Page that can produce Components
 */
public abstract class ComponentPageImpl extends ComponentPage implements PagePro {

	public static final Collection.Key ACCEPT_ARG_COLL_FORMATS = KeyImpl.getInstance("acceptedArgumentCollectionFormats");

	private static final long serialVersionUID = -3483642653131058030L;

	public static final lucee.runtime.type.Collection.Key REMOTE_PERSISTENT_ID = KeyImpl.getInstance("Id16hohohh");

	private long lastCheck = -1;

	public abstract ComponentImpl newInstance(PageContext pc, String callPath, boolean isRealPath, boolean isExtendedComponent, boolean executeConstr)
			throws lucee.runtime.exp.PageException;

	@Override
	public int getHash() {
		return 0;
	}

	@Override
	public long getSourceLength() {
		return 0;
	}

	@Override
	public Object call(PageContext pc) throws PageException {
		// remote persistent (only type server is supported)
		String strRemotePersisId = Caster.toString(getURLorForm(pc, REMOTE_PERSISTENT_ID, null), null);// Caster.toString(pc.urlFormScope().get(REMOTE_PERSISTENT_ID,null),null);

		if (!StringUtil.isEmpty(strRemotePersisId, true)) {
			strRemotePersisId = strRemotePersisId.trim();
		}
		else strRemotePersisId = null;

		HttpServletRequest req = pc.getHttpServletRequest();
		// client
		String client = Caster.toString(req.getAttribute("client"), null);
		// call type (invocation, store-only)
		String callType = Caster.toString(req.getAttribute("call-type"), null);
		boolean internalCall = "lucee-gateway-1-0".equals(client) || "lucee-listener-1-0".equals(client);
		boolean fromRest = "lucee-rest-1-0".equals(client);
		Component component;
		try {
			pc.setSilent();
			// load the cfc
			try {
				if (internalCall && strRemotePersisId != null) {
					ConfigWebPro config = (ConfigWebPro) pc.getConfig();
					GatewayEngineImpl engine = (GatewayEngineImpl) config.getGatewayEngine();
					component = engine.getPersistentRemoteCFC(strRemotePersisId);

					if (component == null) {
						component = newInstance(pc, getComponentName(), false, false, true);
						if (!internalCall) component = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_REMOTE, component);

						engine.setPersistentRemoteCFC(strRemotePersisId, component);
					}

				}
				else {
					component = newInstance(pc, getComponentName(), false, false, true);
					if (!internalCall) component = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_REMOTE, component);
				}
			}
			finally {
				pc.unsetSilent();
			}

			// Only get the Component, no invocation
			if ("store-only".equals(callType)) {
				req.setAttribute("component", component);
				return null;
			}

			// METHOD INVOCATION
			String qs = ReqRspUtil.getQueryString(pc.getHttpServletRequest());

			if (pc.getBasePageSource() == this.getPageSource() && pc.getConfig().debug())
				pc.getDebugger().setOutput(false);

			boolean isPost = pc.getHttpServletRequest().getMethod().equalsIgnoreCase("POST");

			boolean suppressContent = pc.getRequestDialect() == CFMLEngine.DIALECT_LUCEE || ((PageContextImpl) pc).getSuppressContent();
			if (suppressContent)
				pc.clear();

			if (fromRest) {
				callRest(pc, component, Caster.toString(req.getAttribute("rest-path"), ""), (Result) req.getAttribute("rest-result"), suppressContent);
				return null;
			}

			Object method;

			// POST
			if (isPost) {
				// Soap
				if (isSoap(pc)) {
					callWebservice(pc, component);
					// close(pc);
					return null;
				}
				// WDDX
				else if ((method = getURLorForm(pc, KeyConstants._method, null)) != null) {
					callWDDX(pc, component, KeyImpl.toKey(method), suppressContent);
					// close(pc);
					return null;
				}
			}

			// GET
			else {
				// WSDL
				if (qs != null && (qs.trim().equalsIgnoreCase("wsdl") || qs.trim().startsWith("wsdl&"))) {
					callWSDL(pc, component);
					// close(pc);
					return null;
				}
				// WDDX
				else if ((method = getURLorForm(pc, KeyConstants._method, null)) != null) {
					callWDDX(pc, component, KeyImpl.toKey(method), suppressContent);
					// close(pc);
					return null;
				}

				if (qs != null) {
					int rf = UDFUtil.toReturnFormat(qs.trim(), -1);
					if (rf != -1) callCFCMetaData(pc, component, rf);
					// close(pc);
					return null;
				}
			}

			// Include MUST
			Array path = pc.getTemplatePath();
			// if(path.size()>1 ) {
			if (path.size() > 1 && !(path.size() == 3 && ListUtil.last(path.getE(2).toString(), "/\\", true)
					.equalsIgnoreCase(pc.getRequestDialect() == CFMLEngine.DIALECT_CFML ? lucee.runtime.config.Constants.CFML_APPLICATION_EVENT_HANDLER
							: lucee.runtime.config.Constants.LUCEE_APPLICATION_EVENT_HANDLER))) {// MUSTMUST
				// bad
				// impl
				// ->
				// check
				// with
				// and
				// without
				// application
				// .
				// cfc

				ComponentSpecificAccess c = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component);
				Key[] keys = c.keys();
				Object el;
				Scope var = pc.variablesScope();
				for (int i = 0; i < keys.length; i++) {
					el = c.get(keys[i], null);
					if (el instanceof UDF) var.set(keys[i], el);

				}

				return null;
			}

			// DUMP
			// TODO component.setAccess(pc,Component.ACCESS_PUBLIC);
			String cdf = pc.getConfig().getComponentDumpTemplate();

			if (cdf != null && cdf.trim().length() > 0) {
				pc.variablesScope().set(KeyConstants._component, component);
				pc.doInclude(cdf, false);
			}
			else pc.write(pc.getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH).toString(pc, component.toDumpData(pc, 9999, DumpUtil.toDumpProperties()), true));

		}
		catch (Throwable t) {
			throw Caster.toPageException(t);// Exception Handler.castAnd
			// Stack(t, this, pc);
		}
		return null;
	}

	private Object getURLorForm(PageContext pc, Key key, Object defaultValue) {
		Object res = pc.formScope().get(key, null);
		if (res != null) return res;
		return pc.urlScope().get(key, defaultValue);
	}

	private void callRest(PageContext pc, Component component, String path, Result result, boolean suppressContent) throws IOException, ConverterException {
		String method = pc.getHttpServletRequest().getMethod();
		String[] subPath = result.getPath();
		Struct cMeta;
		try {
			cMeta = component.getMetaData(pc);
		}
		catch (PageException pe) {
			throw ExceptionUtil.toIOException(pe);
		}

		// Consumes
		MimeType[] cConsumes = null;
		String strMimeType = Caster.toString(cMeta.get(KeyConstants._consumes, null), null);
		if (!StringUtil.isEmpty(strMimeType, true)) {
			cConsumes = MimeType.getInstances(strMimeType, ',');
		}

		// Produces
		MimeType[] cProduces = null;
		strMimeType = Caster.toString(cMeta.get(KeyConstants._produces, null), null);
		if (!StringUtil.isEmpty(strMimeType, true)) {
			cProduces = MimeType.getInstances(strMimeType, ',');
		}

		Iterator<Entry<Key, Object>> it = component.entryIterator();
		Entry<Key, Object> e;
		Object value;
		UDF udf;
		Struct meta;
		int status = 404;
		MimeType bestP, bestC;
		while (it.hasNext()) {
			e = it.next();
			value = e.getValue();
			if (value instanceof UDF) {
				udf = (UDF) value;
				try {
					meta = udf.getMetaData(pc);

					// check if http method match
					String httpMethod = Caster.toString(meta.get(KeyConstants._httpmethod, null), null);
					if (StringUtil.isEmpty(httpMethod) || !httpMethod.equalsIgnoreCase(method)) continue;

					// get consumes mimetype
					MimeType[] consumes;
					strMimeType = Caster.toString(meta.get(KeyConstants._consumes, null), null);
					if (!StringUtil.isEmpty(strMimeType, true)) {
						consumes = MimeType.getInstances(strMimeType, ',');
					}
					else consumes = cConsumes;

					// get produces mimetype
					MimeType[] produces;
					strMimeType = Caster.toString(meta.get(KeyConstants._produces, null), null);
					if (!StringUtil.isEmpty(strMimeType, true)) {
						produces = MimeType.getInstances(strMimeType, ',');
					}
					else produces = cProduces;

					String restPath = Caster.toString(meta.get(KeyConstants._restPath, null), null);

					// no rest path
					if (StringUtil.isEmpty(restPath)) {
						if (ArrayUtil.isEmpty(subPath)) {
							bestC = best(consumes, result.getContentType());
							bestP = best(produces, result.getAccept());
							if (bestC == null) status = 405;
							else if (bestP == null) status = 406;
							else {
								status = 200;
								_callRest(pc, component, udf, path, result.getVariables(), result, bestP, produces, suppressContent, e.getKey());
								break;
							}
						}
					}
					else {
						Struct var = result.getVariables();
						int index = RestUtil.matchPath(var, Path.init(restPath)/* TODO cache this */, result.getPath());
						if (index >= 0 && index + 1 == result.getPath().length) {
							bestC = best(consumes, result.getContentType());
							bestP = best(produces, result.getAccept());

							if (bestC == null) status = 405;
							else if (bestP == null) status = 406;
							else {
								status = 200;
								_callRest(pc, component, udf, path, var, result, bestP, produces, suppressContent, e.getKey());
								break;
							}
						}
					}
				}
				catch (PageException pe) {
					pc.getConfig().getLog("rest").error("REST", pe);
				}
			}
		}

		if (status == 404) {
			RestUtil.setStatus(pc, 404, "no rest service for [" + HTMLEntities.escapeHTML(path) + "] found");
			pc.getConfig().getLog("rest").error("REST", "404; no rest service for [" + path + "] found");
		}
		else if (status == 405) {
			RestUtil.setStatus(pc, 405, "Unsupported Media Type");
			pc.getConfig().getLog("rest").error("REST", "405; Unsupported Media Type");
		}
		else if (status == 406) {
			RestUtil.setStatus(pc, 406, "Not Acceptable");
			pc.getConfig().getLog("rest").error("REST", "406; Not Acceptable");
		}

	}

	private MimeType best(MimeType[] produces, MimeType... accept) {
		if (ArrayUtil.isEmpty(produces)) {
			if (accept.length > 0) return accept[0];
			return MimeType.ALL;
		}

		MimeType best = null, tmp;

		for (int a = 0; a < accept.length; a++) {
			tmp = accept[a].bestMatch(produces);
			if (tmp != null && !accept[a].hasWildCards() && tmp.hasWildCards()) {
				tmp = accept[a];
			}
			if (tmp != null && (best == null || best.getQuality() < tmp.getQuality() || (best.getQuality() == tmp.getQuality() && best.hasWildCards() && !tmp.hasWildCards())))
				best = tmp;
		}

		return best;
	}

	private void _callRest(PageContext pc, Component component, UDF udf, String path, Struct variables, Result result, MimeType best, MimeType[] produces, boolean suppressContent,
			Key methodName) throws PageException, IOException, ConverterException {
		FunctionArgument[] fa = udf.getFunctionArguments();
		Struct args = new StructImpl(), meta;

		Key name;
		String restArgName, restArgSource, value;
		for (int i = 0; i < fa.length; i++) {
			name = fa[i].getName();
			meta = fa[i].getMetaData();
			restArgSource = meta == null ? "" : Caster.toString(meta.get(KeyConstants._restArgSource, ""), "");

			if ("path".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, variables.get(name, null));
			if ("query".equalsIgnoreCase(restArgSource) || "url".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, pc.urlScope().get(name, null));
			if ("form".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, pc.formScope().get(name, null));
			if ("cookie".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, pc.cookieScope().get(name, null));
			if ("header".equalsIgnoreCase(restArgSource) || "head".equalsIgnoreCase(restArgSource)) {
				restArgName = meta == null ? "" : Caster.toString(meta.get(KeyConstants._restArgName, ""), "");
				if (StringUtil.isEmpty(restArgName)) restArgName = name.getString();
				value = ReqRspUtil.getHeaderIgnoreCase(pc, restArgName, null);
				setValue(fa[i], args, name, value);
			}
			if ("matrix".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, result.getMatrix().get(name, null));

			if ("body".equalsIgnoreCase(restArgSource) || StringUtil.isEmpty(restArgSource, true)) {
				boolean isSimple = CFTypes.isSimpleType(fa[i].getType());
				Object body = ReqRspUtil.getRequestBody(pc, true, null);
				if (isSimple && !Decision.isSimpleValue(body)) body = ReqRspUtil.getRequestBody(pc, false, null);
				setValue(fa[i], args, name, body);
			}
		}
		Object rtn = null;
		try {
			if (suppressContent)
				pc.setSilent();
			rtn = component.callWithNamedValues(pc, methodName, args);
		}
		catch (PageException e) {
			RestUtil.setStatus(pc, 500, ExceptionUtil.getMessage(e));
			pc.getConfig().getLog("rest").error("REST", e);
		}
		finally {
			if (suppressContent)
				pc.unsetSilent();
		}

		// custom response
		Struct sct = result.getCustomResponse();
		boolean hasContent = false;
		if (sct != null) {
			HttpServletResponse rsp = pc.getHttpServletResponse();
			// status
			int status = Caster.toIntValue(sct.get(KeyConstants._status, Constants.DOUBLE_ZERO), 0);
			if (status > 0) rsp.setStatus(status);

			// content
			Object o = sct.get(KeyConstants._content, null);
			if (o != null) {
				String content = Caster.toString(o, null);
				if (content != null) {
					try {
						pc.forceWrite(content);
						hasContent = true;
					}
					catch (IOException e) {}
				}
			}

			// headers
			Struct headers = Caster.toStruct(sct.get(KeyConstants._headers, null), null);
			if (headers != null) {
				// Key[] keys = headers.keys();
				Iterator<Entry<Key, Object>> it = headers.entryIterator();
				Entry<Key, Object> e;
				String n, v;
				Object tmp;
				while (it.hasNext()) {
					e = it.next();
					n = e.getKey().getString();
					tmp = e.getValue();
					v = Caster.toString(tmp, null);
					if (tmp != null && v == null) v = tmp.toString();
					rsp.setHeader(n, v);
				}
			}
		}

		// convert result
		if (rtn != null && !hasContent) {
			Props props = new Props();
			props.format = result.getFormat();
			Charset cs = getCharset(pc);
			if (result.hasFormatExtension()) {
				// setFormat(pc.getHttpServletResponse(), props.format,cs);
				_writeOut(pc, props, null, rtn, cs, true);
			}
			else {
				if (best != null && !MimeType.ALL.same(best)) {
					int f = MimeType.toFormat(best, -1);
					if (f != -1) {
						props.format = f;
						// setFormat(pc.getHttpServletResponse(), f,cs);
						_writeOut(pc, props, null, rtn, cs, true);
					}
					else {
						writeOut(pc, props, rtn, best);
					}
				}
				else {
					_writeOut(pc, props, null, rtn, cs, true);
				}
			}

		}

	}

	private void setValue(FunctionArgument fa, Struct args, Key name, Object value) {
		if (value == null) {
			Struct meta = fa.getMetaData();
			if (meta != null) value = meta.get(KeyConstants._default, null);
		}
		args.setEL(name, value);
	}

	private void writeOut(PageContext pc, Props props, Object obj, MimeType mt) throws PageException, IOException, ConverterException {
		// TODO miemtype mapping with converter defintion from external file
		// Images
		/*
		 * if (mt.same(MimeType.IMAGE_GIF)) writeOut(pc, obj, mt, new ImageConverter("gif")); else if
		 * (mt.same(MimeType.IMAGE_JPG)) writeOut(pc, obj, mt, new ImageConverter("jpeg")); else if
		 * (mt.same(MimeType.IMAGE_PNG)) writeOut(pc, obj, mt, new ImageConverter("png")); else if
		 * (mt.same(MimeType.IMAGE_TIFF)) writeOut(pc, obj, mt, new ImageConverter("tiff")); else if
		 * (mt.same(MimeType.IMAGE_BMP)) writeOut(pc, obj, mt, new ImageConverter("bmp")); else if
		 * (mt.same(MimeType.IMAGE_WBMP)) writeOut(pc, obj, mt, new ImageConverter("wbmp")); else if
		 * (mt.same(MimeType.IMAGE_FBX)) writeOut(pc, obj, mt, new ImageConverter("fbx")); else if
		 * (mt.same(MimeType.IMAGE_FBX)) writeOut(pc, obj, mt, new ImageConverter("fbx")); else if
		 * (mt.same(MimeType.IMAGE_PNM)) writeOut(pc, obj, mt, new ImageConverter("pnm")); else if
		 * (mt.same(MimeType.IMAGE_PGM)) writeOut(pc, obj, mt, new ImageConverter("pgm")); else if
		 * (mt.same(MimeType.IMAGE_PBM)) writeOut(pc, obj, mt, new ImageConverter("pbm")); else if
		 * (mt.same(MimeType.IMAGE_ICO)) writeOut(pc, obj, mt, new ImageConverter("ico")); else if
		 * (mt.same(MimeType.IMAGE_PSD)) writeOut(pc, obj, mt, new ImageConverter("psd")); else if
		 * (mt.same(MimeType.IMAGE_ASTERIX)) writeOut(pc, obj, MimeType.IMAGE_PNG, new
		 * ImageConverter("png"));
		 */
		// Application
		if (mt.same(MimeType.APPLICATION_JAVA)) writeOut(pc, obj, mt, new JavaConverter());
		// if("application".equalsIgnoreCase(mt.getType()))

		else _writeOut(pc, props, null, obj, null, true);
	}

	private static void writeOut(PageContext pc, Object obj, MimeType mt, BinaryConverter converter) throws ConverterException, IOException {
		ReqRspUtil.setContentType(pc.getHttpServletResponse(), mt.toString());

		OutputStream os = null;
		try {
			converter.writeOut(pc, obj, os = pc.getResponseStream());
		}
		finally {
			IOUtil.close(os);
		}
	}

	public static boolean isSoap(PageContext pc) {
		HttpServletRequest req = pc.getHttpServletRequest();
		InputStream is = null;
		try {
			is = req.getInputStream();

			String input = IOUtil.toString(is, CharsetUtil.ISO88591);
			return StringUtil.indexOfIgnoreCase(input, ":Envelope>") != -1;
		}
		catch (IOException e) {
			return false;
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	private void callWDDX(PageContext pc, Component component, Collection.Key methodName, boolean suppressContent) throws PageException {
		try {
			// Struct url = StructUtil.duplicate(pc.urlFormScope(),true);
			Struct url = StructUtil.merge(new Struct[] { pc.formScope(), pc.urlScope() });
			// define args
			url.removeEL(KeyConstants._fieldnames);
			url.removeEL(KeyConstants._method);
			Object args = url.get(KeyConstants._argumentCollection, null);
			String strArgCollFormat = Caster.toString(url.get("argumentCollectionFormat", null), null);

			// url.returnFormat
			int urlReturnFormat = -1;
			Object oReturnFormatFromURL = url.get(KeyConstants._returnFormat, null);
			if (oReturnFormatFromURL != null) urlReturnFormat = UDFUtil.toReturnFormat(Caster.toString(oReturnFormatFromURL, null), -1);

			// request header "accept"
			List<MimeType> accept = ReqRspUtil.getAccept(pc);
			int headerReturnFormat = MimeType.toFormat(accept, UDF.RETURN_FORMAT_XML, -1);

			Object queryFormat = url.get(KeyConstants._queryFormat, null);

			if (args == null) {
				args = pc.getHttpServletRequest().getAttribute("argumentCollection");
			}
			if (StringUtil.isEmpty(strArgCollFormat)) {
				strArgCollFormat = Caster.toString(pc.getHttpServletRequest().getAttribute("argumentCollectionFormat"), null);
			}

			// content-type
			Charset cs = getCharset(pc);
			Object o = component.get(pc, methodName, null);

			// onMissingMethod
			if (o == null) o = component.get(pc, KeyConstants._onmissingmethod, null);

			Props props = getProps(pc, o, urlReturnFormat, headerReturnFormat);
			// if(!props.output)
			setFormat(pc.getHttpServletResponse(), props.format, cs);

			Object rtn = null;
			try {
				if (suppressContent) pc.setSilent();

				if (args == null) {
					url = translate(component, methodName.getString(), url);
					rtn = component.callWithNamedValues(pc, methodName, url);
				}
				else if (args instanceof String) {
					String str = (String) args;
					int format = UDFUtil.toReturnFormat(strArgCollFormat, -1);

					// CFML
					if (UDF.RETURN_FORMAT_SERIALIZE == format) {
						// do not catch exception when format is defined
						args = new CFMLExpressionInterpreter().interpret(pc, str);
					}
					// JSON
					if (UDF.RETURN_FORMAT_JSON == format) {
						// do not catch exception when format is defined
						args = new JSONExpressionInterpreter(false).interpret(pc, str);
					}
					// default
					else {
						// catch exception when format is not defined, then in
						// this case the string can also be a simple argument
						try {
							args = new JSONExpressionInterpreter(false).interpret(pc, str);
						}
						catch (PageException pe) {
							try {
								args = new CFMLExpressionInterpreter().interpret(pc, str);
							}
							catch (PageException _pe) {}
						}
					}
				}

				// call
				if (args != null) {
					if (Decision.isCastableToStruct(args)) {
						rtn = component.callWithNamedValues(pc, methodName, Caster.toStruct(args, false));
					}
					else if (Decision.isCastableToArray(args)) {
						rtn = component.call(pc, methodName, Caster.toNativeArray(args));
					}
					else {
						Object[] ac = new Object[1];
						ac[0] = args;
						rtn = component.call(pc, methodName, ac);
					}
				}
			}
			finally {
				if (suppressContent) pc.unsetSilent();
			}
			// convert result
			if (rtn != null) {
				if (pc.getHttpServletRequest().getHeader("AMF-Forward") != null) {
					pc.variablesScope().setEL("AMF-Forward", rtn);
				}
				else {
					_writeOut(pc, props, queryFormat, rtn, cs, false);
				}
			}
		}
		catch (Throwable t) {
			PageException pe = Caster.toPageException(t);
			if (pc.getConfig().debug()) pe.setExposeMessage(true);
			throw pe;
		}
	}

	private static void setFormat(HttpServletResponse rsp, int format, Charset charset) {
		String strCS;
		if (charset == null) strCS = "";
		else strCS = "; charset=" + charset.displayName();

		switch (format) {
		case UDF.RETURN_FORMAT_WDDX:
			ReqRspUtil.setContentType(rsp, "text/xml" + strCS);
			rsp.setHeader("Return-Format", "wddx");
			break;
		case UDF.RETURN_FORMAT_JSON:
			ReqRspUtil.setContentType(rsp, "application/json" + strCS);
			rsp.setHeader("Return-Format", "json");
			break;
		case UDF.RETURN_FORMAT_PLAIN:
			ReqRspUtil.setContentType(rsp, "text/plain" + strCS);
			rsp.setHeader("Return-Format", "plain");
			break;
		case UDF.RETURN_FORMAT_XML:
			ReqRspUtil.setContentType(rsp, "text/xml" + strCS);
			rsp.setHeader("Return-Format", "xml");
			break;
		case UDF.RETURN_FORMAT_SERIALIZE:
			ReqRspUtil.setContentType(rsp, "application/cfml" + strCS);
			rsp.setHeader("Return-Format", "cfml");
			break;
		case UDF.RETURN_FORMAT_JAVA:
			ReqRspUtil.setContentType(rsp, "application/java");
			rsp.setHeader("Return-Format", "java");
			break;
		}
	}

	private static Props getProps(PageContext pc, Object o, int urlReturnFormat, int headerReturnFormat) {
		Props props = new Props();

		props.strType = "any";
		props.secureJson = pc.getApplicationContext().getSecureJson();
		int udfReturnFormat = -1;
		if (o instanceof UDF) {
			UDF udf = ((UDF) o);
			udfReturnFormat = udf.getReturnFormat(-1);
			props.type = udf.getReturnType();
			props.strType = udf.getReturnTypeAsString();
			props.output = udf.getOutput();
			if (udf.getSecureJson() != null) props.secureJson = udf.getSecureJson().booleanValue();
		}

		// format
		if (isValid(urlReturnFormat)) props.format = urlReturnFormat;
		else if (isValid(udfReturnFormat)) props.format = udfReturnFormat;
		else if (isValid(headerReturnFormat)) props.format = headerReturnFormat;
		else props.format = UDF.RETURN_FORMAT_WDDX;

		// return type XML ignore WDDX
		if (props.type == CFTypes.TYPE_XML) {
			if (UDF.RETURN_FORMAT_WDDX == props.format) props.format = UDF.RETURN_FORMAT_PLAIN;
		}

		return props;
	}

	private static boolean isValid(int returnFormat) {
		return returnFormat != -1 && returnFormat != UDF.RETURN_FORMAT_XML;
	}

	public static void writeToResponseStream(PageContext pc, Component component, String methodName, int urlReturnFormat, int headerReturnFormat, Object queryFormat, Object rtn)
			throws ConverterException, PageException, IOException {
		Object o = component.get(KeyImpl.init(methodName), null);
		Props p = getProps(pc, o, urlReturnFormat, headerReturnFormat);
		_writeOut(pc, p, queryFormat, rtn, null, true);
	}

	private static void _writeOut(PageContext pc, Props props, Object queryFormat, Object rtn, Charset cs, boolean setFormat)
			throws ConverterException, PageException, IOException {
		// return type XML ignore WDDX
		if (props.type == CFTypes.TYPE_XML) {
			// if(UDF.RETURN_FORMAT_WDDX==format)
			// format=UDF.RETURN_FORMAT_PLAIN;
			rtn = Caster.toString(Caster.toXML(rtn));
		}
		// function does no real cast, only check it
		else rtn = Caster.castTo(pc, (short) props.type, props.strType, rtn);
		if (setFormat) setFormat(pc.getHttpServletResponse(), props.format, cs);

		// WDDX
		if (UDF.RETURN_FORMAT_WDDX == props.format) {
			WDDXConverter converter = new WDDXConverter(pc.getTimeZone(), false, false);
			converter.setTimeZone(pc.getTimeZone());
			pc.forceWrite(converter.serialize(rtn));
		}
		// JSON
		else if (UDF.RETURN_FORMAT_JSON == props.format) {
			int qf = SerializationSettings.SERIALIZE_AS_ROW;
			if (queryFormat != null) {
				qf = JSONConverter.toQueryFormat(queryFormat, SerializationSettings.SERIALIZE_AS_UNDEFINED);
				if (qf == SerializationSettings.SERIALIZE_AS_UNDEFINED)
					throw new ApplicationException("invalid queryformat definition [" + queryFormat + "], valid formats are [row,column,struct]");
			}
			JSONConverter converter = new JSONConverter(false, cs);
			String prefix = "";
			if (props.secureJson) {
				prefix = pc.getApplicationContext().getSecureJsonPrefix();
				if (prefix == null) prefix = "";
			}
			pc.forceWrite(prefix + converter.serialize(pc, rtn, qf));
		}
		// CFML
		else if (UDF.RETURN_FORMAT_SERIALIZE == props.format) {
			ScriptConverter converter = new ScriptConverter(false);
			pc.forceWrite(converter.serialize(rtn));
		}
		// XML
		else if (UDF.RETURN_FORMAT_XML == props.format) {
			XMLConverter converter = new XMLConverter(pc.getTimeZone(), false);
			converter.setTimeZone(pc.getTimeZone());
			pc.forceWrite(converter.serialize(rtn));
		}
		// Plain
		else if (UDF.RETURN_FORMAT_PLAIN == props.format) {
			pc.forceWrite(Caster.toString(rtn));
		}

		// JAVA
		else if (UDF.RETURN_FORMAT_JAVA == props.format) {
			writeOut(pc, rtn, MimeType.APPLICATION_JAVA, new JavaConverter());
		}
		else throw new IOException("invalid return format defintion:" + props.format);
	}

	public static Struct translate(Component c, String strMethodName, Struct params) {
		Collection.Key methodName = KeyImpl.init(strMethodName);
		Key[] keys = CollectionUtil.keys(params);
		FunctionArgument[] args = null;
		int index = -1;
		Object value;
		for (int i = 0; i < keys.length; i++) {
			index = Caster.toIntValue(keys[i].getString(), 0);
			if (index > 0) {
				if (args == null) args = _getArgs(c, methodName);
				if (args != null && index <= args.length) {
					value = params.removeEL(keys[i]);
					if (value != null) params.setEL(args[index - 1].getName(), value);
				}
			}

		}
		return params;
	}

	private static FunctionArgument[] _getArgs(Component c, Collection.Key methodName) {
		Object o = c.get(methodName, null);
		if (o instanceof UDF) return ((UDF) o).getFunctionArguments();
		return null;
	}

	private void callCFCMetaData(PageContext pc, Component cfc, int format) throws IOException, PageException, ConverterException {
		ComponentSpecificAccess cw = new ComponentSpecificAccess(Component.ACCESS_REMOTE, cfc);
		ComponentScope scope = cw.getComponentScope();
		Struct udfs = new StructImpl(), sctUDF, sctArg;
		Array arrArg;
		Iterator<Object> it = scope.valueIterator();
		Object v;
		UDF udf;
		FunctionArgument[] args;
		while (it.hasNext()) {
			v = it.next();
			// UDF
			if (v instanceof UDF) {
				udf = (UDF) v;
				sctUDF = new StructImpl();
				arrArg = new ArrayImpl();
				udfs.setEL(udf.getFunctionName(), sctUDF);
				args = udf.getFunctionArguments();
				for (int i = 0; i < args.length; i++) {
					sctArg = new StructImpl();
					arrArg.appendEL(sctArg);
					sctArg.setEL(KeyConstants._name, args[i].getName().getString());
					sctArg.setEL(KeyConstants._type, args[i].getTypeAsString());
					sctArg.setEL(KeyConstants._required, args[i].isRequired());
					if (!StringUtil.isEmpty(args[i].getHint())) sctArg.setEL(KeyConstants._hint, args[i].getHint());
				}
				sctUDF.set(KeyConstants._arguments, arrArg);
				sctUDF.set(KeyConstants._returntype, udf.getReturnTypeAsString());

			}
		}
		Struct rtn = new StructImpl();
		rtn.set(KeyConstants._functions, udfs);
		rtn.set(ACCEPT_ARG_COLL_FORMATS, "cfml,json");

		InputStream is;
		Charset cs = null;
		// WDDX
		if (UDF.RETURN_FORMAT_WDDX == format) {
			WDDXConverter converter = new WDDXConverter(pc.getTimeZone(), false, false);
			converter.setTimeZone(pc.getTimeZone());
			String str = converter.serialize(rtn);
			cs = getCharset(pc);
			is = new ByteArrayInputStream(str.getBytes(cs));
		}

		// JSON
		else if (UDF.RETURN_FORMAT_JSON == format) {
			int qf = SerializationSettings.SERIALIZE_AS_ROW;
			cs = getCharset(pc);
			JSONConverter converter = new JSONConverter(false, cs);
			String str = converter.serialize(pc, rtn, qf);
			is = new ByteArrayInputStream(str.getBytes(cs));

		}
		// CFML
		else if (UDF.RETURN_FORMAT_SERIALIZE == format) {
			ScriptConverter converter = new ScriptConverter(false);
			String str = converter.serialize(rtn);
			cs = getCharset(pc);
			is = new ByteArrayInputStream(str.getBytes(cs));
		}
		// XML
		else if (UDF.RETURN_FORMAT_XML == format) {
			XMLConverter converter = new XMLConverter(pc.getTimeZone(), false);
			converter.setTimeZone(pc.getTimeZone());
			String str = converter.serialize(rtn);
			cs = getCharset(pc);
			is = new ByteArrayInputStream(str.getBytes(cs));
		}
		// Plain
		else if (UDF.RETURN_FORMAT_PLAIN == format) {
			String str = Caster.toString(rtn);
			cs = getCharset(pc);
			is = new ByteArrayInputStream(str.getBytes(cs));
		}
		// Java
		else if (UDF.RETURN_FORMAT_JAVA == format) {
			byte[] bytes = JavaConverter.serializeAsBinary(rtn);
			is = new ByteArrayInputStream(bytes);

		}
		else throw new IOException("invalid format defintion:" + format);

		OutputStream os = null;
		try {
			os = pc.getResponseStream();
			setFormat(pc.getHttpServletResponse(), format, cs);
			IOUtil.copy(is, os, false, false);

		}
		finally {
			IOUtil.flushEL(os);
			IOUtil.close(os);
			((PageContextImpl) pc).getRootOut().setClosed(true);
		}
	}

	private Charset getCharset(PageContext pc) {
		HttpServletResponse rsp = pc.getHttpServletResponse();
		Charset cs = ReqRspUtil.getCharacterEncoding(pc, rsp);
		if (cs == null) cs = pc.getWebCharset();
		return cs;
	}

	private void callWSDL(PageContext pc, Component component) throws ServletException, IOException, PageException {
		// take wsdl file defined by user
		String wsdl = component.getWSDLFile();
		if (!StringUtil.isEmpty(wsdl)) {

			OutputStream os = null;
			Resource input = ResourceUtil.toResourceExisting(pc, wsdl);
			try {
				os = pc.getResponseStream();
				ReqRspUtil.setContentType(pc.getHttpServletResponse(), "text/xml; charset=utf-8");
				IOUtil.copy(input, os, false);

			}
			finally {
				IOUtil.flushEL(os);
				IOUtil.close(os);
				((PageContextImpl) pc).getRootOut().setClosed(true);
			}
		}
		// create a wsdl file
		else {
			((ConfigWebPro) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSServer(pc).doGet(pc, pc.getHttpServletRequest(), pc.getHttpServletResponse(), component);
		}
	}

	private void callWebservice(PageContext pc, Component component) throws IOException, ServletException, PageException {
		((ConfigWebPro) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSServer(pc).doPost(pc, pc.getHttpServletRequest(), pc.getHttpServletResponse(), component);
	}

	/**
	 * default implementation of the static constructor, that does nothing
	 */
	public void staticConstructor(PageContext pagecontext, ComponentImpl cfc) {
		// do nothing
	}

	// this method only exist that old classes from archives still work, not perfectly, but good enough
	public StaticStruct getStaticStruct() {
		return new StaticStruct();
	}

	public abstract void initComponent(PageContext pc, ComponentImpl c, boolean executeDefaultConstructor) throws PageException;

	public void ckecked() {
		lastCheck = System.currentTimeMillis();
	}

	public long lastCheck() {
		return lastCheck;
	}

	public String getComponentName() {
		return getPageSource().getComponentName();
	}

}

class Props {

	public String strType = "any";
	public boolean secureJson;
	public int type = CFTypes.TYPE_ANY;
	public int format = UDF.RETURN_FORMAT_WDDX;
	public boolean output = true;

}