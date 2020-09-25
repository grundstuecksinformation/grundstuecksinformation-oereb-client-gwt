package ch.so.agi.grundstuecksinformation.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ch.ehi.oereb.schemas.oereb._1_0.extract.GetEGRIDResponse;
import ch.ehi.oereb.schemas.oereb._1_0.extract.GetEGRIDResponseType;
import ch.so.agi.grundstuecksinformation.shared.EgridResponse;
import ch.so.agi.grundstuecksinformation.shared.ExtractResponse;
import ch.so.agi.grundstuecksinformation.shared.ExtractService;
import ch.so.agi.grundstuecksinformation.shared.models.Egrid;
import ch.so.agi.grundstuecksinformation.shared.models.RealEstateDPR;

import org.slf4j.Logger;

@SuppressWarnings("serial")
public class ExtractServiceImpl extends RemoteServiceServlet implements ExtractService {
    Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    OerebExtractService oerebExtractService;

    @Override
    public void init() throws ServletException {
         super.init();
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }

    @Override
    public ExtractResponse extractServer(Egrid egrid) throws IllegalArgumentException, IOException {
        logger.info("Oereb extract request for: " + egrid.getEgrid());
        
        RealEstateDPR realEstateDPR = new RealEstateDPR();
        oerebExtractService.getExtract(egrid, realEstateDPR);
        
        ExtractResponse extractResponse = new ExtractResponse();
        extractResponse.setRealEstateDPR(realEstateDPR);
        return extractResponse;
    }  
}
