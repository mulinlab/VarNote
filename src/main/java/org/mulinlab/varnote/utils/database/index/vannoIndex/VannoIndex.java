package org.mulinlab.varnote.utils.database.index.vannoIndex;

import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.utils.database.index.Index;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import java.io.IOException;
import java.util.List;

public class VannoIndex extends Index{
	protected String commentIndicator;
	protected List<String> headerColList;
	protected int version;
	
	public VannoIndex(final BlockCompressedInputStream is, final int version) {
		super(is);
		this.version = version;
		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void readMagic() throws IOException {
		int magicNUmber = GlobalParameter.readInt(is);
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
