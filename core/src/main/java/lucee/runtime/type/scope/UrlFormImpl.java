/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.type.scope;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.servlet.ServletInputStream;

import org.apache.commons.fileupload.disk.DiskFileItem;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.StructSupport;

public final class UrlFormImpl extends StructSupport implements URLForm {

	private static final long serialVersionUID = -5709431392572723178L;

	private final FormImpl form;
	private final URLImpl url;
	private boolean isInit;

	public UrlFormImpl(FormImpl form, URLImpl url) {
		this.form = form;
		this.url = url;
	}

	@Override
	public void initialize(PageContext pc) {
		if (isInit) return;
		isInit = true;
		form.initialize(pc);
		url.initialize(pc);
		form.addRaw(pc.getApplicationContext(), url.getRaw());
	}

	@Override
	public void reinitialize(ApplicationContext ac) {
		form.reinitialize(ac);
		url.reinitialize(ac);
	}

	@Override
	public void release(PageContext pc) {
		isInit = false;
		form.release(pc);
		url.release(pc);
	}

	@Override
	public String getEncoding() {
		return form.getEncoding();
	}

	@Override
	public void setEncoding(ApplicationContext ac, String encoding) throws UnsupportedEncodingException {
		form.setEncoding(ac, encoding);
	}

	@Override
	public int getType() {
		return form.getType();
	}

	@Override
	public String getTypeAsString() {
		return form.getTypeAsString();
	}

	@Override
	public boolean isInitalized() {
		return isInit;
	}

	@Override
	public void clear() {
		form.clear();
		url.clear();
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return form.containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return form.containsKey(pc, key);
	}

	@Override
	public final Object get(Collection.Key key) throws PageException {
		return form.get(key);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws PageException {
		return form.get(pc, key);
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		return form.get(key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return form.get(pc, key, defaultValue);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return form.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return form.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return form.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return form.valueIterator();
	}

	@Override
	public Collection.Key[] keys() {
		return form.keys();
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		return form.remove(key);
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return form.removeEL(key);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return form.set(key, value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return form.setEL(key, value);
	}

	@Override
	public int size() {
		return form.size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return form.toDumpData(pageContext, maxlevel, dp);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return form.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return form.castToBoolean(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return form.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return form.castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return form.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return form.castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return form.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return form.castToString(defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return form.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return form.compareTo(dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return form.compareTo(d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return form.compareTo(str);
	}

	public DiskFileItem getFileUpload(String key) {
		return form.getFileUpload(key);
	}

	@Override
	public PageException getInitException() {
		return form.getInitException();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return (Collection) Duplicator.duplicate(form, deepCopy);
	}

	@Override
	public void setScriptProtecting(ApplicationContext ac, boolean b) {
		form.setScriptProtecting(ac, b);
	}

	@Override
	public boolean containsValue(Object value) {
		return form.containsValue(value);
	}

	@Override
	public java.util.Collection<Object> values() {
		return form.values();
	}

	@Override
	public FormItem getUploadResource(String key) {
		return form.getUploadResource(key);
	}

	@Override
	public FormItem[] getFileItems() {
		return form.getFileItems();
	}

	public FormImpl getForm() {
		return form;
	}

	public URLImpl getURL() {
		return url;
	}

	@Override
	public ServletInputStream getInputStream() {
		return form.getInputStream();
	}
}