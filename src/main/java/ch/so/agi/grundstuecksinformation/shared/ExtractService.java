package ch.so.agi.grundstuecksinformation.shared;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import ch.so.agi.grundstuecksinformation.shared.models.Egrid;

@RemoteServiceRelativePath("extract")
public interface ExtractService extends RemoteService {
    ExtractResponse extractServer(Egrid egrid) throws IllegalArgumentException, IOException;
}
