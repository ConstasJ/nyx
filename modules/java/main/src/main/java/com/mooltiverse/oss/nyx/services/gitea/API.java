package com.mooltiverse.oss.nyx.services.gitea;

import java.util.Objects;

public class API {
    private final String BASE_URL;

    protected API(String baseUrl) {
        super();
        Objects.requireNonNull(baseUrl, "The base URL cannot be null");
        this.BASE_URL = baseUrl;
    }
}
