package query;


import htsjdk.tribble.readers.TabixReader.Iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import bean.config.run.CountRunBean;
import bean.config.run.DBRunConfigBean;
import bean.config.run.DBRunConfigBean.IndexFormat;
import bean.index.DBBean;
import bean.node.Node;
import bean.query.LineReaderBasic;


public class TabixQuery<T> extends AbstractQuery<T>{

	protected List<DBBean> dbBeans;
	public TabixQuery(final CountRunBean config, final int index, final LineReaderBasic queryLineReader) {
		super(config, index, queryLineReader);
	}

	public void init() throws IOException {
		super.init(); 
		
		List<DBRunConfigBean> dbFiles = config.getDbs();
		dbBeans = new ArrayList<DBBean>(dbFiles.size());
		for (int i = 0; i < dbFiles.size(); i++) {
			if(dbFiles.get(i).getIndexFormat() == IndexFormat.TBI) {
				dbBeans.add(new DBBean(dbFiles.get(i), dbFiles.get(i).getPrintter(threadIndex)) );
			} else {
				throw new IllegalArgumentException("You must have a tabix index file(.tbi) first to use mode 0!");
			}
		}
	}
	
	public void teardown() {
		if(this.dbBeans != null && this.dbBeans.size() > 0) {
			for (DBBean dbBean : dbBeans) {
				dbBean.getTabixReader().close();
			}
		}
	}
	
	@Override
	public void doQuery() {
		Node node = null;
		try {
			while((node = queryLineReader.nextNodeSpider()) != null) {
				this.randomAccessQuery(node);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void randomAccessQuery(Node node) {
		try {
			Iterator it ;
			for (DBBean dbBean : dbBeans) {
				if(config.isFc()) {
					it = dbBean.getTabixReader().query(dbBean.chr2tid(node.chr), node.beg-1 , node.end+1);
				} else {
					it = dbBean.getTabixReader().query(dbBean.chr2tid(node.chr), node.beg , node.end);
				}
				
				String s = null;
				while((s = it.next()) != null) {
//					System.out.println(s);
					dbBean.getPrintter().print(node.index + "\t" + s);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
