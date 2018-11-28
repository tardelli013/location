package br.com.tardelli.location.model;

public class SubLocationDTO {

    private String subLocationId;
    private String subLocationText;
    private String placeId;

    public String getSubLocationId() {
        return subLocationId;
    }

    public void setSubLocationId(String subLocationId) {
        this.subLocationId = subLocationId;
    }

    public String getSubLocationText() {
        return subLocationText;
    }

    public void setSubLocationText(String subLocationText) {
        this.subLocationText = subLocationText;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
