package org.mulinlab.varnote.operations.index;

import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class BinList {

    private List<Bin> list;
    private int bcount;

    public BinList() {
        this.list = new ArrayList<>();
        this.bcount = 0;
    }

    public void addVariant(final Variant variant, final MyEndianOutputStream gtOS) {
        if(bcount % GlobalParameter.GT_BIN_INTERVAL == 0) {
            bcount = 0;
            list.add(new Bin(variant.getPos(), gtOS.getOut().getFilePointer()));
        } else {
            list.get(list.size() - 1).addVariant(variant);
        }
        bcount++;
    }

    public void addBin(final Bin bin) {
        list.add(bin);
    }
    public List<Bin> getList() {
        return list;
    }
}
