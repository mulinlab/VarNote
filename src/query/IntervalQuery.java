package query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import constants.VannoUtils.PROGRAM;
import process.CountResultProcess;
import process.MyResultProcess;
import process.TabixResultProcess;
import readers.MixPlusReader;
import readers.MixReader;
import readers.MixTbiReader;
import stack.ExactStack;
import stack.IntervalStack;
import stack.IntervalStackFC;
import bean.config.run.CountRunBean;
import bean.config.run.DBRunConfigBean;
import bean.config.run.DBRunConfigBean.IndexFormat;
import bean.node.Node;
import bean.query.LineReaderBasic;
import bean.query.QueryLineReaderWithValidation;


public class IntervalQuery<T> extends AbstractQuery<T>{
	
	private List<MixReader> readers;
	private String chr;
	public IntervalQuery(final CountRunBean config, final int index, final LineReaderBasic queryLineReader) {
		super(config, index, queryLineReader);
	}

	public void init() throws IOException {
		super.init();
		List<DBRunConfigBean> dbFiles = config.getDbs();
		readers = new ArrayList<MixReader>(dbFiles.size());
		MixReader tempReader;	
		for (DBRunConfigBean dbf : dbFiles) {
			if(dbf.getIndexFormat() == IndexFormat.TBI) {
				tempReader = addTabixIndex(dbf);
			} else {
				tempReader = addPlusIndex(dbf);
			}
			readers.add(tempReader);
		}
	}
	
	public MixReader addPlusIndex(DBRunConfigBean dbf) throws IOException {

		if(config.isExact()) {
			return new MixPlusReader(dbf.getDbPath(), dbf.getIdx(), dbf.getPlusFile(), new ExactStack(new MyResultProcess()), dbf.getPrintter(threadIndex));
		} else {
			if(config.isFc()) {
				if(config.getPro() == PROGRAM.COUNT) {
					return new MixPlusReader(dbf.getDbPath(), dbf.getIdx(), dbf.getPlusFile(), new IntervalStackFC(new CountResultProcess()), null);
				} else {
					return new MixPlusReader(dbf.getDbPath(), dbf.getIdx(), dbf.getPlusFile(), new IntervalStackFC(new MyResultProcess()), dbf.getPrintter(threadIndex));
				}
			} else {
				if(config.getPro() == PROGRAM.COUNT) {
					return new MixPlusReader(dbf.getDbPath(), dbf.getIdx(), dbf.getPlusFile(), new IntervalStack(new CountResultProcess()), null);
				} else {
					return new MixPlusReader(dbf.getDbPath(), dbf.getIdx(), dbf.getPlusFile(), new IntervalStack(new MyResultProcess()), dbf.getPrintter(threadIndex));
				}
				
			}
		}
	}
	
	public MixReader addTabixIndex(DBRunConfigBean dbf) throws IOException {
		if(config.isExact()) {
			return new MixTbiReader(dbf.getDbPath(), dbf.getIdx(), new ExactStack(new TabixResultProcess(dbf.getPrintter(threadIndex))));
		} else {
			if(config.isFc()) {
				return new MixTbiReader(dbf.getDbPath(), dbf.getIdx(), new IntervalStackFC(new TabixResultProcess(dbf.getPrintter(threadIndex))));
			} else {
				return new MixTbiReader(dbf.getDbPath(), dbf.getIdx(), new IntervalStack(new TabixResultProcess(dbf.getPrintter(threadIndex))));
			}
		}
	}
	
	@Override
	public void teardown() {
		if(this.readers != null && this.readers.size() > 0) {
			for (MixReader tr : readers) {
				tr.close();
			}
		}
	}

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
			for(int i=0; i<readers.size(); i++) {
				readers.get(i).doEnd();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public T getResult() {
		long count = 0;
		for(int i=0; i<readers.size(); i++) {
			count = count + ((CountResultProcess)readers.get(i).getProcess()).getResults();
		}
		return (T)new Long(count);
	}
}
