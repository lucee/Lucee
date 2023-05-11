package lucee.runtime;

// FUTURE add to Page and delete this class
public abstract class PageImpl extends Page implements PagePro {

	@Override
	public int getHash() {
		return 0;
	}

	@Override
	public long getSourceLength() {
		return 0;
	}

	@Override
	public String getSubname() {
		return null;
	}
}
