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
import ch.so.agi.grundstuecksinformation.shared.EgridService;
import ch.so.agi.grundstuecksinformation.shared.OerebWebService;
import ch.so.agi.grundstuecksinformation.shared.models.Egrid;

import org.slf4j.Logger;

@SuppressWarnings("serial")
public class EgridServiceImpl extends RemoteServiceServlet implements EgridService {
    Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    AppConfig config;
    
    @Autowired
    Jaxb2Marshaller marshaller;
            
    // see:
    // https://stackoverflow.com/questions/51874785/gwt-spring-boot-autowired-is-not-working
    @Override
    public void init() throws ServletException {
         super.init();
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }
    
    @Override
    public EgridResponse egridServer(String XY) throws IllegalArgumentException, IOException {
        List<OerebWebService> oerebWebServices = config.getOerebWebServices();  
        
        URL egridUrl = null;
        String oerebBaseUrl = null;
        HttpURLConnection connection = null;
        int responseCode = 204;
        for (OerebWebService ws : oerebWebServices) {
            URL url = new URL(ws.getBaseUrl() + "getegrid/xml/?XY=" + XY.replace(" ",""));
            logger.debug("Url: " + url.toString());
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");
            responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                egridUrl = url;
                oerebBaseUrl = ws.getBaseUrl();
                logger.debug("GetEgrid request successful: " + egridUrl);
                break;
            } 
        }
       
        if (egridUrl == null) {
            EgridResponse response = new EgridResponse();
            response.setResponseCode(responseCode);
            return response;            
        }
        
        File xmlFile = Files.createTempFile("egrid_", ".xml").toFile();
        
        InputStream initialStream = connection.getInputStream();
        java.nio.file.Files.copy(initialStream, xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        initialStream.close();
        logger.debug("File downloaded: " + xmlFile.getAbsolutePath());

        StreamSource xmlSource = new StreamSource(xmlFile);
        GetEGRIDResponse obj = (GetEGRIDResponse) marshaller.unmarshal(xmlSource);
        GetEGRIDResponseType egridResponseType = obj.getValue();
        List<JAXBElement<String>> egridXmlList = egridResponseType.getEgridAndNumberAndIdentDN();
        
        List<Egrid> egridList = new ArrayList<Egrid>();
        for (int i=0; i<egridXmlList.size(); i=i+3) {
            Egrid egridObj = new Egrid();
            egridObj.setEgrid(egridXmlList.get(i).getValue());
            egridObj.setNumber(egridXmlList.get(i+1).getValue());
            egridObj.setIdentDN(egridXmlList.get(i+2).getValue());
            egridObj.setOerebServiceBaseUrl(oerebBaseUrl);
            logger.debug("E-GRID: " + egridObj.getEgrid());
            egridList.add(egridObj);
        }

        EgridResponse response = new EgridResponse();
        response.setResponseCode(responseCode);
        response.setEgrid(egridList);
        return response;
    }
}
