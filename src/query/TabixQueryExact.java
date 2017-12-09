package query;

import htsjdk.tribble.readers.TabixReader.Iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import postanno.FormatWithRef;
import bean.config.run.CountRunBean;
import bean.config.run.DBRunConfigBean;
import bean.config.run.DBRunConfigBean.IndexFormat;
import bean.index.DBBean;
import bean.node.Node;
import bean.node.NodeFactory;
import bean.query.LineReaderBasic;

public class TabixQueryExact<T> extends AbstractQuery<T>{

	protected List<DBBean> dbBeans;
	protected FormatWithRef formatWithRef;
	
	public TabixQueryExact(final CountRunBean config, final int index, final LineReaderBasic queryLineReader) {
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
			for (DBBean dbBean : dbBeans) {
				Iterator it = dbBean.getTabixReader().query(dbBean.chr2tid(node.chr), node.beg , node.end);
				String s = null;
				Node db;
				while((s = it.next()) != null) {
					db = NodeFactory.createBasic(s, dbBean.getFormatWithRef());
					if((node.beg == db.beg) && (node.end == db.end)) {
						dbBean.getPrintter().print(node.index + "\t" + s);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
