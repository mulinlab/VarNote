package test;
import java.io.FileInputStream;
import java.io.IOException;

import constants.BasicUtils;
import bean.node.Node;
import bean.node.NodeFactory;
import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.readers.AsciiLineReaderIterator;
import htsjdk.tribble.readers.PositionalBufferedStream;
import htsjdk.variant.vcf.VCFCodec;


public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		 VannoSearch vs = new VannoSearch();
//		 String s = "G|0.138379|0.3971|0.0562|";
//		 String[] token = s.split("\\|");
//		 System.out.println(token[1]);
		
		
//		VCFCodec codec = new VCFCodec();
//		
//		String file = "/Users/mulin/Desktop/linlin_trio_var_phased.vcf";
//		final AsciiLineReaderIterator source = (AsciiLineReaderIterator) codec.makeIndexableSourceFromStream(new PositionalBufferedStream(new FileInputStream(file)));
//		
//
//        final FeatureCodecHeader header = codec.readHeader(source);
//        codec.close(source);
        System.err.println("111");
		
		String pattern = "\\$\\d+\\.\\.\\$n";
		System.out.println("$23..$n".matches(pattern));
		
		
		System.out.println(1<<14);
		System.out.println(16384>>14);
		
		
		System.out.println(1 << 29);
		
		String s = "1	aaaa";
		System.out.println(s.substring(s.indexOf("\t") + 1));
		
		
        Node it = NodeFactory.createBasic("1	749683	.	C	T	349.22", TabixFormat.VCF);
        System.out.println(it.beg + ", " + it.end);
        
        it = NodeFactory.createBasic("1	749683	.	CAA	T	349.22", TabixFormat.VCF);
        System.out.println(it.beg + ", " + it.end);
        
        it = NodeFactory.createBasic("1	749683	.	C	T,TA,TTA	349.22", TabixFormat.VCF);
        System.out.println(it.beg + ", " + it.end);
        
       System.out.println("chrsss".substring(3));
        
        
        it = NodeFactory.createBasic("1	749683	749684	.	C	T	349.22", TabixFormat.BED);
        System.out.println(it.beg + ", " + it.end);
        
        it = NodeFactory.createBasic("1	69510	69511	A	CG,G,T	", TabixFormat.BED);
        System.out.println(it.beg + ", " + it.end);
        
	}

}
