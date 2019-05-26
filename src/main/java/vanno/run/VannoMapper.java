package main.java.vanno.run;
import main.java.vanno.bean.config.run.OverlapRunConfig;
import main.java.vanno.bean.config.run.OverlapRunConfig.Mode;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.LineReaderBasic;
import main.java.vanno.mapreduce.Mapper;
import main.java.vanno.query.AbstractQuery;
import main.java.vanno.query.VannoQuery;
import main.java.vanno.query.SweepQuery;
import main.java.vanno.query.TabixQuery;


public final class VannoMapper<T> implements Mapper<T>{
	final private AbstractQuery queryEngine;
	final private OverlapRunConfig config;
	final private int index;
	private long count;
	
	public VannoMapper(final OverlapRunConfig config, final int index) {
		super();
		this.config = config;
		this.index = index;
		this.count = 0;
		if(config.getMode() == Mode.TABIX) {
			queryEngine = new TabixQuery(config);
		} else if(config.getMode() == Mode.SWEEP) {
			queryEngine = new SweepQuery(config);
		} else {
			queryEngine = new VannoQuery(config);
		}
	}

	@Override
	public void doMap() {
//		long t1 = System.currentTimeMillis();
		try {
	
			LineReaderBasic spider = config.getQuery().getSpider(index);
			
			Node node = new Node();
			while((node = spider.nextNode(node)) != null) {
				queryEngine.doQuery(node);
				if(config.isCount()) {
					this.count = queryEngine.getResultCount();
				} else {
					config.printRecord(node, queryEngine.getResults(), index);
				}
			}
			queryEngine.teardown();
//			long t2 = System.currentTimeMillis();
//			System.out.println(" Time:" + (t2 - t1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getResult() {
		return (T)new Long(this.count);
	}
	

}
