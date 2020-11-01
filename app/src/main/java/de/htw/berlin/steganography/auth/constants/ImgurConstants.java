package de.htw.berlin.steganography.auth.constants;

public interface ImgurConstants {

    String AUTH_URI = "https://api.imgur.com/oauth2/authorize?";
    String TOKEN_URI = "https://api.imgur.com/oauth2/token";
    String CLIENT_ID = "7fc755188486e07";
    String CLIENT_SECRET = "d983dc5485a58843d791166ee700dbb8271bc2e9";
    String DURATION_PERM = "permanent";
    String REDIRECT = "http://localhost.com/";
    String SCOPE = "";
    String GRANT_TYPE_AUTH_CODE = "authorization_code";
    String GRANT_TYPE_REFRESH = "refresh_token";
    String RESPONSE_TYPE = "code";
}
