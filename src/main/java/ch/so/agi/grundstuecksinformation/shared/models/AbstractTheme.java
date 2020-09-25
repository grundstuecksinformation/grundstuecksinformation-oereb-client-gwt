package ch.so.agi.grundstuecksinformation.shared.models;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class AbstractTheme implements IsSerializable {    
    private String code;
    
    private String name;
    
    private String subtheme;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtheme() {
        return subtheme;
    }

    public void setSubtheme(String subtheme) {
        this.subtheme = subtheme;
    }
}
