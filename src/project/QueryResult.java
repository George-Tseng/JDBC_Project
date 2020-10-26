package project;

import java.util.List;

public class QueryResult {
    private boolean success;
    private String contents;
    private List<Wifi_Info> sqlData;

    protected void setSuccess(boolean success) {
        this.success = success;
    }

    protected void setContents(String contents) {
        this.contents = contents;
    }

    protected void setSqlData(List<Wifi_Info> sqlData) {
        this.sqlData = sqlData;
    }

    protected boolean getSuccess() {
        return success;
    }

    protected String getContents() {
        return contents;
    }

    protected List<Wifi_Info> getSqlData() {
        return sqlData;
    }
}
