package sdntools;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;

class SDNConnector {

    SDNConnector(String url){
        this.SDNUrl = url;
    }

    private String SDNUrl = "http://192.168.0.17:8080";

    private String GetSDNItem(String request_link){
        HttpClient http_client = HttpClients.createDefault();
        HttpGet http_get = new HttpGet(SDNUrl + request_link);

        try {
            System.out.println("Trying to get: " + SDNUrl + request_link);
            HttpResponse http_response = http_client.execute(http_get);
            HttpEntity http_entity = http_response.getEntity();

            if (http_entity != null) {
                try (InputStream inputStream = http_entity.getContent()){
                    String out = convertStreamToString(inputStream);
                    System.out.println(out);
                    return out;
                }
            }
        }
        catch (IOException e) {
            System.out.println("Error getting http response:\n" + e);
        }

        System.out.println("Error, something went wrong. Returning null.\n");
        return null;
    }

    JSONArray GetSDNJsonArray(String sdn_path) {
        try {
            String json = GetSDNItem(sdn_path);

            if (json == null)
                throw new JSONException("Returned SDNItem is empty");

            return new JSONArray(json);
        }
        catch (JSONException e) {
            System.out.println("Error parsing JSON:\n" + e);
        }

        return null;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
