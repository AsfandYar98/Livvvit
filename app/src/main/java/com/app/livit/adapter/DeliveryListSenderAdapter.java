package com.app.livit.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.test.model.Delivery;

import com.app.livit.R;
import com.app.livit.utils.Utils;

import java.util.List;

/**
 * Created by Rémi OLLIVIER on 14/06/2018.
 */

public class DeliveryListSenderAdapter extends RecyclerView.Adapter<DeliveryListSenderAdapter.DeliveryViewHolder> {

    private List<Delivery> array;

    public DeliveryListSenderAdapter (List<Delivery> array){
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_delivery_list_item, parent, false);
        return new DeliveryViewHolder(v);
    }

    /**
     * This method is called when the view is binded to its corresponding data
     * @param holder the holder
     * @param position the current position of the cell in the list
     */
    @Override
    public void onBindViewHolder(@NonNull final DeliveryViewHolder holder, int position) {
        String date = Utils.formatDateString(array.get(position).getCreatedAt());
        holder.tvDeliveryDate.setText(date != null ? date : "");
        Glide.with(Utils.getContext()).load(array.get(position).getPicture()).into(holder.ivDeliveryImage);
        holder.tvDeliveryPrice.setText(Utils.getContext().getString(R.string.formatted_price, Utils.toFormattedDouble(array.get(position).getTotalPrice().doubleValue())));
        holder.tvDeliveryDistance.setText(Utils.getContext().getString(R.string.formatted_distance, Utils.toFormattedDouble(array.get(position).getDistance().doubleValue())));
        holder.tvDeliverySender.setText(Utils.getContext().getString(R.string.sender_name, array.get(position).getSenderName()));
        holder.tvDeliveryWeight.setText(Utils.getContext().getString(R.string.formatted_weight, Utils.toFormattedDouble(array.get(position).getWeight().doubleValue())));
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
        private TextView tvDeliveryDate;
        private TextView tvDeliveryPrice;
        private TextView tvDeliverySender;
        private TextView tvDeliveryDistance;
        private TextView tvDeliveryWeight;
        private ImageView ivDeliveryImage;

        DeliveryViewHolder(View itemView) {
            super(itemView);
            this.tvDeliveryDate = itemView.findViewById(R.id.tv_delivery_date);
            this.tvDeliveryPrice = itemView.findViewById(R.id.tv_delivery_price);
            this.ivDeliveryImage = itemView.findViewById(R.id.iv_delivery);
            this.tvDeliveryWeight = itemView.findViewById(R.id.tv_delivery_weight);
            this.tvDeliveryDistance = itemView.findViewById(R.id.tv_delivery_distance);
            this.tvDeliverySender = itemView.findViewById(R.id.tv_delivery_sendername);
        }
    }
}