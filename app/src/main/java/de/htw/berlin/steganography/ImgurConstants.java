package de.htw.berlin.steganography;

public interface ImgurConstants {

    String AUTH_URI = "https://api.imgur.com/oauth2/authorize?";
    String TOKEN_URI = "https://api.imgur.com/oauth2/token";
    String CLIENT_ID = "7fc755188486e07";
    String CLIENT_SECRET = "d983dc5485a58843d791166ee700dbb8271bc2e9";
    String DURATION_PERM = "permanent";
    String REDIRECT = "http://localhost.com/";
    String SCOPE = "";
    String GRANT_TYPE_AUTH_CODE = "authorization_code";
    String RESPONSE_TYPE = "code";
    String STATE = "";

    String AUTH_STATUS = "imgur_auth_status";

    String TOKEN_STORAGE = "imgur_network_token";
    String TOKEN_STORAGE_TIMESTAMP = "imgur_network_token_timestamp";

    String ACCESS_TOKEN_STORAGE = "imgur_network_access_token";
    String ACCESS_TOKEN_STORAGE_TIMESTAMP = "imgur_network_access_token_timestamp";

    String REFRESH_TOKEN_STORAGE = "imgur_network_refresh_token";
    String REFRESH_TOKEN_STORAGE_TIMESTAMP = "imgur_network_refresh_token_timestamp";

    /**
     *                 .withAuthUrl(Constants.AUTH_URI)
     *                 .withTokenUrl(Constants.TOKEN_URI)
     *                 .withClientId(Constants.CLIENT_ID)
     *                 .withClientSecret(Constants.CLIENT_SECRET)
     *                 .withDuration(Constants.DURATION_PERM)
     *                 .withRedirectUri(Constants.REDIRECT)
     *                 .withScope(Constants.SCOPE)
     *                 .withGrantType(Constants.GRANT_TYPE_AUTH_CODE)
     */
}
