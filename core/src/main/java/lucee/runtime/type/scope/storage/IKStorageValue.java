package lucee.runtime.type.scope.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lucee.commons.collection.MapPro;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.NumberUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;

public class IKStorageValue implements Serializable {

	private static final long serialVersionUID = 2728185742217909233L;
	private static final byte[] EMPTY = new byte[0];
	
	transient MapPro<Collection.Key,IKStorageScopeItem> value;
	final long lastModified;
	final byte[] barr;

	public IKStorageValue(MapPro<Collection.Key,IKStorageScopeItem> value) throws PageException {
		this(value,serialize(value),System.currentTimeMillis());
	}
	public IKStorageValue(MapPro<Collection.Key,IKStorageScopeItem> value, byte[] barr, long lastModified) {
		this.value=value;
		this.barr=barr;
		this.lastModified=lastModified;
	}

	public IKStorageValue(byte[][] barrr) throws PageException {
		this.barr=barrr[0];
		this.lastModified=toLong(barrr[1]);
	}

	public static byte[][] toByteRepresentation(MapPro<Collection.Key,IKStorageScopeItem> value) throws PageException {
		return new byte[][]{serialize(value),NumberUtil.longToByteArray(System.currentTimeMillis())};
	}

	public static byte[][] toByteRepresentation(IKStorageValue val) throws PageException {
		return new byte[][]{val.barr,NumberUtil.longToByteArray(val.lastModified)};
	}
	
	public long lastModified() {
		return lastModified;
	}
	
	public static long toLong(byte[] barr) {
		return NumberUtil.byteArrayToLong(barr);
	}
	
	
	public MapPro<Collection.Key,IKStorageScopeItem> getValue() throws PageException {
		if(value==null) {
			if(barr.length==0) return null;
			value=deserialize(barr);
		}
		return value;
	}
	
	public static MapPro<Collection.Key,IKStorageScopeItem> deserialize(byte[] barr) throws PageException {
		if(barr==null || barr.length==0) return null;
		
		ObjectInputStream ois=null;
		MapPro<Collection.Key,IKStorageScopeItem> data=null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(barr));
			data=(MapPro<Collection.Key,IKStorageScopeItem>)ois.readObject();
		}
		catch(Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			IOUtil.closeEL(ois);
		}
		return data;
	}
	
	static byte[] serialize(MapPro<Collection.Key,IKStorageScopeItem> data) throws PageException {
		if(data==null) return EMPTY;
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(data);
		}
		catch(Exception e){
			throw Caster.toPageException(e);
		}
		finally {
			IOUtil.closeEL(oos);
		}
		return os.toByteArray();
	}
}
