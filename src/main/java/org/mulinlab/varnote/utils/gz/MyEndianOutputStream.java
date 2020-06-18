package org.mulinlab.varnote.utils.gz;

import java.io.FilterOutputStream;
import java.io.IOException;

public final class MyEndianOutputStream  extends FilterOutputStream {

	private final MyBlockCompressedOutputStream out;
    public MyEndianOutputStream(MyBlockCompressedOutputStream out) {
        super(out);
        this.out = out; 
    }

    public MyBlockCompressedOutputStream getOut() {
		return out; 
	}


	public void write(int b) throws IOException {
        out.write(b);
    }

    public void writeBoolean(boolean b) throws IOException {
        if (b) this.write(1);
        else this.write(0);
    }

    public void writeByte(int b) throws IOException {
        out.write(b);
    }

    public void writeShort(int s) throws IOException {
        out.write(s & 0xFF);
        out.write((s >>> 8) & 0xFF);
    }

    public void writeChar(int c) throws IOException {
        out.write(c & 0xFF);
        out.write((c >>> 8) & 0xFF);
    }

    public void writeInt(int i) throws IOException {
        out.write(i & 0xFF);
        out.write((i >>> 8) & 0xFF);
        out.write((i >>> 16) & 0xFF);
        out.write((i >>> 24) & 0xFF);
    }

    public void writeLong(long l) throws IOException {
        out.write((int) l & 0xFF);
        out.write((int) (l >>> 8) & 0xFF);
        out.write((int) (l >>> 16) & 0xFF);
        out.write((int) (l >>> 24) & 0xFF);
        out.write((int) (l >>> 32) & 0xFF);
        out.write((int) (l >>> 40) & 0xFF);
        out.write((int) (l >>> 48) & 0xFF);
        out.write((int) (l >>> 56) & 0xFF);
    }

    public final void writeFloat(float f) throws IOException {
        this.writeInt(Float.floatToIntBits(f));
    }

    public final void writeDouble(double d) throws IOException {
        this.writeLong(Double.doubleToLongBits(d));
    }

    public void writeBytes(String s) throws IOException {
        int length = s.length();
        for (int i = 0; i < length; i++) {
            out.write((byte) s.charAt(i));
        }
    }
    
    public void close(long address) throws IOException {
        out.flush();
        out.close(address);
    }
}
