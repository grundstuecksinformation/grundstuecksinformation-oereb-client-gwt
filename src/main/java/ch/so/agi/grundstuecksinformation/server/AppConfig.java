package ch.so.agi.grundstuecksinformation.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import ch.so.agi.grundstuecksinformation.shared.OerebWebService;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private List<OerebWebService> oerebWebServices = new ArrayList<OerebWebService>();

    public List<OerebWebService> getOerebWebServices() {
        return oerebWebServices;
    }

    public void setOerebWebServices(List<OerebWebService> oerebWebServices) {
        this.oerebWebServices = oerebWebServices;
    }
}
