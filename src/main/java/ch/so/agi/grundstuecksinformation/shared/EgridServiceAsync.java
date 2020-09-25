package ch.so.agi.grundstuecksinformation.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EgridServiceAsync {
    void egridServer(String XY, AsyncCallback<EgridResponse> callback)
            throws IllegalArgumentException;
}
