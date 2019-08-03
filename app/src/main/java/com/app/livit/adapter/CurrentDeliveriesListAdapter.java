package com.app.livit.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.livit.R;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.test.model.Delivery;

import java.util.List;

/**
 * Created by Rémi OLLIVIER on 04/04/2018.
 */

public class CurrentDeliveriesListAdapter extends RecyclerView.Adapter<CurrentDeliveriesListAdapter.DeliveryViewHolder> {

    private List<Delivery> array;

    public CurrentDeliveriesListAdapter (List<Delivery> array) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_deliveries_item, parent, false);
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
        holder.tvDeliveryStatus.setText(getDisplayStatusString(array.get(position).getDeliveryStatus()));
        holder.tvDeliveryPrice.setText(Utils.getContext().getString(R.string.formatted_price, Utils.toFormattedDouble(array.get(position).getTotalPrice().doubleValue())));
        holder.tvDeliveryDistance.setText(Utils.getContext().getString(R.string.formatted_distance, Utils.toFormattedDouble(array.get(position).getDistance().doubleValue())));
        holder.tvDeliveryDate.setText(Utils.formatDateString(array.get(position).getCreatedAt()));
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
        private TextView tvDeliveryDistance;
        private TextView tvDeliveryStatus;
        private ImageView ivDeliveryImage;

        DeliveryViewHolder(View itemView) {
            super(itemView);
            this.tvDeliveryPrice = itemView.findViewById(R.id.tv_delivery_price);
            this.tvDeliveryDistance = itemView.findViewById(R.id.tv_delivery_address);
            this.tvDeliveryStatus = itemView.findViewById(R.id.tv_delivery_status);
            this.tvDeliveryDate = itemView.findViewById(R.id.tv_delivery_date);
            this.ivDeliveryImage = itemView.findViewById(R.id.iv_delivery);
        }
    }

    /**
     * This method is used to transform the delivery's status from backend language to user's language
     * @param status the backend's status
     * @return the user-readable status
     */
    private int getDisplayStatusString(String status) {
        switch (status) {
            case Constants.DELIVERYSTATUS_CREATED:
                return R.string.created;
            case Constants.DELIVERYSTATUS_PAID:
                return R.string.paid;
            case Constants.DELIVERYSTATUS_ACCEPTED:
                return R.string.accepted;
            case Constants.DELIVERYSTATUS_PICKEDUP:
                return R.string.pickedup;
            case Constants.DELIVERYSTATUS_DELIVERED:
                return R.string.droppedoff;
            case Constants.DELIVERYSTATUS_CANCELED:
                return R.string.canceled;
            default:
                return R.string.no_status;
        }
    }
}