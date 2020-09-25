package ch.so.agi.grundstuecksinformation.shared.models;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Egrid implements IsSerializable {
    private String egrid;
    private String number;
    private String identDN;
    private String limit;
    private String oerebServiceBaseUrl;
    
    public String getEgrid() {
        return egrid;
    }
    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getIdentDN() {
        return identDN;
    }
    public void setIdentDN(String identDN) {
        this.identDN = identDN;
    }
    public String getLimit() {
        return limit;
    }
    public void setLimit(String limit) {
        this.limit = limit;
    }
    public String getOerebServiceBaseUrl() {
        return oerebServiceBaseUrl;
    }
    public void setOerebServiceBaseUrl(String oerebServiceBaseUrl) {
        this.oerebServiceBaseUrl = oerebServiceBaseUrl;
    }
}
