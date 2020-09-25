package ch.so.agi.grundstuecksinformation.server;

public class ThemeTuple {
    private String theme;
    private String subtheme;
    
    public ThemeTuple() {}
    
    public ThemeTuple(String theme, String subtheme) {
        this.theme = theme;
        this.subtheme = subtheme;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getSubtheme() {
        return subtheme;
    }

    public void setSubtheme(String subtheme) {
        this.subtheme = subtheme;
    }

    @Override
    public String toString() {
        return "ThemeTuple [theme=" + theme + ", subtheme=" + subtheme + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subtheme == null) ? 0 : subtheme.hashCode());
        result = prime * result + ((theme == null) ? 0 : theme.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThemeTuple other = (ThemeTuple) obj;
        if (subtheme == null) {
            if (other.subtheme != null)
                return false;
        } else if (!subtheme.equals(other.subtheme))
            return false;
        if (theme == null) {
            if (other.theme != null)
                return false;
        } else if (!theme.equals(other.theme))
            return false;
        return true;
    }
}
