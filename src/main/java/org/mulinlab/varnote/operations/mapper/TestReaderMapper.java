package org.mulinlab.varnote.operations.mapper;


import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.queryreader.ThreadLineReader;
import static org.mulinlab.varnote.utils.LoggingUtils.logger;


public final class TestReaderMapper<T> implements Mapper<T> {

	private ThreadLineReader reader;

	public TestReaderMapper(final ThreadLineReader reader) {
		this.reader = reader;
	}

	public void doMap()  {
		try {
			long t1 = System.currentTimeMillis();
			String s;
			int i = 0;
			while(true) {
				if (!((s = reader.readLine()) != null)) break;
				i++;
			}
			System.out.println("i=" + i);
			reader.close();

			long t2 = System.currentTimeMillis();
			logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public T getResult() {
		return null;
	}
}
