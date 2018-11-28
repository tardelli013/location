package br.com.tardelli.location.model;

import java.util.ArrayList;
import java.util.List;

public class ReturnPlacesByAddressDTO {

    private Integer total = 0;
    private List<Places> places = new ArrayList<>();

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Places> getPlaces() {
        return places;
    }

    public void setPlaces(List<Places> places) {
        this.places = places;
    }

    public static class Places {
        private String placeId;
        private String placeAddress;

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public String getPlaceAddress() {
            return placeAddress;
        }

        public void setPlaceAddress(String placeAddress) {
            this.placeAddress = placeAddress;
        }
    }
}
