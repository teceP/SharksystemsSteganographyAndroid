package de.htw.berlin.steganography.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import de.htw.berlin.steganography.MainActivity;
import de.htw.berlin.steganography.auth.constants.Constants;
import de.htw.berlin.steganography.R;
import de.htw.berlin.steganography.auth.models.TokenInformation;

public class NetworkListAdapter extends RecyclerView.Adapter<NetworkListAdapter.ViewHolder> {

    private List<TokenInformation> list;

    public NetworkListAdapter(List<TokenInformation> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textView;
        private FloatingActionButton refreshBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.networkIcon);
            textView = itemView.findViewById(R.id.networkText);
            refreshBtn = itemView.findViewById(R.id.refreshTokenBtnn);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.network_card_item, parent, false);
        return new NetworkListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TokenInformation curr = list.get(position);
        switch (curr.getNetwork().toLowerCase()) {
            case "reddit":
                holder.imageView.setImageDrawable(holder.imageView.getContext().getResources().getDrawable(R.drawable.ic_reddit_ico));
                break;
            case "imgur":
                holder.imageView.setImageDrawable(holder.imageView.getContext().getResources().getDrawable(R.drawable.ic_imgur_ico));
                break;
            case "twitter":
                holder.imageView.setImageDrawable(holder.imageView.getContext().getResources().getDrawable(R.drawable.ic_twitter_ico));
                break;
            case "youtube":
                holder.imageView.setImageDrawable(holder.imageView.getContext().getResources().getDrawable(R.drawable.ic_youtube_ico));
                break;
            case "instagram":
                holder.imageView.setImageDrawable(holder.imageView.getContext().getResources().getDrawable(R.drawable.ic_instagram_ico));
                break;
        }
        this.setTextView(curr, holder);
        this.setRefreshButton(curr.getNetwork(), holder);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void setRefreshButton(String network, ViewHolder holder){
        holder.refreshBtn.setOnClickListener(MainActivity.getMainActivityInstance().doRefreshOnClick(network));
    }

    private void setTextView(TokenInformation curr, ViewHolder holder) {
        if (!curr.getAccessToken().equals(Constants.NO_RESULT) && !this.tokenExpired(curr.getAccessTokenTimestamp())) {
            holder.textView.setText("Has valid access token:\n" + this.cut(curr.getAccessToken())
                    + "\nTime till expiration: " + (60 - this.getTimeDifferent(curr.getAccessTokenTimestamp())) + " minutes.");
        } else if (!curr.getAccessToken().equals(Constants.NO_RESULT) && this.tokenExpired(curr.getAccessTokenTimestamp())) {
            holder.textView.setText("Access token has expired.\nRefresh your token.");
        } else {
            holder.textView.setText("No access token found.\nYou have to authorize first.");
        }
    }

    public String cut(String s){
        if(s.length() > 30){
            return s.substring(0, 29) + "...";
        }
        return s;
    }

    private boolean tokenExpired(long l) {
        if ((this.getTimeDifferent(l) > Constants.ONE_HOUR_IN_MINS) || l == -1) {
            return true;
        }
        return false;
    }

    public double getTimeDifferent(long l) {
        return (double) (((System.currentTimeMillis() - l) / 1000) / 60);
    }
}
