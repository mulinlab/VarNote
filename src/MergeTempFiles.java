import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import constants.BasicUtils;
import process.ProcessResult;
import readers.AbstractReader.Iterator;
import stack.MergeStack;
import bean.config.run.DBRunConfigBean;
import bean.config.run.OverlapRunBean;
import bean.config.run.OverlapRunBean.OutMode;
import bean.node.Node;
import bean.node.NodeFactory;

public class MergeTempFiles implements ProcessResult{
	private final OverlapRunBean config;
	private final BufferedWriter write;
	private final int index;
	private List<Node> results;
	
	public MergeTempFiles(final OverlapRunBean config, final BufferedWriter write, final int index) {
		super();
		this.config = config;
		this.write = write;
		this.index = index;
		this.results = new ArrayList<Node>();
	}
	

	
	public void doProcess() {
		try {
			final BufferedReader qReader = new BufferedReader(new InputStreamReader(new FileInputStream(config.getTempFileName(index))));
			
			List<MergeStack> stacks = new ArrayList<MergeStack>();
			MergeStack stack;
			
			for (DBRunConfigBean db : config.getDbs()) {
				stack = new MergeStack(this);
				stack.setIterator(new MergeImpl(db.getPrintter(index).getFile(), db.getLable()));
				stacks.add(stack);
			}
			
			String line;
			Node qNode;
			while((line = qReader.readLine()) != null) {
				if(!BasicUtils.isHeader(line)) {
					qNode = NodeFactory.createNodeIndex(line, null);

					if(results.size() > 0) results.clear();
					for (MergeStack mergeStack : stacks) {
						mergeStack.findOverlap(qNode);
					}

//					System.out.println("size=" + results.size());
					
					if(config.isLoj()) {
						write.write("#query\t" + qNode.getOrigStr() + "\n"); //.substring(0,30)
					} else {
						if(results.size() > 0 && config.getOutMode() != OutMode.DB) {
							write.write("#query\t" + qNode.getOrigStr()  + "\n"); //.substring(0,30)
						}
					}
					
					if(results.size() > 0 && config.getOutMode() != OutMode.QUERY) {	
						for (Node node : results) {
							write.write( node.dbID + "\t" + node.getOrigStr()  + "\n"); //
						}
					}
				} else {
					write.write(line + "\n");
				}
			}
			
			qReader.close();
		
			for (MergeStack postStack : stacks) {
				MergeImpl it = (MergeImpl)postStack.getIt();
				it.closeReader();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doProcess(Node q, Node d) {
		results.add(d);
	}
	
	public static void main(String[] args) {
		
	}
	
	private class MergeImpl implements Iterator {
		private final BufferedReader reader;
		private boolean iseof;
		private final String fileName;
		private MergeImpl(final File path, final String _filename) throws FileNotFoundException {
	    	iseof = false;
	    	reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
	    	fileName = _filename;
	    }
	    		 
		@Override
		public String next() throws IOException {
			return reader.readLine();
		}
		
		@Override
		public Node nextNode() throws IOException {
			if (iseof) return null;
			
			String s = this.next();	
			if(s == null) {
				iseof = true;
				return null;
			} else {
				return NodeFactory.createNodeIndex(s, fileName);
			}
		}
		
		public void closeReader() throws IOException {
			reader.close();
		}
	}

}
