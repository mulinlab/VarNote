package org.mulinlab.varnote.operations.index;

public final class Bin {
    private final int min;
    private int max;
    private final long address;

    public Bin(final int min, final long address) {
        this.min = min;
        this.max = min;
        this.address = address;
    }

    public Bin(final int min, final int max, final long address) {
        this.min = min;
        this.max = max;
        this.address = address;
    }

    public void addVariant(final Variant variant) {
        if(variant.getPos() > max) {
            max = variant.getPos();
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public long getAddress() {
        return address;
    }
}
