/**
 * 
 */
package mapreduce;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Li Jun Mulin
 *
 */
public class SimpleMapReduce<T,M> implements MapReduce<T,M> {

	private ExecutorService executor;
	private BlockingQueue<Runnable> workQueue;
	private List<Mapper<M>> mappers;
	private Reducer<T,Mapper<M>,M> reducer;
	
	public SimpleMapReduce(int coreNumber, Reducer<T,Mapper<M>,M> r) {
		mappers = new LinkedList<Mapper<M>>();
		reducer = r;
		workQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(coreNumber,coreNumber,Long.MAX_VALUE,TimeUnit.SECONDS,workQueue);
	}
	
	public boolean addMapper(Mapper<M> mapper){
		if(executor.isTerminated())
			return false;
		LocalRunner r = new LocalRunner(mapper);
		mappers.add(mapper);
		executor.execute(r);
		return true;
	}
	
	public T getResult(){
		try {
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		T o = reducer.doReducer(mappers);
		return o;
	}

	class LocalRunner implements Runnable{
		private Mapper<M> local;
		public LocalRunner(Mapper<M> mapper) {
			local = mapper;
		}
		public void run() {
			local.doMap();
		}
	}

}
