# oereb-client-gwt

## TODO
- ~~Hintergrundkarte Schweiz... ?!~~
- cadastre-web-service entfernen (xsd, ...)
- ~~Unterschiedliches Icon für Suchresultate (siehe wgc-gwt)~~
- Error handling
- Settings auslagern
- ~~JSON-Antwort in POJO?~~ won't fix
- Eigene elemento-Elemente?
- oereb web service URL als Konfig
- console.log() bereinigen
- Fehlende GUI-Übersetzungen
- Multilingual im Backend?

## Development

First Terminal:
```
mvn clean spring-boot:run
```

Second Terminal:
```
mvn clean gwt:generate-module gwt:codeserver
```

Or simple devmode (which worked better for java.xml.bind on client side):
```
mvn gwt:generate-module gwt:devmode 
```
