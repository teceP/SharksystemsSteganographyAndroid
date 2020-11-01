package de.htw.berlin.steganography;

public interface Constants {

    double ONE_HOUR_IN_MINS = 60;
    String SHARKSYS_PREF = "sharksys_prefs";

    String AUTH_STATUS = "reddit_auth_status";

    String TOKEN_STORAGE = "reddit_network_token";
    String TOKEN_STORAGE_TIMESTAMP = "reddit_network_token_timestamp";

    String ACCESS_TOKEN_STORAGE = "reddit_network_access_token";
    String ACCESS_TOKEN_STORAGE_TIMESTAMP = "reddit_network_access_token_timestamp";

    String REFRESH_TOKEN_STORAGE = "reddit_network_refresh_token";
    String REFRESH_TOKEN_STORAGE_TIMESTAMP = "reddit_network_refresh_token_timestamp";

    String NO_RESULT = "";

    String APP_NAME = "SharksystemsStegaMobile";

    String AUTH_URI = "https://www.reddit.com/api/v1/authorize.compact?";
    String TOKEN_URI = "https://www.reddit.com/api/v1/access_token";

    String REDIRECT = "http://localhost.com/";
    String RESPONSE_TYPE = "code";

    String CLIENT_ID = "Tv6k2mgIJ0UYrg";
    String CLIENT_SECRET = "";

    String GRANT_TYPE_AUTH_CODE = "authorization_code";
    String GRANT_TYPE_REFRESH = "refresh_token";

    String DURATION_PERM = "permanent";
    String SCOPE = "identity,edit,flair,history,modconfig,modflair,modlog,modposts,modwiki,mysubreddits,privatemessages,read,report,save,submit,subscribe,vote,wikiedit,wikiread";

    int T_AT_NOT_EXPIRED = 0;
    int T_EXPIRED = 1;
    int AT_NEVER_RETRIEVED = 2;
    int AT_NEEDS_REFRESH = 3;
    int STATUS_UNCHECKED = 4;


}
