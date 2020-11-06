package de.htw.berlin.steganography.auth.models;

import android.util.Log;

import apis.SocialMedia;
import de.htw.berlin.steganography.auth.strategy.AuthStrategy;

public class NetworkParcel implements Comparable<NetworkParcel>{

    /**
     * Also holds AuthInformations
     */
    private final Integer id;
    private String networkName;
    private AuthStrategy authStrategy;
    private TokenInformation tokenInformation;
    private SocialMedia socialMedia;

    public NetworkParcel(Builder builder){
        this.id = IdService.getId();
        this.networkName = builder.networkName;
        this.authStrategy = builder.authStrategy;
        this.tokenInformation = builder.tokenInformation;
        this.socialMedia = builder.socialMedia;
    }

    public AuthInformation getAuthInformation(){
        return this.authStrategy.getAuthInformation();
    }

    public void setAuthInformation(AuthInformation authInformation){
        this.authStrategy.setAuthInformation(authInformation);
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public void setSocialMedia(SocialMedia socialMedia) {
        this.socialMedia = socialMedia;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public AuthStrategy getAuthStrategy() {
        return authStrategy;
    }

    public void setAuthStrategy(AuthStrategy authStrategy) {
        this.authStrategy = authStrategy;
    }

    public TokenInformation getTokenInformation() {
        return tokenInformation;
    }

    public void setTokenInformation(TokenInformation tokenInformation) {
        this.tokenInformation = tokenInformation;
    }

    public int getId(){
        return this.id;
    }

    @Override
    public int compareTo(NetworkParcel o) {
        Log.i("MYY", o.getNetworkName() + " - " + this.getNetworkName());
        return this.getId() - o.getId();
    }

    public static class Builder{
        private String networkName;
        private AuthStrategy authStrategy;
        private TokenInformation tokenInformation;
        private SocialMedia socialMedia;

        public Builder withSocialMedia(SocialMedia socialMedia){
            this.socialMedia = socialMedia;
            return this;
        }

        public Builder withNetworkName(String networkName){
            this.networkName = networkName;
            return this;
        }

        public Builder withAuthStrategy(AuthStrategy authStrategy){
            this.authStrategy = authStrategy;
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
