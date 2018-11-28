package br.com.tardelli.location.utils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class LocationUtils {

    public static String getJsonRequiredFields(String... str) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("required_fields", str);
        return jsonObject.toString();
    }

    public static String getJsonNotFoundParamInDB(String... str) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("param_not_found_ind_db", str);
        return jsonObject.toString();
    }

    public static String capitalizeString(String text) {
        if (Objects.isNull(text)) {
            return "";
        } else {
            return Arrays.stream(text.split(" "))
                    .map(x -> x.length() > 2 ? capitalize(x) : x)
                    .reduce((x, y) -> x + " " + y)
                    .orElse("");
        }
    }

    private static String capitalize(String x) {
        return x.substring(0, 1).toUpperCase() + x.substring(1).toLowerCase();
    }

}
