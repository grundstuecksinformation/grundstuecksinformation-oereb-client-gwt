package ch.so.agi.grundstuecksinformation.server;

import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.slf4j.Logger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import ch.ehi.oereb.schemas.gml._3_2.Interior;
//import ch.ehi.oereb.schemas.gml._3_2.LinearRing;
import ch.ehi.oereb.schemas.gml._3_2.MultiSurfacePropertyTypeType;
import ch.ehi.oereb.schemas.gml._3_2.MultiSurfaceTypeType;
//import ch.ehi.oereb.schemas.gml._3_2.Polygon;
import ch.ehi.oereb.schemas.gml._3_2.Pos;
import ch.ehi.oereb.schemas.gml._3_2.SurfaceMember;
import ch.ehi.oereb.schemas.gml._3_2.SurfacePropertyTypeType;

public class Gml32ToJts {
    Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    // TODO: make posList aware
    public Polygon convertSurface(SurfacePropertyTypeType surfacePropertyTypeType) {
        PrecisionModel precisionModel = new PrecisionModel(1000);
        GeometryFactory factory = new GeometryFactory(precisionModel);

        ch.ehi.oereb.schemas.gml._3_2.Polygon gmlPolygon = (ch.ehi.oereb.schemas.gml._3_2.Polygon) surfacePropertyTypeType.getAbstractSurface();
            
        // exterior ring
        ch.ehi.oereb.schemas.gml._3_2.LinearRing gmlLinearRing = (ch.ehi.oereb.schemas.gml._3_2.LinearRing) gmlPolygon.getValue().getExterior().getValue().getAbstractRing();
        ArrayList<Coordinate> exteriorCoordinates = new ArrayList<Coordinate>();
        for (int i=0; i<gmlLinearRing.getValue().getPosOrPointPropertyOrPointRep().size(); i++) {
            Pos pos = (Pos) gmlLinearRing.getValue().getPosOrPointPropertyOrPointRep().get(i);                
            double x = pos.getValue().getValue().get(0);
            double y = pos.getValue().getValue().get(1);
            Coordinate coordinate = new Coordinate(x, y);
            exteriorCoordinates.add(coordinate);
        }
        
        LinearRing exteriorRing = factory.createLinearRing(new CoordinateArraySequence((Coordinate[]) exteriorCoordinates.toArray(new Coordinate[0])));

        // interior ring(s)
        List<Interior> interiors = gmlPolygon.getValue().getInterior();
        ArrayList<LinearRing> interiorRings = new ArrayList<LinearRing>();
        for (Interior interior : interiors) {
            ch.ehi.oereb.schemas.gml._3_2.LinearRing gmlInteriorRing = (ch.ehi.oereb.schemas.gml._3_2.LinearRing) interior.getValue().getAbstractRing();
            ArrayList<Coordinate> interiorCoordinates = new ArrayList<Coordinate>();
            for (int i=0; i<gmlInteriorRing.getValue().getPosOrPointPropertyOrPointRep().size(); i++) {
                Pos pos = (Pos) gmlInteriorRing.getValue().getPosOrPointPropertyOrPointRep().get(i);
                double x = pos.getValue().getValue().get(0);
                double y = pos.getValue().getValue().get(1);
                Coordinate coordinate = new Coordinate(x, y);
                interiorCoordinates.add(coordinate);
            }    
            LinearRing interiorRing = factory.createLinearRing(new CoordinateArraySequence((Coordinate[]) interiorCoordinates.toArray(new Coordinate[0])));

            // This is for you, Clemens:
            if (interiorRing.equals(exteriorRing)) {
                continue;
            }
            interiorRings.add(interiorRing);
        }
        
        Polygon polygon = factory.createPolygon(exteriorRing, interiorRings.toArray(new LinearRing[0]));

        return polygon;
    }
    
