package br.com.tardelli.location.model;

public class LogLocalidade {

    private String documentId;
    private Integer loc_nu;
    private String city;
    private String uf;
    private String state;
    private String postal_code;

    public LogLocalidade() {
    }

    public Integer getLoc_nu() {
        return loc_nu;
    }

    public void setLoc_nu(Integer loc_nu) {
        this.loc_nu = loc_nu;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
