package br.com.tardelli.location.model;

import java.util.List;

public class ReturnLocationsInsertedsDTO {

    private Integer total = 0;
    private List<LocationDTO> locations;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<LocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationDTO> locations) {
        this.locations = locations;
    }
}
