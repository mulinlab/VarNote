package main.java.vanno.bean.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import main.java.vanno.bean.format.BEDHeaderParser;
import main.java.vanno.bean.format.Format;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.FileType;
import main.java.vanno.stream.BZIP2InputStream;
import main.java.vanno.stream.GZInputStream;


public final class Query {
	private FileType fileType;
	private String queryPath;
	private Format queryFormat;
	private String queryName;
	private List<LineReaderBasic> spider;
	
	public Query(final String path) {
		this(path, null);
	}
	
	public Query(final String path, final Format queryFormat) {
		LineReaderBasic.readHeader = false;
		if(path == null || path.trim().equals("")) throw new InvalidArgumentException("Query file path is required.");
		
		if(SeekableStreamFactory.isFilePath(path)) {
			this.queryPath = new File(path).getAbsolutePath();
		} else {
			this.queryPath = path;
		}
		IOUtil.assertInputIsValid(this.queryPath);
		
		fileType = VannoUtils.checkFileType(this.queryPath);
		queryName = new File(this.queryPath).getName();
		
		if(queryFormat == null) this.queryFormat = Format.defaultFormat(path, true);	
		else this.queryFormat = queryFormat;
		
		spider = new ArrayList<LineReaderBasic>();
	}
	
	public LineReaderBasic loadQueryFile() {
		try {
			return new LineReaderBasic(VannoUtils.getReader(queryPath), queryFormat, 0);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public void loadSpiderWithThread(final int thread) {
		try {
			if (fileType == FileType.BGZ || fileType == FileType.TXT) {
				BZIP2InputStream bz2_text = new BZIP2InputStream(queryPath, thread);
				bz2_text.adjustPos();
				bz2_text.creatSpider();

				if (bz2_text.getThreadNum() != thread) {
					if (bz2_text.getThreadNum() == 1) {
						spider.add(new LineReaderBasic(bz2_text.spider[0], queryFormat, 0));
					} else {
						throw new InvalidArgumentException("Split file with error!");
					}
				} else {
					for (int i = 0; i < thread; i++) {
						spider.add(new LineReaderBasic(bz2_text.spider[i], queryFormat, i));
					}
				}
			} else {
				GZInputStream in = new GZInputStream(queryPath, thread);
				for (int i = 0; i < thread; i++) {
					spider.add(new LineReaderBasic(in.getReader(i), queryFormat, i));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LineReaderBasic getSpider(final int index) {
		if((index < 0) || (index >= spider.size())) throw new InvalidArgumentException("Min thread number is 0 and max thread number is " + spider.size());
		return spider.get(index);
	}
	
	public void nextNode() {
		
	}
	public Format getQueryFormat() {
		return queryFormat;
	}

	public String getQueryName() {
		return queryName;
	}

	public String getQueryPath() { 
		return queryPath;
	}

	public void setQueryFormat(Format queryFormat) {
		this.queryFormat = queryFormat;
	}

	public FileType getFileType() {
		return fileType;
	}

	public int getSpiderSize() {
		return spider.size();
	}

	public void printLog(final Log log) {
		List<String> colNmaes = queryFormat.getOriginalField();
		log.printStrWhite("\n\n----------------------------------------------------  QUERY  --------------------------------------------------------------");
		log.printKVKCYN("Read Query File", log.isLog() ? new File(queryPath).getName() : queryPath);
		log.printKVKCYN("Query Format is", queryFormat.toString());
		if(!queryFormat.getCommentIndicator().equals("##")) log.printStrNon("Comment indicator of query is " + queryFormat.getCommentIndicator());
		if(colNmaes != null && queryFormat.isHasHeader())  log.printStrNon("Query header is " + StringUtil.join(BEDHeaderParser.COMMA, colNmaes));
	}
	
	
}
