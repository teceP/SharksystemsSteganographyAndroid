package de.htw.berlin.steganography.auth.models;

public class Information {

    private String platform;
    private String clientId;
    private String clientSecret;
    private String authUrl;
    private String tokenUrl;
    private String redirectUri;
    private String duration;
    private String grantType;
    private String scope;
    private String responseType;
    private String state;

    public Information(){
    }

    public Information(Builder builder){
        this.platform = builder.platform;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.authUrl = builder.authUrl;
        this.tokenUrl = builder.tokenUrl;
        this.redirectUri = builder.redirectUri;
        this.duration = builder.duration;
        this.grantType = builder.grantType;
        this.scope = builder.scope;
        this.responseType = builder.responseType;
        this.state = builder.state;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public static Builder Builder(){
        return new Builder();
    }

    public static final class Builder{
        private String platform;
        private String clientId;
        private String clientSecret;
        private String authUrl;
        private String tokenUrl;
        private String redirectUri;
        private String duration;
        private String grantType;
        private String scope;
        private String responseType;
        private String state;

        public Builder(){}

        public Builder withPlatform(String platform){
            this.platform = platform;
            return this;
        }

        public Builder withState(String state){
            this.state = state;
            return this;
        }

        public Builder withResponseType(String responseType){
            this.responseType = responseType;
            return this;
        }

        public Builder withClientId(String clientId){
            this.clientId = clientId;
            return this;
        }

        public Builder withClientSecret(String clientSecret){
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder withTokenUrl(String tokenUrl){
            this.tokenUrl = tokenUrl;
            return this;
        }

        public Builder withAuthUrl(String authUrl){
            this.authUrl = authUrl;
            return this;
        }

        public Builder withRedirectUri(String redirectUri){
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder withDuration(String duration){
            this.duration = duration;
            return this;
        }

        public Builder withGrantType(String grantType){
            this.grantType = grantType;
            return this;
        }

        public Builder withScope(String scope){
            this.scope = scope;
            return this;
        }

        public Information build(){
            return new Information(this);
        }
    }
}
