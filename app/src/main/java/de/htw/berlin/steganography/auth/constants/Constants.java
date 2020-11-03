package de.htw.berlin.steganography.auth.constants;

public interface Constants {

    int ONE_HOUR_IN_MINS = 60;
    long ONE_HOUR_IN_MS = 3600000;
    String SHARKSYS_PREF = "sharksys_prefs";

    String TOKEN_OBJ_SUFFIX = "_shared_pref";

    String REDDIT_TOKEN_OBJ = "reddit" + TOKEN_OBJ_SUFFIX;
    String IMGUR_TOKEN_OBJ = "imgur" + TOKEN_OBJ_SUFFIX;
    String TWITTER_TOKEN_OBJ = "twitter" + TOKEN_OBJ_SUFFIX;
    String INSTAGRAM_TOKEN_OBJ = "instagram" + TOKEN_OBJ_SUFFIX;
    String YOUTUBE_TOKEN_OBJ = "youtube" + TOKEN_OBJ_SUFFIX;

    int T_AT_NOT_EXPIRED = 0;
    int T_EXPIRED = 1;
    int AT_NEVER_RETRIEVED = 2;
    int AT_NEEDS_REFRESH = 3;
    int STATUS_UNCHECKED = 4;

    String NO_RESULT = "";

}
