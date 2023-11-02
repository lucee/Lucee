package lucee.runtime.type.scope.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lucee.commons.io.IOUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class StorageValue implements Serializable {

	private static final long serialVersionUID = 2728185742217909233L;
	private static final byte[] EMPTY = new byte[0];

	private transient Struct value;
	private long lastModified;
	private final byte[] barr;

	public StorageValue(Struct value) throws PageException {
		this.value = value;
		this.barr = serialize(value);
		this.lastModified = System.currentTimeMillis();
	}

	public long lastModified() {
		return lastModified;
	}

	public Struct getValue() throws PageException {
		if (value == null) {
			if (barr.length == 0) return null;
			value = deserialize(barr);
		}
		return value;
	}

	private static Struct deserialize(byte[] barr) throws PageException {
		if (barr == null || barr.length == 0) return null;

		ObjectInputStream ois = null;
		Struct sct = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(barr));
			sct = (Struct) ois.readObject();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			try {
				IOUtil.close(ois);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		return sct;
	}

	private static byte[] serialize(Struct sct) throws PageException {
		if (sct == null) return EMPTY;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(sct);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			try {
				IOUtil.close(oos);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		return os.toByteArray();
	}
}
