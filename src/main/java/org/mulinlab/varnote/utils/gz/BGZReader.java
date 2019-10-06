package org.mulinlab.varnote.utils.gz;


public final class BGZReader {

	private static final String NATIVE_LIBRARY_NAME = "gz_reader";
    private static boolean initialized = false;
    private static boolean isEnd = false;
    private int index;
    
    public BGZReader() {
    		super();
    		index = 0;
    }
    
    public BGZReader(final String bgzFile, final int index) {
		super();
		this.index = index;
		init(bgzFile);
	}

	public synchronized boolean init(final String bgzFile) {
        if (!initialized) {
        		System.loadLibrary(NATIVE_LIBRARY_NAME);
        		System.out.println("111");
            initialized = true;
        }
        System.out.println("222");
        initNative(bgzFile, index);
        return true;
    }
    
	private native void initNative(final String oriFile, final int index);
    private native String readLineNative(final long filePointer, final int index);
    private native void closeFPNative(final int index);
    private native void endNative();
    
    public String  readLine(final long filePointer) {
//    		synchronized
    	
    		return readLineNative(filePointer, index);
	}
    
    public void closeFP() {
    		System.out.println("close:" + index);
    		closeFPNative(index);
    }
    
    public void close() {
    	 	if (!isEnd) {
    	 		endNative();
    	 		isEnd = true;
    	 	}
    }		
    
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		String javaLibPath = System.getProperty("java.library.path");
		System.out.println(javaLibPath);
		
		BGZReader reader = new BGZReader("/Users/hdd/Downloads/dandan/data/AF.ANN.bgz", 0); 
		BGZReader reader2 = new BGZReader("/Users/hdd/Downloads/dandan/data/AF.ANN.bgz", 1); 
		System.out.println("1111=" + reader.readLine(0));
		System.out.println("222=" + reader2.readLine(0));
		System.out.println("333=" + reader.readLine(0));
		
		reader.endNative();
		
		 long t2 = System.currentTimeMillis();
		 System.out.println("Time:" + (t2 - t1));
	}

	public int getIndex() {
		return index;
	}
	
	
}
