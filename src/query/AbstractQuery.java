package query;

import java.io.IOException;

import bean.config.run.CountRunBean;
import bean.query.LineReaderBasic;

public abstract class AbstractQuery<T> implements Query<T>{
	protected final CountRunBean config;
	protected final int threadIndex;
	protected final LineReaderBasic queryLineReader;
	
	public AbstractQuery(final CountRunBean config, final int threadIndex, final LineReaderBasic queryLineReader) {
		super();
		this.config = config;
		this.threadIndex = threadIndex;
		this.queryLineReader = queryLineReader;
		try {
			this.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void init() throws IOException {

	}
	
	@Override
	public abstract void doQuery();
	
	public abstract void teardown();

	
	@Override
	public T getResult() {
		return null;
	}
}
