package org.mulinlab.varnote.cmdline.txtreader.abs;


/**
 * Mapper Interface
 * @author Li Jun Mulin
 *
 */
public interface RunReaderInterface<T> {
	
	public T read(final String filePath);
	
	public void doInit();
	public void processLine(final String line);
	public boolean filterLine(final String line);
	public boolean isBreak(final String line);
	public T doEnd();
}
