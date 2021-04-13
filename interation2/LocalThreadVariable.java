package interation2;

import java.util.ArrayList;
/* This class will store the local variable that we are going to use for threads */

public class LocalThreadVariable {
	private int timestamp;
	private String workingSnippet;
	private ArrayList<String> currentList;
	
	public LocalThreadVariable(int i, String s, ArrayList<String> list) {
		this.timestamp=i;
		workingSnippet=s;
		currentList=new ArrayList<String>(list);
	}
	public int getTimestamp() {
		return this.timestamp;
	}
	public String getWorkingSnippet() {
		return this.workingSnippet;
	}
	public ArrayList<String> getCurrentList(){
		return this.currentList;
	}
	
}
