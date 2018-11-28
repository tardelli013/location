package br.com.tardelli.location.google.places;

import java.util.List;

public class PlaceDetailResult extends Result {

    private List<String> htmlAttributions;

    private PlaceDetail result;

    public List<String> getHtmlAttributions() {
        return this.htmlAttributions;
    }

    public PlaceDetail getResult() {
        return this.result;
    }
}
