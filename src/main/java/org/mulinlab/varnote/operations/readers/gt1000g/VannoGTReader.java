package org.mulinlab.varnote.operations.readers.gt1000g;


import htsjdk.samtools.util.BlockCompressedInputStream;
import javafx.util.Pair;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.index.Bin;
import org.mulinlab.varnote.operations.index.Variant;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class VannoGTReader {
    public final int LD_DISTANCE = (int)(0.1*1000*1000);

    private BlockCompressedInputStream gtFileInputStream;
    private VannoGTIndex index;

    public VannoGTReader(final String file) throws IOException {
        this.gtFileInputStream = new BlockCompressedInputStream(new File(file));
        this.index = new VannoGTIndex(file + GlobalParameter.GT_IDX);
    }

    public void close() throws IOException {
        gtFileInputStream.close();
    }

    public Pair<Double, Double> computeLD(final LocFeature v1, final LocFeature v2) throws IOException {
        return computeLd(findVariant(v1), findVariant(v2));
    }

    public List<LDVariant> getLDList(final LocFeature v1, final double cutoff) throws IOException {
        return findVariantsRelated(v1, cutoff);
    }

    private List<LDVariant> findVariantsRelated(final LocFeature locFeature, final double cutoff) throws IOException {
        List<Bin> binList = index.getBinList(locFeature.chr).getList();

        int beg = locFeature.beg - LD_DISTANCE + 1, end = locFeature.end + LD_DISTANCE;
        for (int i = 0; i < binList.size(); i++) {
            if(binList.get(i).getMin() <= beg && binList.get(i).getMax() >= beg) {
                return findVariantsRelatedInBin(binList.get(i), locFeature, beg, end, cutoff);
            }
        }
        return null;
    }

    private List<LDVariant> findVariantsRelatedInBin(final Bin bin, final LocFeature locFeature, final int beg, int end, final double cutoff) throws IOException {
        gtFileInputStream.seek(bin.getAddress());
        Variant variant, query = null;
        int pos = GlobalParameter.readInt(gtFileInputStream);

        List<Variant> list = new ArrayList<>();
        List<LDVariant> ldList = new ArrayList<>();
        Pair<Double, Double> ld;
        while (pos != GlobalParameter.GT_FILE_END) {
            variant = new Variant();
            variant.setPos(pos);
            variant.setVaiant(gtFileInputStream);

            if(pos >= beg) {
                if(pos == (locFeature.beg + 1)) {
                    query = variant;
                } else {
                    list.add(variant);
                }
            } else if(pos > end) {
                break;
            }
            pos = GlobalParameter.readInt(gtFileInputStream);
        }

        if(query != null) {
            for (Variant v: list) {
                ld = computeLd(query, v);
                if(ld.getKey() * ld.getKey() > cutoff) {
                    ldList.add(new LDVariant(locFeature.chr, v.getPos(), v.getRef().getBaseString(), v.getAlt().getBaseString(), ld.getKey(), ld.getValue()));
                }
            }
        }

        return ldList;
    }

    private Pair<Double, Double> computeLd(final Variant va, final Variant vb) {
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

    private Variant findVariant(final LocFeature locFeature) throws IOException {
        List<Bin> binList = index.getBinList(locFeature.chr).getList();
        for (int i = 0; i < binList.size(); i++) {
            if(binList.get(i).getMin() <= (locFeature.beg + 1) && binList.get(i).getMax() >= (locFeature.beg + 1)) {
                return findVariantInBin(binList.get(i), locFeature);
            }
        }
        return null;
    }

    private Variant findVariantInBin(final Bin bin, final LocFeature locFeature) throws IOException {
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

    class LDVariant {
        private String chr;
        private Integer pos;
        private String ref;
        private String alt;

        private double dPrime;
        private double r;

        public LDVariant(String chr, Integer pos, String ref, String alt, double dPrime, double r) {
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
            this.dPrime = dPrime;
            this.r = r;
        }
    }
}
