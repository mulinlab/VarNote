/**
 * 
 */
package mapreduce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MapReduce FrameWork main class
 * @author Li Jun Mulin
 *
 */
public class ReducerBalancedMapReduce<T,M> implements MapReduce<T,M> {

	private ExecutorService executor;
	private BlockingQueue<Runnable> workQueue;
	private Map<Reducer,List<Mapper>> mappers;
	private boolean running;
	
	public ReducerBalancedMapReduce(int threadPoolSize) {
		mappers = new HashMap<Reducer, List<Mapper>>();
		workQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(threadPoolSize,threadPoolSize,Long.MAX_VALUE,TimeUnit.SECONDS,workQueue);
		running = true;
		init();
	}
	
	public boolean addMapper(Mapper mapper){
//		synchronized (mappers) {
//			mappers.add(mapper);
//			mappers.notify();
//		}
		return false;
	}
	
	public T getResult(){
		return null;
	}

	private void init(){
		while(running){
			synchronized (mappers) {
				if(mappers.isEmpty()){
					try {
						mappers.wait();
					} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		}
	}
	
}
