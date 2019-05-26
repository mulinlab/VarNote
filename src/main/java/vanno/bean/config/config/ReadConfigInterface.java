package main.java.vanno.bean.config.config;


/**
 * Mapper Interface
 * @author Li Jun Mulin
 *
 */
public interface ReadConfigInterface<T> {
	
	public T read(final String filePath);
	
	public void doInit();
	public void processLine(final String line);
	public boolean filterLine(final String line);
	public boolean isBreak(final String line);
	public T doEnd();
}
