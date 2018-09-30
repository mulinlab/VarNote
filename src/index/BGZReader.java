package index;


public class BGZReader {

	private static final String NATIVE_LIBRARY_NAME = "gz_reader";
    private static boolean initialized = false;
    
    public BGZReader() {
		super();
		init();
	}

	public synchronized boolean init() {
        if (!initialized) {
        		System.loadLibrary(NATIVE_LIBRARY_NAME);
            initialized = true;
        }
        return true;
    }
    

    private native void readFileNative(final String oriFile, final String tempFile, final String resultFile);

    public void readFile(final String oriFile, final String tempFile, final String resultFile) {
    		readFileNative(oriFile, tempFile, resultFile);
	}
    
	public static void main(String[] args) {
		
		String javaLibPath = System.getProperty("java.library.path");
		System.out.println(javaLibPath);
		
		long t1 = System.currentTimeMillis();
		BGZReader reader = new BGZReader();  
		reader.readFile("/Users/hdd/Downloads/dandan/data/AF.ANN.bgz", "/Users/hdd/Downloads/dandan/data/linlin2.vcf_MIX_out/AF.ANN.bgz.bgz", 
			 "/Users/hdd/Downloads/dandan/data/linlin2.vcf_MIX_out/AF.ANN.bgz.result");
		 long t2 = System.currentTimeMillis();
		 System.out.println("Time:" + (t2 - t1));
	}
}
