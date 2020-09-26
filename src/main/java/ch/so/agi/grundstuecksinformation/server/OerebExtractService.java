package ch.so.agi.grundstuecksinformation.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ch.ehi.oereb.schemas.oereb._1_0.extract.GetExtractByIdResponse;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.DocumentBaseType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.DocumentType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.ExtractType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LanguageCodeType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LocalisedMTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LocalisedTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LocalisedUriType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.MultilingualMTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.MultilingualTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.MultilingualUriType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.RealEstateDPRType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.RestrictionOnLandownershipType;
import ch.so.agi.grundstuecksinformation.shared.EgridResponse;
import ch.so.agi.grundstuecksinformation.shared.OerebWebService;
import ch.so.agi.grundstuecksinformation.shared.models.AbstractTheme;
import ch.so.agi.grundstuecksinformation.shared.models.ConcernedTheme;
import ch.so.agi.grundstuecksinformation.shared.models.Document;
import ch.so.agi.grundstuecksinformation.shared.models.Egrid;
import ch.so.agi.grundstuecksinformation.shared.models.NotConcernedTheme;
import ch.so.agi.grundstuecksinformation.shared.models.Office;
import ch.so.agi.grundstuecksinformation.shared.models.RealEstateDPR;
import ch.so.agi.grundstuecksinformation.shared.models.ReferenceWMS;
import ch.so.agi.grundstuecksinformation.shared.models.Restriction;
import ch.so.agi.grundstuecksinformation.shared.models.ThemeWithoutData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xerces.impl.dv.util.Base64;

