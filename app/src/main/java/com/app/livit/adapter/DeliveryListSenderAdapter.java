package com.app.livit.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.app.livit.utils.Constants;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.test.model.Delivery;

import com.app.livit.R;
import com.app.livit.utils.Utils;

import java.io.InputStream;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static android.content.res.Resources.getSystem;
import static com.app.livit.utils.Utils.formatDateString;

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

        int x= Resources.getSystem().getDisplayMetrics().widthPixels;
        int y = 300;
        String dimensions = Integer.toString(x)+"x"+ Integer.toString(y);

        String date = array.get(position).getCreatedAt();
        holder.tvDeliveryDate.setText(date != null ? date : "");
        String type = array.get(position).getDeliverymanVehicleType();
        if(type!= null)
            Glide.with(Utils.getContext()).load(holder.getBlueVehicleDrawable(type)).into(holder.ivDeliveryImage);

        holder.tvDeliveryPrice.setText(Utils.getContext().getString(R.string.formatted_price, Utils.toFormattedDouble(array.get(position).getTotalPrice().doubleValue())));
        holder.tvDeliveryDistance.setText(Utils.getContext().getString(R.string.formatted_distance, Utils.toFormattedDouble(array.get(position).getDistance().doubleValue())));
        holder.tvDeliverySender.setText(Utils.getContext().getString(R.string.sender_name, array.get(position).getSenderName()));
        holder.tvDeliveryWeight.setText(Utils.getContext().getString(R.string.formatted_weight, Utils.toFormattedDouble(array.get(position).getWeight().doubleValue())));

        String STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size="+dimensions+
                "&maptype=roadmap&path=";

        String marker_me = "color:blue|label:|" + array.get(position).getLatStart().doubleValue() +","+array.get(position).getLonStart().doubleValue();
        String marker_dest = "color:blue|label:|" + array.get(position).getLatEnd().doubleValue() +","+array.get(position).getLonEnd().doubleValue();
        LatLng loc = new LatLng( array.get(position).getLatStart().doubleValue() ,array.get(position).getLonStart().doubleValue());
        LatLng loc2= new LatLng(array.get(position).getLatEnd().doubleValue() ,array.get(position).getLonEnd().doubleValue());

        try {
            marker_me = URLEncoder.encode(marker_me, "UTF-8");
            marker_dest = URLEncoder.encode(marker_dest, "UTF-8");

            String path = "weight:5|color:green|"+array.get(position).getLatStart().doubleValue() +","+array.get(position).getLonStart().doubleValue() + "|"+array.get(position).getLatEnd().doubleValue() +","+array.get(position).getLonEnd().doubleValue();
            path = URLEncoder.encode(path, "UTF-8");


            String url = STATIC_MAP_API_ENDPOINT + path + "&markers=" + marker_me + "&markers=" + marker_dest +"&key=AIzaSyAZiffx6fX2_cN2z0B5XfBSy14m0-aUN-s";

            Glide
                    .with(Utils.getContext())
                    .load(url)
                    .into(holder.map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
        private ImageView map;

        DeliveryViewHolder(View itemView) {
            super(itemView);
            this.tvDeliveryDate = itemView.findViewById(R.id.tv_delivery_date);
            this.tvDeliveryPrice = itemView.findViewById(R.id.tv_delivery_price);
            this.ivDeliveryImage = itemView.findViewById(R.id.iv_delivery);
            this.tvDeliveryWeight = itemView.findViewById(R.id.tv_delivery_weight);
            this.tvDeliveryDistance = itemView.findViewById(R.id.tv_delivery_distance);
            this.tvDeliverySender = itemView.findViewById(R.id.tv_delivery_sendername);
            this.map = itemView.findViewById(R.id.mapImage);
        }

        int getBlueVehicleDrawable(String vehicleType) {
            if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_blue;
            else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_blue;
            else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_blue;
            else return R.drawable.car_blue;
        }

    }
}