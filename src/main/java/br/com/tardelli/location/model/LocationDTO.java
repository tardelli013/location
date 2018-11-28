package br.com.tardelli.location.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class LocationDTO {

    // param id document
    private String locationId;
    private Date createdAt;

    // params google place
    private String addressName;
    private String neighborhood;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String uf;
    private String placeId;
    private String placeId2;
    private Double latitude;
    private Double longitude;
    private String street;
    private String number;

    // params log localidade
    private Integer locNu;

    //params sublocation
    private Set<SubLocationDTO> subLocations = new HashSet<>();

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getLocNu() {
        return locNu;
    }

    public void setLocNu(Integer locNu) {
        this.locNu = locNu;
    }

    public Set<SubLocationDTO> getSubLocations() {
        return subLocations;
    }

    public void setSubLocations(Set<SubLocationDTO> subLocations) {
        this.subLocations = subLocations;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getPlaceId2() {
        return placeId2;
    }

    public void setPlaceId2(String placeId2) {
        this.placeId2 = placeId2;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
