package org.mulinlab.varnote.exceptions;

import org.mulinlab.varnote.constants.GlobalParameter;

public class InvalidArgumentException extends RuntimeException {
	private static final long serialVersionUID = -3572060677576386495L;
	public InvalidArgumentException(String s) {
	    super(s);
//		System.err.println("\n" + GlobalParameter.KBLU + "InvalidArgumentException: " + GlobalParameter.KRED + s + GlobalParameter.KNRM + "\n");
//		System.exit(1);
	}

    public InvalidArgumentException() {
    }
    
    public InvalidArgumentException(Throwable cause) {
        super(cause);
    }
}
