package com.ruon2.rssapi.http;

public interface ResponseListener {

    void error(Exception e);

    void result(String result);

}
