/**
 * 
 */
package mapreduce;

/**
 * @author Li Jun Mulin
 *
 */
public interface MapReduce<T,M> {
	
	public boolean addMapper(Mapper<M> mapper);
	
	public T getResult();

}
