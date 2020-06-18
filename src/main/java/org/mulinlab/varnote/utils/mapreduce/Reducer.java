package org.mulinlab.varnote.utils.mapreduce;

import java.util.List;

/**
 * Reducer Interface
 * @author Li Jun Mulin
 * @param <M>
 *
 */
public interface Reducer<T,V extends Mapper<M>, M> {

	public T doReducer(List<V> mappers);
	
}
