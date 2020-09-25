package ch.so.agi.grundstuecksinformation.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.so.agi.grundstuecksinformation.shared.models.Egrid;

public interface ExtractServiceAsync {
    void extractServer(Egrid egrid, AsyncCallback<ExtractResponse> callback)
            throws IllegalArgumentException;
}
