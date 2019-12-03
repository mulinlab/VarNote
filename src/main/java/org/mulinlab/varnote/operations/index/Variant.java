package org.mulinlab.varnote.operations.index;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.mulinlab.varnote.constants.GlobalParameter;

import java.io.IOException;
import java.util.BitSet;

public final class Variant {
    private int pos;
    private Allele ref;
    private Allele alt;

    private byte[] refByte;
    private byte[] altByte;
    private BitSet[] bitSets;

    public Variant() {
    }

    public Variant(final int pos, final Allele ref, final Allele alt) {
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    public void setVaiant(final BlockCompressedInputStream in) throws IOException {
        byte[] b = new byte[GlobalParameter.readInt(in)];
        in.read(b);
        for (int i = 0; i < b.length; i++) {
            if (b[i] == 0) {
                refByte = new byte[i - 0];
                altByte = new byte[b.length - i - 1];
                System.arraycopy(b, 0, refByte, 0, refByte.length);
                System.arraycopy(b, i+1, altByte, 0, altByte.length);
                break;
            }
        }

        bitSets = new BitSet[2];
        byte[] bitbytes = new byte[GlobalParameter.readShort(in)];
        in.read(bitbytes);
        bitSets[0] = BitSet.valueOf(bitbytes);

        bitbytes = new byte[GlobalParameter.readShort(in)];
        if(bitbytes.length > 0) {
            in.read(bitbytes);
        }

        bitSets[1] = BitSet.valueOf(bitbytes);
    }


    public void setCalls(byte idx, GenotypesContext gtc, int count) {
        byte[] calls = new byte[count*2];

        Genotype genotype;
        int i = 0;

        for (String sample:gtc.getSampleNamesOrderedByName()) {
            genotype = gtc.get(sample);
            calls[i] = getCallsVal(genotype.getAllele(0), idx);
            calls[i + 1] = getCallsVal(genotype.getAllele(1), idx);

            i = i + 2;
        }
        bitSets = callToBitSet(calls);
    }

    public byte getCallsVal(Allele allele, byte idx) {
        if(allele == ref) {
            return GlobalParameter.REF_IDX;
        } else if(allele == alt) {
            return idx;
        } else {
            return GlobalParameter.NO_CALL_IDX;
        }
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }
    public Allele getRef() {
        return ref;
    }
    public Allele getAlt() {
        return alt;
    }
    public BitSet[] getCalls() {
        return bitSets;
    }

    public BitSet[] callToBitSet(byte[] calls) {
        BitSet[] bitSets = new BitSet[2];

        BitSet gtbit = new BitSet(calls.length);
        BitSet nocallbit = new BitSet(calls.length);
        for (int i = 0; i < calls.length; i++) {
            if(calls[i] >= 1) {
                gtbit.set(i);
            }
        }

        for (int i = 0; i < calls.length; i++) {
            if (calls[i] == GlobalParameter.NO_CALL_IDX) {
                nocallbit.set(i);
            }
        }
        bitSets[0] = gtbit;
        bitSets[1] = nocallbit;
        return bitSets;
    }
}
