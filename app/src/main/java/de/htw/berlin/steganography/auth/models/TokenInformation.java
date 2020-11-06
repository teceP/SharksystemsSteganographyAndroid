package de.htw.berlin.steganography.auth.models;

import de.htw.berlin.steganography.auth.constants.Constants;

public class TokenInformation {

    private transient static final long serialVersionUID = 1L;

    private String network;
    private String key;

    private String token = Constants.NO_RESULT;
    private long tokenTimestamp = -1;

    private String accessToken = Constants.NO_RESULT;
    private long accessTokenTimestamp = -1;

    private String refreshToken = Constants.NO_RESULT;
    private long refreshTokenTimestamp = -1;

    public TokenInformation(){}

    public TokenInformation(String network){
        this.network = network.trim().toLowerCase();
        this.key = this.network + Constants.TOKEN_OBJ_SUFFIX;
    }

    @Override
    public String toString(){
        return this.getNetwork() + " -> "
                + "\n[Token: " + this.getToken() + "/" + this.getTokenTimestamp() + "]"
                + "\n[Access Token: " + this.getAccessToken() + "/" + this.getAccessTokenTimestamp() + "]"
                + "\n[Refresh Token: " + this.getAccessToken() + "/" + this.getAccessTokenTimestamp() + "]";
    }

    public TokenInformation(Builder builder){
        this.network = builder.network;
        this.key = builder.key;
        this.token = builder.token;
        this.tokenTimestamp = builder.tokenTimestamp;
        this.accessToken = builder.accessToken;
        this.accessTokenTimestamp = builder.accessTokenTimestamp;
        this.refreshToken = builder.refreshToken;
        this.refreshTokenTimestamp = builder.refreshTokenTimestamp;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenTimestamp(long tokenTimestamp) {
        this.tokenTimestamp = tokenTimestamp;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAccessTokenTimestamp(long accessTokenTimestamp) {
        this.accessTokenTimestamp = accessTokenTimestamp;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setRefreshTokenTimestamp(long refreshTokenTimestamp) {
        this.refreshTokenTimestamp = refreshTokenTimestamp;
    }

    public String getNetwork() {
        return network;
    }

    public String getKey() {
        return key;
    }

    public String getToken() {
        return token;
    }

    public long getTokenTimestamp() {
        return tokenTimestamp;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getAccessTokenTimestamp() {
        return accessTokenTimestamp;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getRefreshTokenTimestamp() {
        return refreshTokenTimestamp;
    }

    public static Builder Builder(){
        return new Builder();
    }

    public static final class Builder{
        private String network;
        private String key;

        private String token;
        private long tokenTimestamp;

        private String accessToken;
        private long accessTokenTimestamp;

        private String refreshToken;
        private long refreshTokenTimestamp;

        public Builder(){}

        public Builder withNetwork(String network){
            this.network = network;
            this.key = this.network + Constants.TOKEN_OBJ_SUFFIX;
            return this;
        }

        public Builder withToken(String token) {
            this.token = token;
            return this;
        }

        public Builder withTokenTimestamp(long tokenTimestamp) {
            this.tokenTimestamp = tokenTimestamp;
            return this;
        }

        public Builder withAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder withAccessTokenTimestamp(long accessTokenTimestamp) {
            this.accessTokenTimestamp = accessTokenTimestamp;
            return this;
        }

        public Builder withRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder withRefreshTokenTimestamp(long refreshTokenTimestamp) {
            this.refreshTokenTimestamp = refreshTokenTimestamp;
            return this;
        }

        public TokenInformation build(){
            return new TokenInformation(this);
        }
    }
}
