package net.siot.android.gateway.messagetypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sathesh on 30.10.15.
 * Data object class Urls. Representation of a JSON format.
 */
public class Urls {

    private List<String> urls = new ArrayList<String>();

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
