package query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import process.MyResultProcess;
import process.TabixResultProcess;
import readers.SweepPlusReader;
import readers.SweepReader;
import readers.SweepTbiReader;
import stack.ExactStack;
import stack.IntervalStack;
import stack.IntervalStackFC;
import bean.config.run.CountRunBean;
import bean.config.run.DBRunConfigBean;
import bean.config.run.DBRunConfigBean.IndexFormat;
import bean.node.Node;
import bean.query.LineReaderBasic;
import bean.query.QueryLineReaderWithValidation;

public class SweepQuery<T> extends AbstractQuery<T>{

	protected List<SweepReader> readers;
	private String chr;
	
	public SweepQuery(final CountRunBean config, final int index, final LineReaderBasic queryLineReader) {
		super(config, index, queryLineReader);
	}

	public void init() throws IOException {
		super.init();
		
		List<DBRunConfigBean> dbFiles = config.getDbs();
		readers = new ArrayList<SweepReader>(dbFiles.size());
		SweepReader tempReader;	
		for (DBRunConfigBean dbf : dbFiles) {
			if(dbf.getIndexFormat() == IndexFormat.TBI) {
				tempReader = addTabixIndex(dbf);
			} else {
				tempReader = addMyIndex(dbf);
			}
			readers.add(tempReader);
		}
	}
	
	public SweepReader addMyIndex(DBRunConfigBean dbf) throws IOException {
		if(config.isExact()) {
			return new SweepPlusReader(dbf.getDbPath(), dbf.getDbIndex(), dbf.getPlusFile(), new ExactStack(new MyResultProcess()), dbf.getPrintter(threadIndex));
		} else {
			if(config.isFc()) {
				return new SweepPlusReader(dbf.getDbPath(), dbf.getDbIndex(), dbf.getPlusFile(), new IntervalStackFC(new MyResultProcess()), dbf.getPrintter(threadIndex));
			} else {
				return new SweepPlusReader(dbf.getDbPath(), dbf.getDbIndex(), dbf.getPlusFile(), new IntervalStack(new MyResultProcess()), dbf.getPrintter(threadIndex));
			}
		}
	}
	
	public SweepReader addTabixIndex(DBRunConfigBean dbf) throws IOException {
		if(config.isExact()) {
			return new SweepTbiReader(dbf.getDbPath(), dbf.getIdx(), new ExactStack(new TabixResultProcess(dbf.getPrintter(threadIndex))));
		} else {
			if(config.isFc()) {
				return new SweepTbiReader(dbf.getDbPath(), dbf.getIdx(), new IntervalStackFC(new TabixResultProcess(dbf.getPrintter(threadIndex))));
			} else {
				return new SweepTbiReader(dbf.getDbPath(), dbf.getIdx(), new IntervalStack(new TabixResultProcess(dbf.getPrintter(threadIndex))));
			}
		}
	}
	
	public void teardown(){
		if(this.readers != null && this.readers.size() > 0) {
			for (SweepReader tr : readers) {
				tr.close();
			}
		}
	};
	
	@Override
	public void doQuery() {
		chr = "";
		QueryLineReaderWithValidation query = (QueryLineReaderWithValidation)queryLineReader;
		Node node;
		try {
			while((node = query.nextNodeSpider()) != null) {
				if(!node.chr.equals(chr)) {
					chr = node.chr;
					System.out.println("Parsing chr:" + chr);
					for(int i=0; i<readers.size(); i++) {
						readers.get(i).initForChr(chr);
					}
				}
				
				for(int i=0; i<readers.size(); i++) {
					readers.get(i).doQuery(node);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
