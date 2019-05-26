package main.java.vanno.bean.database.index.vannoIndex;

import main.java.vanno.bean.database.index.Index;
import main.java.vanno.bean.database.index.IndexFactory;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.index.MyBlockCompressedInputStream;

import java.io.IOException;
import java.util.List;

public class VannoIndex extends Index{
	protected String commentIndicator;
	protected List<String> headerColList;
	protected int version;
	
	public VannoIndex(final MyBlockCompressedInputStream is, final int version) {
		super(is);
		this.version = version;
		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void readMagic() throws IOException {
		int magicNUmber = BasicUtils.readInt(is);
		if (magicNUmber != IndexFactory.MAGIC_NUMBER) {
            throw new InvalidArgumentException(String.format("Unexpected magic number 0x%x", magicNUmber));
        } 
    }
	
	public void readFormat() throws IOException{
		
	}	 
	
	public void readIndex() throws IOException{
		
	}

	public List<String> getHeaderColList() {
		return headerColList;
	}

	@Override
	public List<String> getColumnNames() {
		return headerColList;
	}

	public int getVersion() {
		return version;
	}
}
