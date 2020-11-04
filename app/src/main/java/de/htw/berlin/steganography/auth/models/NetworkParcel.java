package de.htw.berlin.steganography.auth.models;

import de.htw.berlin.steganography.auth.strategy.AuthStrategy;

public class NetworkParcel {

    private Enum network;
    private AuthStrategy authStrategy;
    private AuthInformation authInformation;
    private TokenInformation tokenInformation;

    public NetworkParcel(Builder builder){
        this.network = builder.network;
        this.authStrategy = builder.authStrategy;
        this.authInformation = builder.authInformation;
        this.tokenInformation = builder.tokenInformation;
    }


    public Enum getNetwork() {
        return network;
    }

    public void setNetwork(Enum network) {
        this.network = network;
    }

    public AuthStrategy getAuthStrategy() {
        return authStrategy;
    }

    public void setAuthStrategy(AuthStrategy authStrategy) {
        this.authStrategy = authStrategy;
    }

    public AuthInformation getAuthInformation() {
        return authInformation;
    }

    public void setAuthInformation(AuthInformation authInformation) {
        this.authInformation = authInformation;
    }

    public TokenInformation getTokenInformation() {
        return tokenInformation;
    }

    public void setTokenInformation(TokenInformation tokenInformation) {
        this.tokenInformation = tokenInformation;
    }

    public static class Builder{
        private Enum network;
        private AuthStrategy authStrategy;
        private AuthInformation authInformation;
        private TokenInformation tokenInformation;

        public Builder withNetwork(Enum network){
            this.network = network;
            return this;
        }

        public Builder withAuthStrategy(AuthStrategy authStrategy){
            this.authStrategy = authStrategy;
            return this;
        }

        public Builder withAuthInformation(AuthInformation authInformation){
            this.authInformation = authInformation;
            return this;
        }

        public Builder withTokenInformation(TokenInformation tokenInformation){
            this.tokenInformation = tokenInformation;
            return this;
        }

        public NetworkParcel build(){
            return new NetworkParcel(this);
        }
    }
}
