package constants;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class IOutils {

//	private static final int MAX_FILE = 30;
	
	public static List<String> readFileWithPattern(File path) {
		PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + path.getAbsolutePath());
		List<String> result = new ArrayList<String>();
		
		String p = path.getAbsolutePath();
		int index = p.lastIndexOf(File.separator);
		if(index == -1) {
			throw new IllegalArgumentException("Cannot parse path:" + p + ", please use like this : /f/example/*.bgz ");
		}
		File dir = new File(p.substring(0, index));  
	    File[] files = dir.listFiles();
	    for (File file : files) {
			if(pathMatcher.matches(file.toPath())) {
				result.add(file.getAbsolutePath());
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
		readFileWithPattern(new File("/Users/mulin/Desktop/*.bgz"));
		System.out.println(hasPattern("/Users/mulin/Desktop/AF.ANN.bgz"));

	      // Create a Pattern object
//	      Pattern r = Pattern.compile("A.\\*.gz");
//	
//	      System.out.println(r.matcher("Afa.gz").find());
//	      System.out.println(r.matcher("ba.gz").matches());
	}
}
