package org.mulinlab.varnote.operations.readers.gt1000g;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.index.Bin;
import org.mulinlab.varnote.operations.index.BinList;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class VannoGTIndex {

    private final String indexPath;
    private Map<String, Integer> mChr2tid;
    private Map<Integer, BinList> chrMap;

    private byte[] longbuf = new byte[8];

    public VannoGTIndex(final String filePath) {
        IOUtil.assertFileIsReadable(new File(filePath));
        this.indexPath = filePath;

        this.mChr2tid = new HashMap<>();
        this.chrMap = new HashMap<>();

        try {
            readIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readIndex() throws IOException {
        BlockCompressedInputStream in = new BlockCompressedInputStream(new File(this.indexPath));

        final int seqSize = GlobalParameter.readInt(in);
        int seqNameLen;

        for (int i = 0; i < seqSize; i++) {
            seqNameLen = GlobalParameter.readInt(in);
            byte[] b = new byte[seqNameLen];
            in.read(b);
            mChr2tid.put(new String(b), i);
        }

        int binSize, beg = 0, end = 0;
        BinList binList;
        for (int i = 0; i < seqSize; i++) {
            binSize = GlobalParameter.readInt(in);
            binList = new BinList();

            for (int j = 0; j < binSize; j++) {
                beg = beg + GlobalParameter.readInt(in);
                end = beg + GlobalParameter.readInt(in);
                binList.addBin(new Bin(beg, end, GlobalParameter.readLong(in, longbuf)));
            }
            chrMap.put(i, binList);
        }
        in.close();
    }

    public Map<Integer, BinList> getChrMap() {
        return chrMap;
    }

    public BinList getBinList(final String chr) {
        return chrMap.get(mChr2tid.get(chr.toUpperCase().replace("CHR", "")));
    }
}
