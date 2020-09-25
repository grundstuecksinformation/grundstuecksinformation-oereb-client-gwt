package ch.so.agi.grundstuecksinformation.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.so.agi.grundstuecksinformation.shared.models.Egrid;

public class EgridResponse implements IsSerializable {
    private List<Egrid> egrid;
    
    private int responseCode;

    public List<Egrid> getEgrid() {
        return egrid;
    }

    public void setEgrid(List<Egrid> egrid) {
        this.egrid = egrid;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
