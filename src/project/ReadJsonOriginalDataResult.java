package project;

import java.util.List;

public class ReadJsonOriginalDataResult {
	String result;
	String contents;
	List<Wifi_Info> wifiDataList;
	
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
	protected List<Wifi_Info> getWifiDataList() {
		return wifiDataList;
	}
	protected void setWifiDataList(List<Wifi_Info> wifiDataList) {
		this.wifiDataList = wifiDataList;
	}
}