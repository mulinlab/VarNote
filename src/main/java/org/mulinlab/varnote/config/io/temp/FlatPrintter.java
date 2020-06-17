package org.mulinlab.varnote.config.io.temp;


import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.config.param.output.OutParam;
import java.io.*;

public final class FlatPrintter extends Printter {

    public FlatPrintter(OutParam outParam) throws FileNotFoundException {
        super(outParam);
    }


    @Override
    public void addPrintter(String path, Integer index) throws FileNotFoundException {
        threadPrintters.add(new FlatThreadPrintter(path, index));
    }

}
