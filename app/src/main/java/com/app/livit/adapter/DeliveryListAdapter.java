package com.app.livit.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.test.model.Delivery;

import com.app.livit.R;
import com.app.livit.utils.Utils;

import java.util.List;

/**
 * Created by Rémi OLLIVIER on 04/04/2018.
 */

public class DeliveryListAdapter extends RecyclerView.Adapter<DeliveryListAdapter.DeliveryViewHolder> {

    private List<Delivery> array;

    public DeliveryListAdapter (List<Delivery> array){
        this.array = array;
    }

    public void setList (List<Delivery> array) {
        this.array = array;
        notifyDataSetChanged();
    }

    public List<Delivery> getList() {
        return this.array;
    }

    /**
     * Create the view for a cell
     * @param parent the parent
     * @param viewType the view type
     * @return the view holder
     */
    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.delivery_item, parent, false);
        return new DeliveryViewHolder(v);
    }

    /**
     * This method is called when the view is binded to its corresponding data
     * @param holder the holder
     * @param position the current position of the cell in the list
     */
    @Override
    public void onBindViewHolder(@NonNull final DeliveryViewHolder holder, int position) {
        Glide.with(Utils.getContext())
                .load(array.get(position).getPicture())
                .apply(new RequestOptions().error(R.drawable.package_image).centerCrop())
                .into(holder.ivDeliveryImage);
        holder.tvDeliveryDistance.setText(Utils.getContext().getString(R.string.formatted_distance, Utils.toFormattedDouble(array.get(position).getDistance().doubleValue())));
    }

    /**
     * Get the item's id for position
     * @param position the position
     * @return the id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * This method is used to get the array's size
     * @return the size of the list
     */
    @Override
    public int getItemCount() {
        return array.size();
    }

    /**
     * View holder declaration
     */
    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDeliveryDistance;
        private TextView tvDeliveryPrice;
        private ImageView ivDeliveryImage;

        DeliveryViewHolder(View itemView) {
            super(itemView);
            this.tvDeliveryDistance = itemView.findViewById(R.id.tv_delivery_distance);
            this.tvDeliveryPrice = itemView.findViewById(R.id.tv_delivery_price);
            this.ivDeliveryImage = itemView.findViewById(R.id.iv_delivery);
        }
    }
}