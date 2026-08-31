package com.ruon2.rssapi.http;

import static com.ruon2.rssapi.common.Util.drain;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class Http {

    private static Get get = new Get() {
        @Override
        public void execute(String uri, ResponseListener responseListener) {
            try {
                URL url = new URL(uri);
                String response = drain(url.openConnection().getInputStream());
                responseListener.result(response);
            } catch (Exception e) {
                responseListener.error(e);
            }
        }
    };

    public static void setGetImplementation(Get get) {
        Http.get = get;
    }

    private static String urlencode(String p) {
        try {
            return URLEncoder.encode(p, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return p;
        }
    }

    public static void get(ResponseListener responseListener, String uri, Object... params) {
        for (int i = 0; i < params.length; ++i) {
            params[i] = urlencode(params[i].toString());
        }
        uri = String.format(uri, params);
        get.execute(uri, responseListener);
    }
}
