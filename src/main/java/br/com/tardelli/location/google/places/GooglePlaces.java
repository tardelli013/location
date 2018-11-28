package br.com.tardelli.location.google.places;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

@Component
public class GooglePlaces {

    @Value("${maps.googleapi.url.autocomplete}")
    private String autocompleteUrl;

    @Value("${maps.googleapi.url.detail}")
    private String detailUrl;

    @Value("${maps.googleapi.url.photo}")
    private String photoUrl;

    @Value("${maps.googleapi.url.nearby_search}")
    private String nearbySearchUrl;

    @Value("${maps.googleapi.url.text_search}")
    private String textSearchUrl;

    @Value("${maps.googleapi.apikey}")
    private String apikey;

    private CloseableHttpClient client;

    private Gson gson;

    public void buildConnection() {
        buildConnection(HttpClientBuilder.create().useSystemProperties().build());
    }

    public void buildConnection(CloseableHttpClient client) {
        GsonBuilder gb = new GsonBuilder();
        gb.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        this.gson = gb.create();

        this.client = client;
    }

    private AutocompleteResult parseAutocompleteResponse(HttpResponse response) throws IOException {
        return this.gson.fromJson(new InputStreamReader(response.getEntity().getContent()), AutocompleteResult.class);
    }

    private PlaceDetailResult parseDetailResponse(HttpResponse response) throws IOException {
        return this.gson.fromJson(new InputStreamReader(response.getEntity().getContent()), PlaceDetailResult.class);
    }

    private PlacesResult parseSearchResponse(HttpResponse response) throws IOException {
        return this.gson.fromJson(new InputStreamReader(response.getEntity().getContent()), PlacesResult.class);
    }

    public AutocompleteResult autocomplete(String input, String language) {
        return autocomplete(input, language, null);
    }

    public AutocompleteResult autocomplete(String input, String language, PlacesQueryOptions options) {
        try {
            buildConnection();
            URIBuilder url = new URIBuilder(autocompleteUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter("input", input);

            if (Objects.nonNull(options)) {
                options.params().keySet().forEach(param -> url.addParameter(param, options.param(param)));
            }

            HttpGet get = new HttpGet(url.build());
            return this.parseAutocompleteResponse(this.client.execute(get));

        } catch (Exception e) {
            throw new PlacesException(e);
        }
    }

    public PlaceDetailResult detail(String placeId, String language) {
        try {
            buildConnection();
            URIBuilder url = new URIBuilder(detailUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter("placeid", placeId);

            HttpGet get = new HttpGet(url.build());
            return this.parseDetailResponse(this.client.execute(get));

        } catch (Exception e) {
            throw new PlacesException(e);
        }
    }

    @Deprecated
    public PlaceDetailResult detail(String reference, String language, boolean sensor) {
        try {
            buildConnection();
            URIBuilder url = new URIBuilder(detailUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter("reference", reference);
            url.addParameter("sensor", String.valueOf(sensor));

            HttpGet get = new HttpGet(url.build());
            return this.parseDetailResponse(this.client.execute(get));

        } catch (Exception e) {
            throw new PlacesException(e);
        }
    }

    public URL photoUrl(String photoReference, String language, Integer maxHeight, Integer maxWidth) {

        try {
            buildConnection();
            URIBuilder url = new URIBuilder(photoUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter("photoreference", photoReference);
            url.addParameter("maxheight", maxHeight != null ? String.valueOf(maxHeight) : null);
            url.addParameter("maxwidth", maxWidth != null ? String.valueOf(maxWidth) : null);

            return url.build().toURL();

        } catch (MalformedURLException | URISyntaxException e) {
            throw new PlacesException(e);
        }
    }

    public PlacesResult searchNearby(float lat, float lon, int radius, String language) {
        return searchNearby(lat, lon, radius, null, language);
    }

    public PlacesResult searchNearby(float lat, float lon, int radius, PlacesQueryOptions options, String language) {
        try {
            buildConnection();
            URIBuilder url = new URIBuilder(nearbySearchUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter(PlacesQueryOptions.LOCATION, lat + "," + lon);
            url.addParameter(PlacesQueryOptions.RADIUS, String.valueOf(radius));

            if (Objects.nonNull(options)) {
                options.params().keySet().forEach(param -> url.addParameter(param, options.param(param)));
            }

            HttpGet get = new HttpGet(url.build());
            return this.parseSearchResponse(this.client.execute(get));

        } catch (Exception e) {
            throw new PlacesException(e);
        }
    }

    public PlacesResult searchNearby(Place.Location location, int radius, String language) {
        return this.searchNearby(location, radius, null, language);
    }

    public PlacesResult searchNearby(Place.Location location, int radius, PlacesQueryOptions options, String language) {
        return this.searchNearby(location.getLat(), location.getLng(), radius, options, language);
    }

    public PlacesResult searchNearby(String pageToken, String language) {

        try {
            buildConnection();
            URIBuilder url = new URIBuilder(nearbySearchUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter("pagetoken", pageToken);

            HttpGet get = new HttpGet(url.build());
            return this.parseSearchResponse(this.client.execute(get));

        } catch (Exception e) {
            throw new PlacesException(e);
        }
    }

    public PlacesResult searchText(String query, String language) {
        return searchText(query, language, null);
    }

    public PlacesResult searchText(String query, String language, PlacesQueryOptions options) {

        try {
            buildConnection();
            URIBuilder url = new URIBuilder(textSearchUrl);
            url.addParameter("key", this.apikey);
            url.addParameter(PlacesQueryOptions.LANGUAGE, language);
            url.addParameter("query", query);

            if (Objects.nonNull(options)) {
                options.params().keySet().forEach(param -> url.addParameter(param, options.param(param)));
            }

            HttpGet get = new HttpGet(url.build());
            return this.parseSearchResponse(this.client.execute(get));

        } catch (Exception e) {
            throw new PlacesException(e);
        }
    }
}
