package br.com.tardelli.location.rest;

import br.com.tardelli.location.Exceptions.BadRequestException;
import br.com.tardelli.location.google.places.AutocompleteResult;
import br.com.tardelli.location.google.places.GooglePlaces;
import br.com.tardelli.location.google.places.PlaceDetailResult;
import br.com.tardelli.location.google.places.PlacesQueryOptions;
import br.com.tardelli.location.model.*;
import br.com.tardelli.location.service.LocationService;
import br.com.tardelli.location.service.LogLocalidadeService;
import br.com.tardelli.location.utils.LocationUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Api(value = "Location", description = "API Locations")
@RequestMapping("/api")
public class LocationServiceRest {

    @Autowired
    private GooglePlaces googlePlaces;
    @Autowired
    private LocationService locationService;
    @Autowired
    private LogLocalidadeService logLocalidadeService;

    @GetMapping(value = "/autocomplete/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Autocomplete Places by address",
            position = 1,
            notes = "Supported languages: pt-BR \n" +
                    "Autocomplete after 5 characters entered \n" +
                    "Required fields: address, language ",
            response = ReturnPlacesByAddressDTO.class)
    public ResponseEntity<ReturnPlacesByAddressDTO> autocompletePlacesByAddress(
            @RequestParam(required = true) String address,
            @RequestParam(required = true) String language) {

        if ((Objects.isNull(address) || address.trim().equals(""))
                || (Objects.isNull(language) || language.trim().equals(""))) {
            throw new BadRequestException(LocationUtils.getJsonRequiredFields("language", "address"));
        }

        ReturnPlacesByAddressDTO dto = new ReturnPlacesByAddressDTO();
        if (address.trim().length() < 6) {
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        PlacesQueryOptions placesQueryOptions = new PlacesQueryOptions();
        placesQueryOptions.types("address");
        AutocompleteResult result = googlePlaces.autocomplete(address, language.replaceAll("_", "-"), placesQueryOptions);

        result.iterator().forEachRemaining(prediction -> {
            ReturnPlacesByAddressDTO.Places places = new ReturnPlacesByAddressDTO.Places();
            places.setPlaceAddress(prediction.getDescription());
            places.setPlaceId(prediction.getPlaceId());
            dto.getPlaces().add(places);
        });

        dto.setTotal(dto.getPlaces().size());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/sub-location/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "find subLocations by placeId",
            position = 2,
            notes = "Required fields: placeId",
            response = SubLocationDTO.class)
    public ResponseEntity<List<SubLocationDTO>> findSublocations(@RequestParam(required = true) String placeId) {

        if ((Objects.isNull(placeId) || placeId.trim().equals(""))) {
            throw new BadRequestException(LocationUtils.getJsonRequiredFields("placeId"));
        }

        List<SubLocationDTO> subLocations = locationService.findSubLocationsInDB(placeId, null);

        return new ResponseEntity<>(subLocations, HttpStatus.OK);
    }

    @PostMapping(value = "/sub-location/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Create subLocation by placeId",
            position = 3,
            notes = "Required fields: placeId, subLocationText",
            response = ReturnLocationsInsertedsDTO.class)
    public ResponseEntity<List<SubLocationDTO>> insertSubLocationByPlaceId(@RequestBody List<SubLocationDTO> insertSubLocationDTOS) {

        insertSubLocationDTOS.forEach(subLocationDTO -> {
            if ((Objects.isNull(subLocationDTO.getPlaceId()) || subLocationDTO.getPlaceId().trim().equals(""))
                    || (Objects.isNull(subLocationDTO.getSubLocationText()) || subLocationDTO.getSubLocationText().trim().equals(""))) {
                throw new BadRequestException(LocationUtils.getJsonRequiredFields("placeId", "subLocationText"));
            }
        });

        List<SubLocationDTO> list = new ArrayList<>();

        insertSubLocationDTOS.forEach(subLocationDTO -> list.add(locationService.insertSublocation(subLocationDTO.getSubLocationText(), subLocationDTO.getPlaceId())));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping(value = "/location/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Create Location by placeId",
            position = 4,
            notes = "Supported languages: pt-BR \n" +
                    "Required fields: placeId, language \n" +
                    "If subLocation is present, insert subLocation ",
            response = ReturnLocationsInsertedsDTO.class)
    public ResponseEntity<ReturnLocationsInsertedsDTO> insertLocationByPlaceId(@RequestBody List<InsertLocationDTO> insertLocationDTOS, @RequestParam(required = true) boolean returnSublocations) {

        insertLocationDTOS.forEach(insertLocation -> {
            if ((Objects.isNull(insertLocation.getLanguage()) || insertLocation.getLanguage().trim().equals(""))
                    || (Objects.isNull(insertLocation.getPlaceId()) || insertLocation.getPlaceId().trim().equals(""))) {
                throw new BadRequestException(LocationUtils.getJsonRequiredFields("placeId", "language"));
            }
        });

        Map<String, PlaceDetailResult> placesResultMap = new HashMap<>();
        Map<String, PlaceDetailResult> subLocationForInsert = new HashMap<>();

        insertLocationDTOS.forEach(insertLocationDTO -> {
            PlaceDetailResult detail = googlePlaces.detail(insertLocationDTO.getPlaceId(), insertLocationDTO.getLanguage().replaceAll("_", "-"));
            placesResultMap.put(insertLocationDTO.getPlaceId(), detail);

            if (Objects.nonNull(insertLocationDTO.getSubLocation())) {
                subLocationForInsert.put(insertLocationDTO.getSubLocation(), detail);
            }
        });

        locationService.insertSublocations(subLocationForInsert);
        List<LocationDTO> locationDTOS = locationService.insertLocations(placesResultMap, returnSublocations);

        ReturnLocationsInsertedsDTO returnLocationsInsertedsDTO = new ReturnLocationsInsertedsDTO();
        returnLocationsInsertedsDTO.setTotal(locationDTOS.size());
        returnLocationsInsertedsDTO.setLocations(locationDTOS);

        return new ResponseEntity<>(returnLocationsInsertedsDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "Find LocNu (table LOG_LOCALIDADE)",
            notes = "Required fields: city, state, uf \n",
            position = 5)
    @GetMapping(value = "/locnu/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<LogLocalidade>> findLogLocalidade(
            @RequestParam(required = true) String city,
            @RequestParam(required = true) String state,
            @RequestParam(required = true) String uf) {

        if ((Objects.isNull(city) || city.trim().equals(""))
                || (Objects.isNull(state) || state.trim().equals(""))
                || (Objects.isNull(uf) || uf.trim().equals(""))
                ) {
            throw new BadRequestException(LocationUtils.getJsonRequiredFields("city", "state", "uf"));
        }

        LogLocalidade filter = new LogLocalidade();
        filter.setCity(city);
        filter.setState(state);
        filter.setUf(uf);

        return new ResponseEntity<>(logLocalidadeService.findLogLocalidade(filter), HttpStatus.OK);
    }

    @ApiOperation(value = "Insert LocNu (table LOG_LOCALIDADE)",
            notes = "Required fields: loc_nu, city, uf, state \n",
            position = 6)
    @PostMapping(value = "/locnu/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity insertLogLocalidade(@RequestBody List<LogLocalidade> localidades, @RequestParam(required = true) String pass) {

        if (!pass.equals("s4bdigital")) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        localidades.forEach(logLocalidade -> {
            if ((Objects.isNull(logLocalidade.getLoc_nu()))
                    || (Objects.isNull(logLocalidade.getCity()) || logLocalidade.getCity().trim().equals(""))
                    || (Objects.isNull(logLocalidade.getUf()) || logLocalidade.getUf().trim().equals(""))
                    || (Objects.isNull(logLocalidade.getState()) || logLocalidade.getState().trim().equals(""))
                    ) {
                throw new BadRequestException(LocationUtils.getJsonRequiredFields("loc_nu", "city", "state", "uf"));
            }
        });

        Firestore db = FirestoreClient.getFirestore();
        AtomicReference<Integer> count = new AtomicReference<>(0);

        localidades.forEach(logLocalidade -> {

            count.getAndSet(count.get() + 1);

            Map<String, Object> data = new HashMap<>();
            data.put("loc_nu", logLocalidade.getLoc_nu());
            data.put("city", logLocalidade.getCity().trim().toLowerCase());
            data.put("uf", logLocalidade.getUf().trim().toLowerCase());
            data.put("state", logLocalidade.getState().trim().toLowerCase());
            data.put("postal_code", logLocalidade.getPostal_code());

            ApiFuture<WriteResult> result = db.collection("location").document().create(data);

            try {
                System.out.println(count + " - " + logLocalidade.getCity());
                System.out.println("Update time : " + result.get().getUpdateTime());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new BadRequestException(e.getMessage());
            }
        });

        return new ResponseEntity(HttpStatus.OK);
    }

}
