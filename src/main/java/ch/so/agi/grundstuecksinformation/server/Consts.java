package ch.so.agi.grundstuecksinformation.server;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Consts {
    
    // TODO: Inject from application.properties
    // Cannot be static anymore. 
    public static List<String> OEREB_SERVICE_BASE_URL = Stream.of(
//            "https://oereb.geo.sh.ch/oereb/", 
//            "https://maps.zh.ch/oereb/",
//            "https://map.geo.gl.ch/oereb/wsgi/oereb/",
//            "https://map.geo.sz.ch/oereb/wsgi/oereb/",
//            "https://api.geo.ag.ch/v1/oereb/",
            "https://geo.so.ch/api/oereb/")
            .collect(Collectors.toList());
    
    public static List<String> CADASTRE_SERVICE_BASE_URL = Stream.of(
//          "https://oereb.geo.sh.ch/oereb/", 
//          "https://maps.zh.ch/oereb/",
//          "https://map.geo.gl.ch/oereb/wsgi/oereb/",
//          "https://map.geo.sz.ch/oereb/wsgi/oereb/",
//          "https://api.geo.ag.ch/v1/oereb/",
          "http://localhost:8282/")
          .collect(Collectors.toList());    
}
