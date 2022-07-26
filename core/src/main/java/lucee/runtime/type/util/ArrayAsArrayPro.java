package lucee.runtime.type.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryArrayIterator;

public class ArrayAsArrayPro implements ArrayPro {

	private Array array;

	public ArrayAsArrayPro(Array array) {
		this.array = array;
	}

	@Override
	public Iterator<Entry<Integer, Object>> entryArrayIterator() {
		return new EntryArrayIterator(array, array.intKeys());
	}

	@Override
	public Object pop() throws PageException {
		return array.removeE(size());
	}

	@Override
	public Object pop(Object defaultValue) {
		try {
			return array.removeE(size());
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Object shift() throws PageException {
		return array.removeE(1);
	}

	@Override
	public Object shift(Object defaultValue) {
		try {
			return array.removeE(1);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	/////////////////////////// array methods //////////////////////////////
	@Override
	public Object clone() {
		return array.duplicate(true);
	}

	@Override
	public Object append(Object arg0) throws PageException {
		return array.append(arg0);
	}

	@Override
	public Object appendEL(Object arg0) {
		return array.appendEL(arg0);
	}

	@Override
	public boolean containsKey(int arg0) {
		return array.containsKey(arg0);
	}

	@Override
	public Object get(int arg0, Object arg1) {
		return array.get(arg0, arg1);
	}

	@Override
	public int getDimension() {
		return array.getDimension();
	}

	@Override
	public Object getE(int arg0) throws PageException {
		return array.getE(arg0);
	}

	@Override
	public boolean insert(int arg0, Object arg1) throws PageException {
		return array.insert(arg0, arg1);
	}

	@Override
	public int[] intKeys() {
		return array.intKeys();
	}

	@Override
	public Object prepend(Object arg0) throws PageException {
		return array.prepend(arg0);
	}

	@Override
	public Object removeE(int arg0) throws PageException {
		return array.removeE(arg0);
	}

	@Override
	public Object removeEL(int arg0) {
		return array.removeEL(arg0);
	}

	@Override
	public void resize(int arg0) throws PageException {
		array.resize(arg0);
	}

	@Override
	public Object setE(int arg0, Object arg1) throws PageException {
		return array.setE(arg0, arg1);
	}

	@Override
	public Object setEL(int arg0, Object arg1) {
		return array.setEL(arg0, arg1);
	}

	@Override
	public void sort(String arg0, String arg1) throws PageException {
		array.sort(arg0, arg1);
	}

	@Override
	public void sortIt(Comparator arg0) {
		array.sortIt(arg0);
	}

	@Override
	public Object[] toArray() {
		return array.toArray();
	}

	@Override
	public List toList() {
		return array.toList();
	}

	@Override
	public void clear() {
		array.clear();
	}

	@Override
	public boolean containsKey(String arg0) {
		return array.containsKey(arg0);
	}

	@Override
	public boolean containsKey(Key arg0) {
		return array.containsKey(arg0);
	}

	@Override
	public Collection duplicate(boolean arg0) {
		return array.duplicate(arg0);
	}

	@Override
	public Object get(String arg0) throws PageException {
		return array.get(arg0);
	}

	@Override
	public Object get(Key arg0) throws PageException {
		return array.get(arg0);
	}

	@Override
	public Object get(String arg0, Object arg1) {
		return array.get(arg0, arg1);
	}

	@Override
	public Object get(Key arg0, Object arg1) {
		return array.get(arg0, arg1);
	}

	@Override
	public Key[] keys() {
		return array.keys();
	}

	@Override
	public Object remove(Key arg0) throws PageException {
		return array.remove(arg0);
	}

	@Override
	public Object remove(Key arg0, Object arg1) {
		return array.remove(arg0, arg1);
	}

	@Override
	public Object removeEL(Key arg0) {
		return array.removeEL(arg0);
	}

	@Override
	public Object set(String arg0, Object arg1) throws PageException {
		return array.set(arg0, arg1);
	}

	@Override
	public Object set(Key arg0, Object arg1) throws PageException {
		return array.set(arg0, arg1);
	}

	@Override
	public Object setEL(String arg0, Object arg1) {
		return array.setEL(arg0, arg1);
	}

	@Override
	public Object setEL(Key arg0, Object arg1) {
		return array.setEL(arg0, arg1);
	}

	@Override
	public int size() {
		return array.size();
	}

	@Override
	public DumpData toDumpData(PageContext arg0, int arg1, DumpProperties arg2) {
		return array.toDumpData(arg0, arg1, arg2);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return array.entryIterator();
	}

	@Override
	public Iterator<Key> keyIterator() {
		return array.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return array.keysAsStringIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return array.valueIterator();
	}

	@Override
	public Boolean castToBoolean(Boolean arg0) {
		return array.castToBoolean(arg0);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return array.castToBooleanValue();
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return array.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime arg0) {
		return array.castToDateTime(arg0);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return array.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double arg0) {
		return array.castToDoubleValue(arg0);
	}

	@Override
	public String castToString() throws PageException {
		return array.castToString();
	}

	@Override
	public String castToString(String arg0) {
		return array.castToString(arg0);
	}

	@Override
	public int compareTo(String arg0) throws PageException {
		return array.compareTo(arg0);
	}

	@Override
	public int compareTo(boolean arg0) throws PageException {
		return array.compareTo(arg0);
	}

	@Override
	public int compareTo(double arg0) throws PageException {
		return array.compareTo(arg0);
	}

	@Override
	public int compareTo(DateTime arg0) throws PageException {
		return array.compareTo(arg0);
	}

	@Override
	public Object call(PageContext arg0, Key arg1, Object[] arg2) throws PageException {
		return array.call(arg0, arg1, arg2);
	}

	@Override
	public Object callWithNamedValues(PageContext arg0, Key arg1, Struct arg2) throws PageException {
		return array.callWithNamedValues(arg0, arg1, arg2);
	}

	@Override
	public Object get(PageContext arg0, Key arg1) throws PageException {
		return array.get(arg0, arg1);
	}

	@Override
	public Object get(PageContext arg0, Key arg1, Object arg2) {
		return array.get(arg0, arg1, arg2);
	}

	@Override
	public Object set(PageContext arg0, Key arg1, Object arg2) throws PageException {
		return array.set(arg0, arg1, arg2);
	}

	@Override
	public Object setEL(PageContext arg0, Key arg1, Object arg2) {
		return array.setEL(arg0, arg1, arg2);
	}

	@Override
	public Iterator<?> getIterator() {
		return array.getIterator();
	}

}
