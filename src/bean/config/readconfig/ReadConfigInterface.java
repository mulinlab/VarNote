package bean.config.readconfig;


/**
 * Mapper Interface
 * @author Li Jun Mulin
 *
 */
public interface ReadConfigInterface<T> {
	
	public T read(T obj, final String filePath);
	
	public void processLine(final T obj, final String line);
	public boolean filterLine(final T obj, final String line);
	public boolean isBreak(final String line);

	public void doEnd(T obj);
}
