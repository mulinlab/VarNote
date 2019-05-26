package main.java.vanno.constants;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public final class IOutils {

//	private static final int MAX_FILE = 30;
	
	public static List<String> readFileWithPattern(final String path) {
		String apath = new File(path).getAbsolutePath();
		PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + apath);
		List<String> result = new ArrayList<String>();

		int index = apath.lastIndexOf(File.separator);
		if(index == -1) {
			throw new InvalidArgumentException("Cannot parse path:" + apath + ", please use like this : /f/example/*.bgz ");
		}
		File dir = new File(apath.substring(0, index));  
	    File[] files = dir.listFiles();
	    
	    if(files != null)
	    for (File file : files) {
			if(pathMatcher.matches(file.toPath())) {
				result.add(file.getAbsolutePath());
//				System.out.println(file.getAbsolutePath());
			} 
		}
//	    if(result.size() > MAX_FILE) {
//	    	throw new IllegalArgumentException("Cannot handle files more than " + MAX_FILE + " at one time, please merge file first." );
//	    }
	    return result;
	}
	
	public static boolean hasPattern(String path) {
		return path.indexOf("*") != -1; 
	}
	
	public static void main(String[] args) {
		readFileWithPattern("/Users/hdd/Desktop/*.gz");
		System.out.println(hasPattern("/Users/hdd/Desktop/00-All.tsv.gz"));

	      // Create a Pattern object
//	      Pattern r = Pattern.compile("A.\\*.gz");
//	
//	      System.out.println(r.matcher("Afa.gz").find());
//	      System.out.println(r.matcher("ba.gz").matches());
	}
}
