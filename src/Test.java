import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;


public class Test {

	public static void main(String[] args) {
		 OptionParser parser = new OptionParser();

	        List<String> synonyms = Arrays.asList( "message", "m", "greeting" );
	        parser.acceptsAll( synonyms ).withRequiredArg();
	        String expectedMessage = "Hello";

	        OptionSet options = parser.parse( "--message", expectedMessage );

	        for ( String each : synonyms ) {
	        	System.out.println(each + ", " + options.has( each ));
//	            assertTrue( each,  );
//	            assertTrue( each, options.hasArgument( each ) );
//	            assertEquals( each, expectedMessage, options.valueOf( each ) );
//	            assertEquals( each, asList( expectedMessage ), options.valuesOf( each ) );
	        }

	        System.out.println("abc".startsWith(String.valueOf('c')));
	}

}