    public MultiPolygon convertMultiSurface(MultiSurfacePropertyTypeType multiSurfacePropertyTypeType) throws ParseException {
        PrecisionModel precisionModel = new PrecisionModel(1000);
        GeometryFactory factory = new GeometryFactory(precisionModel);
        
        MultiSurfaceTypeType multiSurfaceTypeType = multiSurfacePropertyTypeType.getMultiSurface().getValue();
        logger.debug(String.valueOf(multiSurfaceTypeType.getSurfaceMember().size()));
        
        ArrayList<Polygon> polygonList = new ArrayList<Polygon>();
        for (SurfaceMember surfaceMember : multiSurfaceTypeType.getSurfaceMember()) {            
            ch.ehi.oereb.schemas.gml._3_2.Polygon gmlPolygon = (ch.ehi.oereb.schemas.gml._3_2.Polygon) surfaceMember.getValue().getAbstractSurface();
                        
            // exterior ring
            ch.ehi.oereb.schemas.gml._3_2.LinearRing gmlLinearRing = (ch.ehi.oereb.schemas.gml._3_2.LinearRing) gmlPolygon.getValue().getExterior().getValue().getAbstractRing();
            ArrayList<Coordinate> exteriorCoordinates = new ArrayList<Coordinate>();
                        
            if (gmlLinearRing.getValue().getPosOrPointPropertyOrPointRep().size() > 0) {
                for (int i=0; i<gmlLinearRing.getValue().getPosOrPointPropertyOrPointRep().size(); i++) {
                    Pos pos = (Pos) gmlLinearRing.getValue().getPosOrPointPropertyOrPointRep().get(i);                
                    double x = pos.getValue().getValue().get(0);
                    double y = pos.getValue().getValue().get(1);
                    Coordinate coordinate = new Coordinate(x, y);
                    exteriorCoordinates.add(coordinate);
                }
            } else if (gmlLinearRing.getValue().getPosList() != null) {
                for (int i=0; i<gmlLinearRing.getValue().getPosList().getValue().getValue().size(); i=i+2) {
                    double x = gmlLinearRing.getValue().getPosList().getValue().getValue().get(i);
                    double y = gmlLinearRing.getValue().getPosList().getValue().getValue().get(i+1);
                    Coordinate coordinate = new Coordinate(x, y);
                    exteriorCoordinates.add(coordinate);
                }
            } else {
                throw new java.text.ParseException("could not parse gml (exterior ring)", 0);
            }
            
            LinearRing exteriorRing = factory.createLinearRing(new CoordinateArraySequence((Coordinate[]) exteriorCoordinates.toArray(new Coordinate[0])));
            logger.debug("exterior ring: " + exteriorRing.toText());
            
            // interior ring(s)
            List<Interior> interiors = gmlPolygon.getValue().getInterior();
            ArrayList<LinearRing> interiorRings = new ArrayList<LinearRing>();
            for (Interior interior : interiors) {
                ch.ehi.oereb.schemas.gml._3_2.LinearRing gmlInteriorRing = (ch.ehi.oereb.schemas.gml._3_2.LinearRing) interior.getValue().getAbstractRing();
                ArrayList<Coordinate> interiorCoordinates = new ArrayList<Coordinate>();
                
                if (gmlInteriorRing.getValue().getPosOrPointPropertyOrPointRep().size() > 0) {
                    for (int i=0; i<gmlInteriorRing.getValue().getPosOrPointPropertyOrPointRep().size(); i++) {
                        Pos pos = (Pos) gmlInteriorRing.getValue().getPosOrPointPropertyOrPointRep().get(i);

                        double x = pos.getValue().getValue().get(0);
                        double y = pos.getValue().getValue().get(1);

                        Coordinate coordinate = new Coordinate(x, y);
                        interiorCoordinates.add(coordinate);
                    }      
                } else if (gmlInteriorRing.getValue().getPosList() != null) {
                    for (int i=0; i<gmlLinearRing.getValue().getPosList().getValue().getValue().size(); i=i+2) {
                        double x = gmlLinearRing.getValue().getPosList().getValue().getValue().get(i);
                        double y = gmlLinearRing.getValue().getPosList().getValue().getValue().get(i+1);
                        Coordinate coordinate = new Coordinate(x, y);
                        interiorCoordinates.add(coordinate);
                    }
                } else {
                    throw new java.text.ParseException("could not parse gml (interior ring)", 0);
                }

                LinearRing interiorRing = factory.createLinearRing(new CoordinateArraySequence((Coordinate[]) interiorCoordinates.toArray(new Coordinate[0])));
                logger.debug("interior ring: " + interiorRing.toText());

                // This is for you, again, Clemens:
                if (interiorRing.equals(exteriorRing)) {
                    continue;
                }
                interiorRings.add(interiorRing);
            }
            
            Polygon polygon = factory.createPolygon(exteriorRing, interiorRings.toArray(new LinearRing[0]));
            logger.debug("resulting single polygon: " + polygon.toText());
            
            polygonList.add(polygon);
        }
        
        MultiPolygon multiPolygon = factory.createMultiPolygon(polygonList.toArray(new Polygon[0]));
        return multiPolygon;
    }
}
