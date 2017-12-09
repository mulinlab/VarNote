package bean.index;

import java.io.IOException;

import postanno.FormatWithRef;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.readers.TabixReader;
import bean.config.Printter;
import bean.config.run.DBRunConfigBean;

public class DBBean {
	private final TabixReader tabixReader;
	private final Printter printter;
	private final TbiIndex index;
	
	public DBBean(final DBRunConfigBean config, final Printter printter) throws IOException {
		super();
		this.tabixReader = new TabixReader(config.getDbPath(), config.getDbIndex());
		this.printter = printter;
		this.index = new TbiIndex(config.getDbIndex());
	}
	
	public TabixReader getTabixReader() {
		return tabixReader;
	}

	public Printter getPrintter() {
		return printter;
	}

	public TabixFormat getFormat() {
		return index.getFormat();
	}
	
	public FormatWithRef getFormatWithRef() {
		return index.getFormatWithRef();
	}

	public int chr2tid(final String chr) {
        Integer tid = index.getmChr2tid().get(chr);
        return tid == null ? -1 : tid;
    }
}
