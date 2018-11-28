package br.com.tardelli.location.service;

import br.com.tardelli.location.Exceptions.BadRequestException;
import br.com.tardelli.location.google.places.AddressComponent;
import br.com.tardelli.location.google.places.AddressComponentType;
import br.com.tardelli.location.google.places.PlaceDetailResult;
import br.com.tardelli.location.model.LocationDTO;
import br.com.tardelli.location.model.LogLocalidade;
import br.com.tardelli.location.model.SubLocationDTO;
import br.com.tardelli.location.utils.LocationUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class LocationService {

    @Autowired
    private LogLocalidadeService logLocalidadeService;

    public List<LocationDTO> insertLocations(Map<String, PlaceDetailResult> placesResultMap, boolean returnSublocations) {

        //buscar os placeIds que ja existem na base de dados
        Map<String, LocationDTO> dtosInDataBase = findPlacesIdsExistsInDataBase(placesResultMap);

        placesResultMap.forEach((s, placeDetailResult) -> {
            if (placeDetailResult.isOkay()) {

                // caso placeId nao exista na base de dados, efetuar insert
                if (!(dtosInDataBase.containsKey(placeDetailResult.getResult().getPlaceId()) || dtosInDataBase.containsKey(s))) {

                    //verificar se o placeId de retorno eh igual o da busca, se for diferente inserir os dois no registro (placeId e placeId2)
                    if (s.equalsIgnoreCase(placeDetailResult.getResult().getPlaceId())) {
                        dtosInDataBase.put(s, insertLocationInDataBase(placeDetailResult, null));
                    } else {
                        dtosInDataBase.put(s, insertLocationInDataBase(placeDetailResult, s));
                    }
                }
            }
        });

        List<LocationDTO> list = new ArrayList<>(dtosInDataBase.values());
        if (returnSublocations) {
            list.forEach(locationDTO -> locationDTO.setSubLocations(new HashSet<>(findSubLocationsInDB(locationDTO.getPlaceId(), null))));
        }
        return list;
    }

    private LocationDTO insertLocationInDataBase(PlaceDetailResult placeDetailResult, String placeId1) {
        Firestore db = FirestoreClient.getFirestore();

        final LocationDTO locationDTO = new LocationDTO();
        Map<String, Object> data = mapPlaceDetailResultInDataMap(placeDetailResult, placeId1, locationDTO);

        mapLocNuInDataMap(data, locationDTO);

        ApiFuture<WriteResult> result = db.collection("places").document().create(data);

        try {
            System.out.println("Inserted placeId in DB: " + locationDTO.getPlaceId() + " Date: " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage());
        }

        return locationDTO;
    }

    private void mapLocNuInDataMap(Map<String, Object> data, LocationDTO locationDTO) {
        LogLocalidade logLocalidade = new LogLocalidade();
        logLocalidade.setUf(locationDTO.getUf());
        logLocalidade.setState(locationDTO.getState());
        logLocalidade.setCity(locationDTO.getCity());

        List<LogLocalidade> logLocalidadeReturn = logLocalidadeService.findLogLocalidade(logLocalidade);

        if (!logLocalidadeReturn.isEmpty()) {
            data.put("locNu", logLocalidadeReturn.get(0).getLoc_nu());
            locationDTO.setLocNu(logLocalidadeReturn.get(0).getLoc_nu());
        } else {
            throw new BadRequestException(LocationUtils.getJsonNotFoundParamInDB("locNu"));
        }
    }

    private Map<String, Object> mapPlaceDetailResultInDataMap(PlaceDetailResult placeDetailResult, String placeId2, LocationDTO locationDTO) {
        Map<String, Object> data = new HashMap<>();

        if (placeDetailResult.isOkay()) {

            locationDTO.setCreatedAt(new Date());
            data.put("created_at", locationDTO.getCreatedAt());

            String addressName = getAddressNameByPlaceDetailResult(placeDetailResult);
            String street = getStreetByPlaceDetailResult(placeDetailResult);
            String number = getNumberByPlaceDetailResult(placeDetailResult);
            String neighborhood = getNeighborhoodByPlaceDetailResult(placeDetailResult);

            String city = getCityByPlaceDetailResult(placeDetailResult);
            String state = getStateByPlaceDetailResult(placeDetailResult);
            String country = getCountryByPlaceDetailResult(placeDetailResult);

            String postalCode = getPostalCodeByPlaceDetailResult(placeDetailResult);
            String uf = getUFByPlaceDetailResult(placeDetailResult);
            String placeId = getPlaceIdByPlaceDetailResult(placeDetailResult);
            Double latitude = getLatitudeByPlaceDetailResult(placeDetailResult);
            Double longitude = getLongitudeByPlaceDetailResult(placeDetailResult);

            data.put("addressName", addressName);
            locationDTO.setAddressName(addressName);
            data.put("street", street);
            locationDTO.setStreet(street);
            data.put("number", number);
            locationDTO.setNumber(number);
            data.put("neighborhood", neighborhood);
            locationDTO.setNeighborhood(neighborhood);
            data.put("city", city);
            locationDTO.setCity(city);
            data.put("state", state);
            locationDTO.setState(state);
            data.put("country", country);
            locationDTO.setCountry(country);
            data.put("postalCode", postalCode);
            locationDTO.setPostalCode(postalCode);
            data.put("uf", uf);
            locationDTO.setUf(uf);
            data.put("latitude", latitude);
            locationDTO.setLatitude(latitude);
            data.put("longitude", longitude);
            locationDTO.setLongitude(longitude);

            if (Objects.nonNull(placeId2)) {
                data.put("placeId2", placeId2);
                locationDTO.setPlaceId2(placeId2);
                data.put("placeId", placeId);
                locationDTO.setPlaceId(placeId);
            } else {
                data.put("placeId", placeId);
                locationDTO.setPlaceId(placeId);
            }

        }

        return data;
    }

    private String getNumberByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> number = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.STREET_NUMBER.toString())) {
                number.set(addressComponent.getLongName());
            }
        }));

        return number.get();
    }

    private String getStreetByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> street = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.ROUTE.toString())) {
                street.set(addressComponent.getLongName());
            }
        }));

        return street.get();
    }

    private String getAddressNameByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        return placeDetailResult.getResult().getFormattedAddress();
    }

    private Double getLongitudeByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        return (double) placeDetailResult.getResult().getGeometry().getLocation().getLng();
    }

    private Double getLatitudeByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        return (double) placeDetailResult.getResult().getGeometry().getLocation().getLat();
    }

    private String getPlaceIdByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        return placeDetailResult.getResult().getPlaceId();
    }

    private String getUFByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> uf = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1.toString())) {
                uf.set(addressComponent.getShortName());
            }
        }));

        return uf.get();
    }

    private String getPostalCodeByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> postalCode = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.POSTAL_CODE.toString())) {
                postalCode.set(addressComponent.getLongName());
            }
        }));

        return postalCode.get();
    }

    private String getCountryByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> country = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.COUNTRY.toString())) {
                country.set(addressComponent.getLongName());
            }
        }));

        return country.get();
    }

    private String getStateByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> state = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1.toString())) {
                state.set(addressComponent.getLongName());
            }
        }));

        return state.get();
    }

    private String getCityByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        AtomicReference<String> city = new AtomicReference<>();
        List<AddressComponent> addressComponents = placeDetailResult.getResult().getAddressComponents();
        addressComponents.forEach(addressComponent -> addressComponent.getTypes().forEach(s -> {
            if (s.equalsIgnoreCase(AddressComponentType.LOCALITY.toString()) ||
                    s.equalsIgnoreCase(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2.toString())) {
                city.set(addressComponent.getLongName());
            }
        }));

        return city.get();
    }

    private String getNeighborhoodByPlaceDetailResult(PlaceDetailResult placeDetailResult) {
        return placeDetailResult.getResult().getVicinity();
    }

    private Map<String, LocationDTO> findPlacesIdsExistsInDataBase(Map<String, PlaceDetailResult> placesResultMap) {
        Set<String> placeIds = new HashSet<>();
        placesResultMap.forEach((s, placeDetailResult) -> {
            if (placeDetailResult.isOkay()) {
                placeIds.add(s);
                placeIds.add(placeDetailResult.getResult().getPlaceId());
            }
        });

        return findLocationsInDBByPlaceIds(placeIds);
    }

    private Map<String, LocationDTO> findLocationsInDBByPlaceIds(Set<String> placeIds) {
        final Map<String, LocationDTO> dtoMap = new HashMap<>();

        if (Objects.isNull(placeIds)) {
            return dtoMap;
        }

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference places = db.collection("places");

        placeIds.forEach(placeId -> {
            Query query = places.whereEqualTo("placeId", placeId).limit(1);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            mapLocationDTO(querySnapshot, dtoMap, false);
        });

        placeIds.forEach(placeId -> {
            Query query = places.whereEqualTo("placeId2", placeId).limit(1);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            mapLocationDTO(querySnapshot, dtoMap, true);
        });

        return dtoMap;
    }

    private void mapLocationDTO(ApiFuture<QuerySnapshot> querySnapshot, Map<String, LocationDTO> dtoMap, boolean isPlaceId2) {

        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                LocationDTO locationDTO = new LocationDTO();

                locationDTO.setLocationId(document.getId());
                locationDTO.setAddressName(document.contains("addressName") ? document.getString("addressName") : null);
                locationDTO.setStreet(document.contains("street") ? document.getString("street") : null);
                locationDTO.setNumber(document.contains("number") ? document.getString("number") : null);
                locationDTO.setNeighborhood(document.contains("neighborhood") ? document.getString("neighborhood") : null);
                locationDTO.setCity(document.contains("city") ? document.getString("city") : null);
                locationDTO.setState(document.contains("state") ? document.getString("state") : null);
                locationDTO.setCountry(document.contains("country") ? document.getString("country") : null);
                locationDTO.setPostalCode(document.contains("postalCode") ? document.getString("postalCode") : null);
                locationDTO.setUf(document.contains("uf") ? document.getString("uf") : null);
                locationDTO.setPlaceId(document.contains("placeId") ? document.getString("placeId") : null);
                locationDTO.setPlaceId2(document.contains("placeId2") ? document.getString("placeId2") : null);
                locationDTO.setLatitude(document.contains("latitude") ? document.getDouble("latitude") : null);
                locationDTO.setLongitude(document.contains("longitude") ? document.getDouble("longitude") : null);
                locationDTO.setLocNu(document.contains("locNu") ? Objects.requireNonNull(document.getLong("locNu")).intValue() : null);
                locationDTO.setCreatedAt((Date) document.get("created_at"));

                //String placeIdToMap = isPlaceId2 ? locationDTO.getPlaceId2() : locationDTO.getPlaceId();
                dtoMap.put(locationDTO.getPlaceId(), locationDTO);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public List<SubLocationDTO> insertSublocations(Map<String, PlaceDetailResult> subLocationForInsert) {
        List<SubLocationDTO> list = new ArrayList<>();
        if (Objects.isNull(subLocationForInsert)) {
            return list;
        }

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference subLocations = db.collection("subLocations");

        subLocationForInsert.forEach((s, placeDetailResult) -> list.add(insertSubLocation(db, subLocations, s, placeDetailResult.getResult().getPlaceId())));

        return list;
    }

    public SubLocationDTO insertSublocation(String subLocation, String placeId){
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference subLocations = db.collection("subLocations");

        return insertSubLocation(db, subLocations, subLocation, placeId);
    }

    private SubLocationDTO insertSubLocation(Firestore db, CollectionReference subLocations, String subLocation, String placeId) {
        Query query = subLocations
                .whereEqualTo("placeId", placeId)
                .whereEqualTo("text", subLocation).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        SubLocationDTO dto = null;

        // pesquisa registro na base de dados
        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                dto = new SubLocationDTO();
                dto.setSubLocationId(document.getId());
                dto.setPlaceId(document.getString("placeId"));
                dto.setSubLocationText(document.getString("text"));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage());
        }

        // caso ja exista na base apenas retorna objeto, se nao existir insere
        if (Objects.isNull(dto)) {
            dto = new SubLocationDTO();
            dto.setSubLocationText(subLocation);
            dto.setPlaceId(placeId);

            Map<String, Object> data = new HashMap<>();
            data.put("placeId", dto.getPlaceId());
            data.put("text", dto.getSubLocationText());

            ApiFuture<WriteResult> result = db.collection("subLocations").document().create(data);

            try {
                System.out.println("Inserted subLocation in DB: " + dto.getSubLocationText() + " Date: " + result.get().getUpdateTime());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new BadRequestException(e.getMessage());
            }
        }

        return dto;
    }

    public List<SubLocationDTO> findSubLocationsInDB(String placeId, String text) {
        List<SubLocationDTO> locationDTOS = new ArrayList<>();

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference subLocations = db.collection("subLocations");

        Query query;

        if (Objects.nonNull(text)) {
            query = subLocations
                    .whereEqualTo("placeId", placeId)
                    .whereEqualTo("text", text);
        } else {
            query = subLocations
                    .whereEqualTo("placeId", placeId);
        }

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // pesquisa registro na base de dados
        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                SubLocationDTO dto = new SubLocationDTO();
                dto.setSubLocationId(document.getId());
                dto.setPlaceId(document.getString("placeId"));
                dto.setSubLocationText(document.getString("text"));

                locationDTOS.add(dto);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage());
        }

        return locationDTOS;
    }
}
