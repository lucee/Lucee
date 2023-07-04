package lucee.commons.cpu;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Cookie;

import lucee.commons.cpu.CPULogger.StaticData;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public abstract class CFMLListener implements Listener {

	private ConfigWeb config;

	public CFMLListener() {
		this.config = (ConfigWeb) ThreadLocalPageContext.getConfig();
	}

	@Override
	public final void listen(List<StaticData> list) {
		PageContext pc = ThreadLocalPageContext.get();
		boolean release = false;
		if (pc == null) {
			release = true;
			pc = ThreadUtil.createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", "/", "", new Cookie[0], new Pair[0], null, new Pair[0],
					new StructImpl(), false, -1);
		}
		try {
			_listen(pc, list);
		}
		catch (PageException pe) {
			LogUtil.log(pc, "application", "cpu", pe);
		}
		finally {
			if (release) {
				CFMLFactory f = pc.getConfig().getFactory();
				f.releaseLuceePageContext(pc, true);
			}
			// ThreadLocalPageContext.register(oldPC);
		}
	}

	private static final Key PERCENTAGE = KeyImpl.getInstance("percentage");
	private static final Key[] columns = new Key[] { KeyConstants._name, PERCENTAGE, KeyConstants._stacktrace, KeyConstants._time, KeyConstants._total };

	protected Object toQuery(List<StaticData> list) throws PageException {
		StaticData sd = null;
		QueryImpl qry = new QueryImpl(columns, list.size(), "cpu");
		Iterator<StaticData> it = list.iterator();
		int row = 0;
		while (it.hasNext()) {
			row++;
			sd = it.next();
			qry.setAt(KeyConstants._name, row, sd.name);
			qry.setAt(PERCENTAGE, row, sd.getPercentage());
			qry.setAt(KeyConstants._stacktrace, row, sd.getStacktrace());
			qry.setAt(KeyConstants._time, row, sd.getTime());
			qry.setAt(KeyConstants._total, row, sd.getTotal());
		}
		return qry;
	}

	public abstract void _listen(PageContext pc, List<StaticData> list) throws PageException;

}