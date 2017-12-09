import query.AbstractQuery;
import mapreduce.Mapper;


public class VannoMapper<T> implements Mapper<T>{
	protected AbstractQuery<T> queryEngine;
	public VannoMapper(AbstractQuery<T> queryEngine) {
		super();
		this.queryEngine = queryEngine;
	}

	@Override
	public void doMap() {
		try {
			queryEngine.doQuery();
			queryEngine.teardown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public T getResult() {
		return (T) queryEngine.getResult();
	}
}
