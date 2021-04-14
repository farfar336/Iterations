// LocalThreadVariable.java

package interation2;
import java.util.ArrayList;

/* This class will store the local variable that we are going to use for threads

Key functions:
-LocalThreadVariable: Initializes the thread
-getTimestamp: Gets the time stamp of the thread
-getWorkingSnippet: Gets the working snippet of the thread 
-getCurrentList: Gets the currentlist of the thread 

*/
public class LocalThreadVariable {
	private int timestamp;
	private String workingSnippet;
	private ArrayList<String> currentList;
	
	// Initializes the thread
	public LocalThreadVariable(int i, String s, ArrayList<String> list) {
		this.timestamp = i;
		workingSnippet = s;
		currentList = new ArrayList<String>(list);
	}

	// Gets the time stamp of the thread
	public int getTimestamp() {
		return this.timestamp;
	}

	// Gets the working snippet of the thread 
	public String getWorkingSnippet() {
		return this.workingSnippet;
	}

	// Gets the currentlist of the thread 
	public ArrayList<String> getCurrentList(){
		return this.currentList;
	}	
}
