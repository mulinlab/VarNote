package org.mulinlab.varnote.operations.readers.gt1000g;


import htsjdk.samtools.util.BlockCompressedInputStream;
import javafx.util.Pair;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.index.Bin;
import org.mulinlab.varnote.operations.index.Variant;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;

public final class VannoGTReader {
    public static final int LD_DISTANCE = (int)(0.1*1000*1000);

    private BlockCompressedInputStream gtFileInputStream;
    private VannoGTIndex index;

    public VannoGTReader(final String file) throws IOException {
        this.gtFileInputStream = new BlockCompressedInputStream(new File(file + GlobalParameter.GT_COMPRESS_FILE));
        this.index = new VannoGTIndex(file + GlobalParameter.GT_COMPRESS_FILE_IDX);
    }

    public void close() throws IOException {
        gtFileInputStream.close();
    }

    public Pair<Double, Double> computeLD(final LocFeature v1, final LocFeature v2) throws IOException {
        return computeLd(findVariant(v1), findVariant(v2));
    }

    public Pair<Double, Double> computeLd(final Variant va, final Variant vb) {
        if(va == null || vb == null) {
            return null;
        }

        BitSet missingA = va.getCalls()[1];
        BitSet missingInEither = (BitSet) missingA.clone();
        missingInEither.or(vb.getCalls()[1]);

        BitSet oneInThis = (BitSet) va.getCalls()[0].clone();
        oneInThis.andNot(missingInEither);

        BitSet oneInThat = (BitSet) vb.getCalls()[0].clone();
        oneInThat.andNot(missingInEither);

        BitSet oneInBoth = (BitSet) oneInThis.clone();
        oneInBoth.and(oneInThat);

        long n = oneInThis.length() - missingInEither.cardinality();
        long ab = oneInBoth.cardinality();
        long a = oneInThis.cardinality();
        long b = oneInThat.cardinality();

        if (n == 0 || a == 0 || b == 0 || n == a || n == b) {
            return new Pair<>(0.0, 0.0);
        }

        double d = (double) (n * ab - a * b);
        double r = d / Math.sqrt(a * (n - a) * b * (n - b));
        double dPrime = d / (d < 0 ? Math.max(- a * b, -(n - a) * (n - b)) : Math.min(a * (n - b), (n - a) * b));

        return new Pair(r, dPrime);
    }

    public Variant findVariant(final LocFeature locFeature) throws IOException {
        List<Bin> binList = index.getBinList(locFeature.chr).getList();
        for (int i = 0; i < binList.size(); i++) {
            if(binList.get(i).getMin() <= (locFeature.beg + 1) && binList.get(i).getMax() >= (locFeature.beg + 1)) {
                return findVariantInBin(binList.get(i), locFeature);
            }
        }
        return null;
    }

    public Variant findVariantInBin(final Bin bin, final LocFeature locFeature) throws IOException {
        gtFileInputStream.seek(bin.getAddress());
        Variant variant = new Variant();
        int pos = GlobalParameter.readInt(gtFileInputStream);

        while (pos != GlobalParameter.GT_FILE_END) {
            variant.setPos(pos);
            variant.setVaiant(gtFileInputStream);

            if(pos == locFeature.beg + 1) {
                return variant;
            } else if(pos > locFeature.beg + 1) {
                break;
            }
            pos = GlobalParameter.readInt(gtFileInputStream);
        }

        return null;
    }

    public void test() throws IOException {
        gtFileInputStream.seek(0);

        Variant variant = new Variant();
        int pos = GlobalParameter.readInt(gtFileInputStream);

        while (pos != GlobalParameter.GT_FILE_END) {
            variant.setPos(pos);
            variant.setVaiant(gtFileInputStream);
        }
    }
}
