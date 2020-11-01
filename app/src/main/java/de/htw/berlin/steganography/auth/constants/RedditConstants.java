package de.htw.berlin.steganography.auth.constants;

public interface RedditConstants {
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


}
