package bean.config.run;

import postanno.FormatWithRef;
import bean.config.readconfig.AbstractReadConfig;
import bean.config.readconfig.OverlapReadConfig;

public class OverlapHeaderBean extends AbstractRunCongig{	
	public OverlapHeaderBean() {
		super();
		pro = null;
	}
	
	public void setDBIndex(final String val) {
		checkDB(AbstractReadConfig.DB_PATH);
		cdb.setDbFormat(new FormatWithRef(val));
	}
	
	public void setDBLabel(final String val) {
		checkDB(AbstractReadConfig.DB_PATH);
		cdb.setLable(val);
	}
	
	public void newDB(final String val) {
		addDB();
		cdb = new DBRunConfigBean();
		cdb.setDb(val);
	}
	
	public static OverlapHeaderBean readCountRunBean(String configFile) {
		OverlapReadConfig<OverlapHeaderBean> readFromConfig = new OverlapReadConfig<OverlapHeaderBean>();
		OverlapHeaderBean bean = new OverlapHeaderBean();
		readFromConfig.read(bean, configFile);
		return bean;
	}

	@Override
	public void printLog() {
	};
}
