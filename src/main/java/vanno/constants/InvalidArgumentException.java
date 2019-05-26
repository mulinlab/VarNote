package main.java.vanno.constants;

public class InvalidArgumentException extends RuntimeException {
	private static final long serialVersionUID = -3572060677576386495L;
	public InvalidArgumentException(String s) {
//	    super(s);
		System.err.println("\n" + BasicUtils.KBLU + "InvalidArgumentException: " + BasicUtils.KRED + s + BasicUtils.KNRM + "\n");
		System.exit(1);
	}

    public InvalidArgumentException() {
    }
    
    public InvalidArgumentException(Throwable cause) {
        super(cause);
    }
}
