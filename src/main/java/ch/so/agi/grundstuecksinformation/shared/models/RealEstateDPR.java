package ch.so.agi.grundstuecksinformation.shared.models;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RealEstateDPR implements IsSerializable {
    private String realEstateType;
    
    private String number;
    
    private String identND;
    
    private String egrid;
    
    private String canton;
    
    private String municipality;
    
    private String subunitOfLandRegister;
    
    private int fosnNr;
    
    private int landRegistryArea;
    
    private String limit;
    
    private String oerebExtractIdentifier;
    
    private String oerebPdfExtractUrl;
    
    private Office oerebCadastreAuthority;
    
    private ArrayList<ThemeWithoutData> oerebThemesWithoutData;
    
    private ArrayList<NotConcernedTheme> oerebNotConcernedThemes;
    
    private ArrayList<ConcernedTheme> oerebConcernedThemes;
    
    public String getRealEstateType() {
        return realEstateType;
    }

    public void setRealEstateType(String realEstateType) {
        this.realEstateType = realEstateType;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIdentND() {
        return identND;
    }

    public void setIdentND(String identND) {
        this.identND = identND;
    }

    public String getEgrid() {
        return egrid;
    }

    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getSubunitOfLandRegister() {
        return subunitOfLandRegister;
    }

    public void setSubunitOfLandRegister(String subunitOfLandRegister) {
        this.subunitOfLandRegister = subunitOfLandRegister;
    }

    public int getFosnNr() {
        return fosnNr;
    }

    public void setFosnNr(int fosnNr) {
        this.fosnNr = fosnNr;
    }

    public int getLandRegistryArea() {
        return landRegistryArea;
    }

    public void setLandRegistryArea(int landRegistryArea) {
        this.landRegistryArea = landRegistryArea;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOerebExtractIdentifier() {
        return oerebExtractIdentifier;
    }

    public void setOerebExtractIdentifier(String oerebExtractIdentifier) {
        this.oerebExtractIdentifier = oerebExtractIdentifier;
    }

    public String getOerebPdfExtractUrl() {
        return oerebPdfExtractUrl;
    }

    public void setOerebPdfExtractUrl(String oerebPdfExtractUrl) {
        this.oerebPdfExtractUrl = oerebPdfExtractUrl;
    }

    public Office getOerebCadastreAuthority() {
        return oerebCadastreAuthority;
    }

    public void setOerebCadastreAuthority(Office oerebCadastreAuthority) {
        this.oerebCadastreAuthority = oerebCadastreAuthority;
    }

    public ArrayList<ThemeWithoutData> getOerebThemesWithoutData() {
        return oerebThemesWithoutData;
    }

    public void setOerebThemesWithoutData(ArrayList<ThemeWithoutData> oerebThemesWithoutData) {
        this.oerebThemesWithoutData = oerebThemesWithoutData;
    }

    public ArrayList<NotConcernedTheme> getOerebNotConcernedThemes() {
        return oerebNotConcernedThemes;
    }

    public void setOerebNotConcernedThemes(ArrayList<NotConcernedTheme> oerebNotConcernedThemes) {
        this.oerebNotConcernedThemes = oerebNotConcernedThemes;
    }

    public ArrayList<ConcernedTheme> getOerebConcernedThemes() {
        return oerebConcernedThemes;
    }

    public void setOerebConcernedThemes(ArrayList<ConcernedTheme> oerebConcernedThemes) {
        this.oerebConcernedThemes = oerebConcernedThemes;
    }
}