import javax.xml.transform.stream.StreamSource;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OerebExtractService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AppConfig config;

    @Autowired
    Jaxb2Marshaller marshaller;

    private static final LanguageCodeType DE = LanguageCodeType.DE;

    // Sortierung Kanton Solothurn
    private List<String> themesOrderingList = Stream.of(
            // "LandUsePlans",
            "ch.SO.NutzungsplanungGrundnutzung", "ch.SO.NutzungsplanungUeberlagernd",
            "ch.SO.NutzungsplanungSondernutzungsplaene", "ch.SO.Baulinien", "MotorwaysProjectPlaningZones",
            "MotorwaysBuildingLines", "RailwaysProjectPlanningZones", "RailwaysBuildingLines",
            "AirportsProjectPlanningZones", "AirportsBuildingLines", "AirportsSecurityZonePlans", "ContaminatedSites",
            "ContaminatedMilitarySites", "ContaminatedCivilAviationSites", "ContaminatedPublicTransportSites",
            "GroundwaterProtectionZones", "GroundwaterProtectionSites", "NoiseSensitivityLevels", "ForestPerimeters",
            "ForestDistanceLines", "ch.SO.Einzelschutz")
            .collect(Collectors.toList());

    Map<String, String> realEstateTypesMap = Stream.of(new String[][] {
        { "Distinct_and_permanent_rights.BuildingRight", "Baurecht" }, 
        { "RealEstate", "Liegenschaft" }, 
    })
    .collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public RealEstateDPR getExtract(Egrid egrid, RealEstateDPR realEstateDPR) throws IOException { 
        List<OerebWebService> oerebWebServices = config.getOerebWebServices();  
        
        HttpURLConnection connection = null;
        int responseCode = 204;
        
        URL url = new URL(egrid.getOerebServiceBaseUrl() + "extract/reduced/xml/geometry/" + egrid.getEgrid());
        logger.debug("Url: " + url.toString());

        try {
            connection = (HttpURLConnection) url.openConnection();
            // TODO: how to handle read timeouts?
            // Read timeout darf ja nicht zu klein sein, weil das
            // Herstellen des PDF eine Weile dauern kann.
            connection.setConnectTimeout(4000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");
            responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                logger.debug("Extract request successful: " + url.toString());
            }   
        } catch (Exception e) {
             logger.error(e.getMessage());
        }
        
        if (responseCode == 204) {
            throw new IllegalStateException("No extract found for egrid: " + egrid.getEgrid());
        }

        File xmlFile = Files.createTempFile("oereb_extract_", ".xml").toFile();
        InputStream initialStream = connection.getInputStream();
        java.nio.file.Files.copy(initialStream, xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        initialStream.close();
        logger.debug("File downloaded: " + xmlFile.getAbsolutePath());

        StreamSource xmlSource = new StreamSource(xmlFile);
        GetExtractByIdResponse obj = (GetExtractByIdResponse) marshaller.unmarshal(xmlSource);
        ExtractType xmlExtract = obj.getValue().getExtract().getValue();
        logger.info("Extract-Id: " + xmlExtract.getExtractIdentifier());
        realEstateDPR.setOerebExtractIdentifier(xmlExtract.getExtractIdentifier());
        
        ArrayList<ThemeWithoutData> themesWithoutData = xmlExtract.getThemeWithoutData()
                .stream()
                .map(theme -> {
                    ThemeWithoutData themeWithoutData = new ThemeWithoutData();
                    themeWithoutData.setCode(theme.getCode());
                    themeWithoutData.setName(theme.getText().getText());
                    return themeWithoutData;
                })
                .collect(collectingAndThen(toList(), ArrayList<ThemeWithoutData>::new));
         //TODO: Sortierung nur für Kanton SO:
        themesWithoutData.sort(compare);
        
        logger.debug("===========Not concerned themes===========");
        ArrayList<NotConcernedTheme> notConcernedThemes = xmlExtract.getNotConcernedTheme()
                .stream()
                .map(theme -> {
                    NotConcernedTheme notConcernedTheme = new NotConcernedTheme();
                    notConcernedTheme.setCode(theme.getCode());
                    notConcernedTheme.setName(theme.getText().getText());
                    
                    logger.debug("-------");
                    logger.debug(theme.getCode());
                    logger.debug(theme.getText().getText());
                    
                    return notConcernedTheme;
                })
                .collect(collectingAndThen(toList(), ArrayList<NotConcernedTheme>::new));
        // TODO: Sortierung nur für Kanton SO.
        notConcernedThemes.sort(compare);
        logger.debug("===========End of Not concerned themes===========");
 
        // Grundidee: Es gibt ein ConcerncedTheme-Objekt pro Thema mit allen ÖREBs zu diesem Thema. 
        // Diese ConcernedThemes werden in einer Liste gespeichert. Dies entspricht
        // dem späteren Handling im GUI.
        logger.debug("===========Concerned themes===========");
        
        /*
         * Solothurn:
         * Theme.Code:           LandUsePlans                               LandUsePlans
         * Theme.Text.Text:      Nutzungsplanung Grundnutzung               Baulinien (kantonal/kommunal)
         * Subtheme:             ch.SO.NutzungsplanungGrundnutzung          ch.SO.Baulinien
         * 
         * Glarus:
         * Theme.Code:           LandUsePlans                               LandUsePlans
         * Theme.Text.Text:      Nutzungsplanung                            Nutzungsplanung
         * Subtheme:             Grundnutzung                               Linienbezogene Festlegungen
         * 
         * Aargau:
         * Theme.Code:           LandUsePlans                              
         * Theme.Text.Text:      Nutzungsplanung (kantonal/kommunal)       
         * Subtheme:             73B_73C_ARE_DNPGrundnutzung               
         */
        
        /*
         * Weil Kantone Subthemen völlig anders behandeln und verwenden, muss dem auch beim Verarbeiten
         * und Darstellen im Client Rechnung getragen werden.
         * Auf Serverseite muss die Kombination Theme.Text.Text + Subtheme gruppiert werden.
         * Im Client für das Beschriften der Handorgeln wird geprüft, ob - falls vorhanden - das Subthema
         * sprechend ist (Kanton Solothurn) oder ob es sich um den technischen Namen handelt (Kanton GL).
         */
        Map<ThemeTuple, List<RestrictionOnLandownershipType>> groupedXmlRestrictions = xmlExtract.getRealEstate().getRestrictionOnLandownership()
                .stream()
                .collect(Collectors.groupingBy(r -> new ThemeTuple(r.getTheme().getText().getText(), r.getSubTheme())));
        logger.debug("groupedXmlRestrictions (tuple): " + groupedXmlRestrictions.toString());

        ArrayList<ConcernedTheme> concernedThemesList = new ArrayList<ConcernedTheme>();
        for (Map.Entry<ThemeTuple, List<RestrictionOnLandownershipType>> entry : groupedXmlRestrictions.entrySet()) {
            logger.debug("---------------------------------------------");
            logger.debug("ConcernedTheme: " + entry.getKey().toString());

            List<RestrictionOnLandownershipType> xmlRestrictions = entry.getValue();
            logger.debug("Anzahl einzelne OEREB-Objekte im XML für dieses Thema: " + String.valueOf(xmlRestrictions.size()));

            // Es wird eine Map erzeugt mit einem vereinfachten Restriction-Objekt
            // resp. OEREB-Objekt pro Artcode.
            // 'groupingBy' kann nicht verwendet werden, weil das eine Liste pro Artcode
            // zurückliefert.
            // Später werden dem vereinfachten Restriction-Objekt mehr Infos hinzugefügt.
            // TODO: Neu wird mit TypeCode und TypeCodeListe gruppiert. Das sollte jetzt
            // hinkommen (Egerkingen GB-Nr. 1293).            
            Map<TypeTuple, Restriction> restrictionsMap = xmlRestrictions
                    .stream()
                    .filter(distinctByKey(r -> {
                        return new TypeTuple(r.getTypeCode(), r.getTypeCodelist());
                    }))
                    .map(r -> {
                        Restriction restriction = new Restriction();
                        restriction.setInformation(getLocalisedText(r.getInformation(), DE));
                        restriction.setTypeCode(r.getTypeCode());
                        restriction.setTypeCodeList(r.getTypeCodelist());
                        if (r.getSymbol() != null) {
                            String encodedImage = Base64.encode(r.getSymbol());
                            encodedImage = "data:image/png;base64," + encodedImage;
                            restriction.setSymbol(encodedImage);
                        } else if (r.getSymbolRef() != null) {
                            try {
                                String symbolUrl = URLDecoder.decode(r.getSymbolRef(), StandardCharsets.UTF_8.toString());
                                restriction.setSymbolRef(symbolUrl);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        return restriction;
                    })
                    .collect(Collectors.toMap(r -> {
                        return new TypeTuple(r.getTypeCode(), r.getTypeCodeList());
                    }, Function.identity()));
            logger.debug("*********: " + restrictionsMap.toString());
            
            // Die Summe der sogenannten Shares (Fläche(prozent)/Länge/Anzahl Punkte) pro
            // Typecode/TypcodeList-Tupel.
            Map<TypeTuple, Integer> sumAreaShare = xmlRestrictions
                    .stream()
                    .filter(r -> r.getAreaShare() != null)
                    .collect(Collectors.groupingBy(r -> {return new TypeTuple(r.getTypeCode(), r.getTypeCodelist());}, Collectors.summingInt(r -> r.getAreaShare())));

            Map<TypeTuple, Integer> sumLengthShare = xmlRestrictions
                    .stream()
                    .filter(r -> r.getLengthShare() != null)
                    .collect(Collectors.groupingBy(r -> {return new TypeTuple(r.getTypeCode(), r.getTypeCodelist());}, Collectors.summingInt(r -> r.getLengthShare())));

            Map<TypeTuple, Integer> sumNrOfPoints = xmlRestrictions
                    .stream()
                    .filter(r -> r.getNrOfPoints() != null)
                    .collect(Collectors.groupingBy(r -> {return new TypeTuple(r.getTypeCode(), r.getTypeCodelist());}, Collectors.summingInt(r -> r.getNrOfPoints())));

            Map<TypeTuple, Double> sumAreaPercentShare = xmlRestrictions
                    .stream()
                    .filter(r -> r.getPartInPercent() != null)
                    .collect(Collectors.groupingBy(r -> {return new TypeTuple(r.getTypeCode(), r.getTypeCodelist());}, Collectors.summingDouble(r -> r.getPartInPercent().doubleValue())));

            logger.debug("sumAreaShare: " + sumAreaShare.toString());
            logger.debug("sumLengthShare: " + sumLengthShare.toString());
            logger.debug("sumNrOfPoints: " + sumNrOfPoints.toString());
            logger.debug("sumAreaPercentShare: " + sumAreaPercentShare.toString());

            // Die vorher berechnete Summe wird dem jeweiligen vereinfachten
            // OEREB-Objekt zugewiesen. Dieses wird in einer Liste
            // von vereinfachten OEREB-Objekten eingefügt. Eine solche definitive Liste
            // gibt es pro ConcernedTheme.
            List<Restriction> restrictionsList = new ArrayList<Restriction>();
            for (Map.Entry<TypeTuple, Restriction> restrictionEntry : restrictionsMap.entrySet()) {
                TypeTuple typeTuple = restrictionEntry.getKey();
                if (sumAreaShare.get(typeTuple) != null) {
                    restrictionEntry.getValue().setAreaShare(sumAreaShare.get(typeTuple));
                }
                if (sumLengthShare.get(typeTuple) != null) {
                    restrictionEntry.getValue().setLengthShare(sumLengthShare.get(typeTuple));
                }
                if (sumNrOfPoints.get(typeTuple) != null) {
                    restrictionEntry.getValue().setNrOfPoints(sumNrOfPoints.get(typeTuple));
                }
                if (sumAreaPercentShare.get(typeTuple) != null) {
                    restrictionEntry.getValue().setPartInPercent(sumAreaPercentShare.get(typeTuple));
                }
                restrictionsList.add(restrictionEntry.getValue());
            }
            logger.debug("restrictionsList: " + restrictionsList);
            
            // Collect responsible offices in a office list.
            // Distinct by office url.
            ArrayList<Office> officeList = (ArrayList<Office>) xmlRestrictions.stream()
                    .filter(distinctByKey(r -> {
                        String officeName = r.getResponsibleOffice().getOfficeAtWeb().getValue();
                        return officeName;
                    }))
                    .map(r -> {
                        Office office = new Office();
                        if (r.getResponsibleOffice().getName() != null) {
                            office.setName(getLocalisedText(r.getResponsibleOffice().getName(), DE));
                        }      
                        try {
                            office.setOfficeAtWeb(URLDecoder.decode(r.getResponsibleOffice().getOfficeAtWeb().getValue(), StandardCharsets.UTF_8.toString()));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            office.setOfficeAtWeb(r.getResponsibleOffice().getOfficeAtWeb().getValue());
                        }
                        return office;
                    })
                    .collect(Collectors.toList());

            logger.debug("Size of office: " + officeList.size());

            // Get legal provisions, laws and hints. Put them in Lists. 
            // Die Gesetze und Hinweise stammen nicht aus der gleichen Hierarchie wie
            // die Rechtsgrundlagen. Sondern sind in den Rechtsgrundlagen
            // verschachtelt. Man schaue sich ein korrektes XML an.
            // Aus diesem Grund können Gesetze vielfach vorkommen und
            // müssen anschliessend distincted werden. Das gilt natürlich
            // aber auch für die Rechtsgrundlagen.
            List<Document> legalProvisionsList = new ArrayList<Document>();
            List<Document> lawsList = new ArrayList<Document>();
            List<Document> hintsList = new ArrayList<Document>();

            for (RestrictionOnLandownershipType xmlRestriction : xmlRestrictions) {
                List<DocumentBaseType> xmlLegalProvisions = xmlRestriction.getLegalProvisions()
                        .stream()
                        .filter(d -> d.getDocumentType().equalsIgnoreCase("LegalProvision"))
                        .collect(Collectors.toList());
                for (DocumentBaseType xmlDocumentBase : xmlLegalProvisions) {
                    DocumentType xmlLegalProvision = (DocumentType) xmlDocumentBase;                    
                    Document legalProvision = new Document();
                    if (xmlLegalProvision.getTitle() != null) {
                        legalProvision.setTitle(getLocalisedText(xmlLegalProvision.getTitle(), DE));
                    }
                    if (xmlLegalProvision.getOfficialTitle() != null) {
                        legalProvision.setOfficialTitle(getLocalisedText(xmlLegalProvision.getOfficialTitle(), DE));
                    }
                    legalProvision.setOfficialNumber(xmlLegalProvision.getOfficialNumber());
                    if (xmlLegalProvision.getAbbreviation() != null) {
                        legalProvision.setAbbreviation(getLocalisedText(xmlLegalProvision.getAbbreviation(), DE));
                    }
                    if (xmlLegalProvision.getTextAtWeb() != null) { 
                       try {
                           legalProvision.setTextAtWeb(URLDecoder.decode(getLocalisedText(xmlLegalProvision.getTextAtWeb(), DE), StandardCharsets.UTF_8.toString()));
                       } catch (UnsupportedEncodingException e) {
                           legalProvision.setTextAtWeb(getLocalisedText(xmlLegalProvision.getTextAtWeb(), DE));
                       }
                    }
                    legalProvisionsList.add(legalProvision);

                    List<DocumentType> xmlLaws = xmlLegalProvision.getReference()
                            .stream()
                            .filter(d -> d.getDocumentType().equalsIgnoreCase("Law"))
                            .collect(Collectors.toList());
                    for (DocumentType xmlLaw : xmlLaws) {
                        Document law = new Document();
                        if (xmlLaw.getTitle() != null) {
                            law.setTitle(getLocalisedText(xmlLaw.getTitle(), DE));
                        }
                        if (xmlLaw.getOfficialTitle() != null) {
                            law.setOfficialTitle(getLocalisedText(xmlLaw.getOfficialTitle(), DE));
                        }
                        law.setOfficialNumber(xmlLaw.getOfficialNumber());
                        if (xmlLaw.getAbbreviation() != null) {
                            law.setAbbreviation(getLocalisedText(xmlLaw.getAbbreviation(), DE));
                        }
                        if (xmlLaw.getTextAtWeb() != null) {
                            try {
                                law.setTextAtWeb(URLDecoder.decode(getLocalisedText(xmlLaw.getTextAtWeb(), DE), StandardCharsets.UTF_8.toString()));
                            } catch (UnsupportedEncodingException e) {
                                law.setTextAtWeb(getLocalisedText(xmlLaw.getTextAtWeb(), DE));
                            }
                        }
                        lawsList.add(law);
                    }
                    
                    List<DocumentType> xmlHints = xmlLegalProvision.getReference()
                            .stream()
                            .filter(d -> d.getDocumentType().equalsIgnoreCase("Hint"))
                            .collect(Collectors.toList());
                    for (DocumentType xmlHint : xmlHints) {
                        Document hint = new Document();
                        if (xmlHint.getTitle() != null) {
                            hint.setTitle(getLocalisedText(xmlHint.getTitle(), DE));
                        }
                        if (xmlHint.getOfficialTitle() != null) {
                            hint.setOfficialTitle(getLocalisedText(xmlHint.getOfficialTitle(), DE));
                        }
                        hint.setOfficialNumber(xmlHint.getOfficialNumber());
                        if (xmlHint.getAbbreviation() != null) {
                            hint.setAbbreviation(getLocalisedText(xmlHint.getAbbreviation(), DE));
                        }
                        if (xmlHint.getTextAtWeb() != null) {
                            try {
                                hint.setTextAtWeb(URLDecoder.decode(getLocalisedText(xmlHint.getTextAtWeb(), DE), StandardCharsets.UTF_8.toString()));
                            } catch (UnsupportedEncodingException e) {
                                hint.setTextAtWeb(getLocalisedText(xmlHint.getTextAtWeb(), DE));
                            }
                        }
                        hintsList.add(hint);
                    }
                }
                // FIXME: ZH liefert die Gesetze nicht als Referenz zu den Rechtsvorschriften,
                // sondern auf gleicher Ebene. Das ist m.E. falsch.
                // Hints nicht speziell für Kanton ZH behandelt.
                List<DocumentBaseType> xmlLaws = xmlRestriction.getLegalProvisions()
                        .stream()
                        .filter(d -> d.getDocumentType().equalsIgnoreCase("Law"))
                        .collect(Collectors.toList());
                for (DocumentBaseType xmlDocumentBase : xmlLaws) {
                    DocumentType xmlLaw = (DocumentType) xmlDocumentBase;                    
                    Document law = new Document();
                    if (xmlLaw.getTitle() != null) {
                        law.setTitle(getLocalisedText(xmlLaw.getTitle(), DE));
                    }
                    if (xmlLaw.getOfficialTitle() != null) {
                        law.setOfficialTitle(getLocalisedText(xmlLaw.getOfficialTitle(), DE));
                    }
                    law.setOfficialNumber(xmlLaw.getOfficialNumber());
                    if (xmlLaw.getAbbreviation() != null) {
                        law.setAbbreviation(getLocalisedText(xmlLaw.getAbbreviation(), DE));
                    }
                    if (xmlLaw.getTextAtWeb() != null) {
                        try {
                            law.setTextAtWeb(URLDecoder.decode(getLocalisedText(xmlLaw.getTextAtWeb(), DE), StandardCharsets.UTF_8.toString()));
                        } catch (UnsupportedEncodingException e) {
                            law.setTextAtWeb(getLocalisedText(xmlLaw.getTextAtWeb(), DE));
                        }
                    }
                    lawsList.add(law);
                } 
            }

            // Because restrictions can share the same legal provision and laws,
            // we need to distinct them.
            List<Document> distinctLegalProvisionsList = legalProvisionsList.stream()
                    .filter(distinctByKey(Document::getTextAtWeb)).collect(Collectors.toList());

            List<Document> distinctLawsList = lawsList.stream().filter(distinctByKey(Document::getTextAtWeb))
                    .collect(Collectors.toList());
            
            List<Document> distinctHintsList = hintsList.stream().filter(distinctByKey(Document::getTextAtWeb))
                    .collect(Collectors.toList());

            logger.debug("distinct legal provisions: " + distinctLegalProvisionsList.toString());
            logger.debug("distinct laws: " + distinctLawsList.toString());
            logger.debug("distinct hints: " + distinctHintsList.toString());
            
            // WMS: Muss auseinandergenommen werden, damit man im Client mit ol3 arbeiten kann.
            double layerOpacity = xmlRestrictions.get(0).getMap().getLayerOpacity();
            int layerIndex = xmlRestrictions.get(0).getMap().getLayerIndex();
            String wmsUrl = xmlRestrictions.get(0).getMap().getReferenceWMS();
            
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(URLDecoder.decode(wmsUrl, StandardCharsets.UTF_8.toString())).build();            
            String schema = uriComponents.getScheme();
            String host = uriComponents.getHost();
            String path = uriComponents.getPath();
            
            String layers = null;
            String imageFormat = null;
            Iterator<Map.Entry<String, List<String>>> iterator = uriComponents.getQueryParams().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> e = iterator.next();
                if (e.getKey().equalsIgnoreCase("layers")) {
                    // LAYERS string needs to be decoded as well because ol3 will encode it again.
                    layers = URLDecoder.decode(e.getValue().get(0), StandardCharsets.UTF_8.toString());
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    imageFormat = e.getValue().get(0);
                }
            }
            
            StringBuilder baseUrlBuilder = new StringBuilder();
            baseUrlBuilder.append(schema).append("://").append(host);
            if (uriComponents.getPort() != -1) {
                baseUrlBuilder.append(":" + String.valueOf(uriComponents.getPort()));
            }
            baseUrlBuilder.append(path);
            String baseUrl = baseUrlBuilder.toString();

            ReferenceWMS referenceWMS = new ReferenceWMS();
            referenceWMS.setBaseUrl(baseUrl);
            referenceWMS.setLayers(layers);
            referenceWMS.setImageFormat(imageFormat);
            referenceWMS.setLayerOpacity(layerOpacity);
            referenceWMS.setLayerIndex(layerIndex);
            logger.debug("referenceWMS: " + referenceWMS.toString()); 
            
            // Bundesthemen haben, Stand heute, keine LegendeImWeb
            String legendAtWeb = null;
            if (xmlRestrictions.get(0).getMap().getLegendAtWeb() != null) {
                legendAtWeb = URLDecoder.decode(xmlRestrictions.get(0).getMap().getLegendAtWeb().getValue(), StandardCharsets.UTF_8.toString());
            }
            
            // Finally we create the concerned theme with all information.
            ConcernedTheme concernedTheme = new ConcernedTheme();
            concernedTheme.setRestrictions(restrictionsList);
            concernedTheme.setLegalProvisions(distinctLegalProvisionsList);
            concernedTheme.setLaws(distinctLawsList);
            concernedTheme.setHints(distinctHintsList);
            concernedTheme.setReferenceWMS(referenceWMS);
            concernedTheme.setLegendAtWeb(legendAtWeb);
            concernedTheme.setCode(xmlRestrictions.get(0).getTheme().getCode());
            concernedTheme.setName(xmlRestrictions.get(0).getTheme().getText().getText());
            concernedTheme.setSubtheme(xmlRestrictions.get(0).getSubTheme());
            concernedTheme.setResponsibleOffice(officeList);

            concernedThemesList.add(concernedTheme);

            logger.debug("---------------------------------------------");
        }
        // TODO: Sorting funktioniert nur für Kanton SO.     
        concernedThemesList.sort(compare);        
        logger.debug("===========End of Concerned themes===========");
        
        RealEstateDPRType xmlRealEstateDPR = xmlExtract.getRealEstate();
        realEstateDPR.setEgrid(xmlRealEstateDPR.getEGRID());
        realEstateDPR.setFosnNr(xmlRealEstateDPR.getFosNr());
        realEstateDPR.setMunicipality(xmlRealEstateDPR.getMunicipality());
        realEstateDPR.setCanton(xmlRealEstateDPR.getCanton().value());
        realEstateDPR.setNumber(xmlRealEstateDPR.getNumber());
        realEstateDPR.setSubunitOfLandRegister(xmlRealEstateDPR.getSubunitOfLandRegister());
        realEstateDPR.setLandRegistryArea(xmlRealEstateDPR.getLandRegistryArea());
        
        try {
            realEstateDPR.setLimit(new Gml32ToJts().convertMultiSurface(xmlRealEstateDPR.getLimit()).toText()); 
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            realEstateDPR.setLimit(null);
        }
        
        realEstateDPR.setOerebThemesWithoutData(themesWithoutData);
        realEstateDPR.setOerebNotConcernedThemes(notConcernedThemes);        
        realEstateDPR.setOerebConcernedThemes(concernedThemesList);
        realEstateDPR.setRealEstateType(realEstateTypesMap.get(xmlRealEstateDPR.getType().value()));
        
        // TODO: which one is correct (according spec)?
        //realEstateDPR.setOerebPdfExtractUrl(egrid.getOerebServiceBaseUrl() + "extract/reduced/pdf/geometry/" + egrid.getEgrid());
        realEstateDPR.setOerebPdfExtractUrl(egrid.getOerebServiceBaseUrl() + "extract/reduced/pdf/" + egrid.getEgrid());
                
        Office oerebCadastreAuthority = new Office();
        oerebCadastreAuthority.setName(getLocalisedText(xmlExtract.getPLRCadastreAuthority().getName(), DE));
        oerebCadastreAuthority.setOfficeAtWeb(URLDecoder.decode(xmlExtract.getPLRCadastreAuthority().getOfficeAtWeb().getValue(), StandardCharsets.UTF_8.toString()));
        oerebCadastreAuthority.setStreet(xmlExtract.getPLRCadastreAuthority().getStreet());
        oerebCadastreAuthority.setNumber(xmlExtract.getPLRCadastreAuthority().getNumber());
        oerebCadastreAuthority.setPostalCode(xmlExtract.getPLRCadastreAuthority().getPostalCode());
        oerebCadastreAuthority.setCity(xmlExtract.getPLRCadastreAuthority().getCity());
        realEstateDPR.setOerebCadastreAuthority(oerebCadastreAuthority);

        return realEstateDPR;
    }
    
    
    Comparator<AbstractTheme> compare = new Comparator<AbstractTheme>() {
        public int compare(AbstractTheme t1, AbstractTheme t2) {
            if (t1.getSubtheme() != null && t2.getSubtheme() == null) {
                return themesOrderingList.indexOf(t1.getSubtheme()) - themesOrderingList.indexOf(t2.getCode());
            }

            if (t2.getSubtheme() != null && t1.getSubtheme() == null) {
                return themesOrderingList.indexOf(t1.getCode()) - themesOrderingList.indexOf(t2.getSubtheme());
            }

            if (t1.getSubtheme() != null && t2.getSubtheme() != null) {
                return themesOrderingList.indexOf(t1.getSubtheme()) - themesOrderingList.indexOf(t2.getSubtheme());
            }
            return themesOrderingList.indexOf(t1.getCode()) - themesOrderingList.indexOf(t2.getCode());
        }
    };
    
    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    // TODO: nicht-DE Kantone?
    // Falls DE nicht gefunden wird, das erste nehmen?
    
    private String getLocalisedText(MultilingualTextType multilingualTextType, LanguageCodeType languageCodeType) {
        Iterator<LocalisedTextType> it = multilingualTextType.getLocalisedText().iterator();
        while(it.hasNext()) {
            LocalisedTextType textType = it.next();
            if (textType.getLanguage().compareTo(languageCodeType) == 0) {
                return textType.getText();
            }
        }        
        return null;
    }
    
    private String getLocalisedText(MultilingualMTextType multilingualMTextType, LanguageCodeType languageCodeType) {
        Iterator<LocalisedMTextType> it = multilingualMTextType.getLocalisedText().iterator();
        while(it.hasNext()) {
            LocalisedMTextType textType = it.next();
            if (textType.getLanguage().compareTo(languageCodeType) == 0) {
                return textType.getText();
            }
        }
        return null;
    }    
    
    private String getLocalisedText(MultilingualUriType multilingualUriType, LanguageCodeType languageCodeType) {
        Iterator<LocalisedUriType> it = multilingualUriType.getLocalisedText().iterator();
        while(it.hasNext()) {
            LocalisedUriType textType = it.next();
            if (textType.getLanguage().compareTo(languageCodeType) == 0) {
                return textType.getText();
            }
        }
        return null;
    }    
}
