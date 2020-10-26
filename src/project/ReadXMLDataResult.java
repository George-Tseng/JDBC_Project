package project;

import java.util.List;

public class ReadXMLDataResult {
	String result;
	String contents;
	List<String> encodeMessage;
	
	protected String getResult() {
		return result;
	}
	protected void setResult(String result) {
		this.result = result;
	}
	protected String getContents() {
		return contents;
	}
	protected void setContents(String contents) {
		this.contents = contents;
	}
	protected List<String> getEncodeMessage() {
		return encodeMessage;
	}
	protected void setEncodeMessage(List<String> encodeMessage) {
		this.encodeMessage = encodeMessage;
	}
}