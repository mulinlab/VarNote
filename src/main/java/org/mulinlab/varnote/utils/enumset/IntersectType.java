package org.mulinlab.varnote.utils.enumset;

public enum IntersectType {
    INTERSECT("Intersect", 0),
    EXACT("Exact Match", 1),
    FULLCLOASE("Full Close", 2);

    private final String name;
    private final int val;

    IntersectType(final String name, final int val) {
        this.name = name;
        this.val = val;
    }
    public String getName() {
        return name;
    }
    public int getVal() {
        return val;
    }
}
