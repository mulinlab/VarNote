package test;

import java.io.IOException;
import java.util.List;

import process.ProcessResult;
import stack.AbstractReaderStack;
import bean.node.Node;

public class CopyOfExactStack extends AbstractReaderStack {
	private Node currentDB;

	public CopyOfExactStack(ProcessResult resultProcessor) {
		super(resultProcessor);
		currentDB = null;

	}
	
	public void clearST() {
		currentDB = null;
	}
	
	
	public void findOverlaps(List<Node> nodes) {
		if(it == null) return;
//		if(iseof) return;
		try {
			int size = nodes.size();
			int count = 0;
			Node query, db = it.nextNode();

			while((count < size) && (db != null)) {
				query = nodes.get(count);
				
//				System.out.println("query: [" + query.beg + "," + query.end + "] " + "db: [" + db.beg + "," + db.end + "] ");
//				if(!query.chr.equals(db.chr)) {
//					iseof = true;
//					break;
//				}
				
				if(query.beg == db.beg) {
					if(query.end == db.end) {
						resultProcessor.doProcess(query, db);
						//hit
						
						db = it.nextNode();
					} else if(query.end < db.end) {
						//query++
						count++;
					} else {   //query.end > db.end
						db = it.nextNode();
					}
					
				} else if(query.beg < db.beg) {
					count++;
				} else {
					db = it.nextNode();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void findOverlap(Node query){
//		if(iseof) return;
		if(it == null) return;
		try {	
			if(currentDB == null) {
				currentDB = it.nextNode();
			}

			while(currentDB != null) {
				
//				if(!query.chr.equals(currentDB.chr)) {
//					iseof = true;
//					break;
//				}
				if(query.beg == currentDB.beg) {
					if(query.end == currentDB.end) {
						resultProcessor.doProcess(query, currentDB);
						//hit
						
						currentDB = it.nextNode();
					} else if(query.end < currentDB.end) {
						//query++
						return;
					} else {   //query.end > db.end
						currentDB = it.nextNode();
					}
					
				} else if(query.beg < currentDB.beg) {
					return;
				} else {
					currentDB = it.nextNode();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
