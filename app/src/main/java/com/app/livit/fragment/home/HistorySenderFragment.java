package com.app.livit.fragment.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.model.Delivery;

import com.app.livit.R;
import com.app.livit.activity.MainActivity;
import com.app.livit.adapter.DeliveryListSenderAdapter;
import com.app.livit.event.delivery.GetClosedDeliveriesAsSenderFailureEvent;
import com.app.livit.event.delivery.GetClosedDeliveriesAsSenderSuccessEvent;
import com.app.livit.fragment.DeliveryDetailsFragment;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.RecyclerTouchListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Rémi OLLIVIER on 25/06/2018.
 */

public class HistorySenderFragment extends Fragment {
    private TextView tvEmptyList;
    private RecyclerView rvHistory;
    private List<Delivery> deliveryList;
    private SwipeRefreshLayout srlHistory;

    public static HistorySenderFragment newInstance() {

        HistorySenderFragment fragment = new HistorySenderFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_history_sender, container, false);

        this.rvHistory = view.findViewById(R.id.rv_history_sender);
        this.tvEmptyList = view.findViewById(R.id.tv_history_empty);
        this.srlHistory = view.findViewById(R.id.srl_history_sender);
        this.srlHistory.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        if (getActivity() != null)
            ((MainActivity) getActivity()).changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());

        return view;
    }

    /**
     * Lifecycle event methods
     */
    @Override
    public void onResume() {
        super.onResume();
        refreshList();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * This method refreshes the history list for the deliveryman's deliveries
     */
    private void refreshList() {
        srlHistory.setRefreshing(true);
        new DeliveryService().getMyClosedDeliveriesAsSender();
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(GetClosedDeliveriesAsSenderSuccessEvent event) {
        Log.d("HistorySender", "GetDeliveriesSuccessEvent");
        this.deliveryList = event.getDeliveries();
        this.srlHistory.setRefreshing(false);
        if (this.deliveryList.isEmpty()) {
            this.rvHistory.setVisibility(View.GONE);
            this.tvEmptyList.setVisibility(View.VISIBLE);
            this.tvEmptyList.setText(R.string.empty_history);
            Log.d("onEvent", "GetDeliveriesSuccessEvent empty");
        } else {
            this.rvHistory.setVisibility(View.VISIBLE);
            this.tvEmptyList.setVisibility(View.GONE);
            this.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
            this.rvHistory.setAdapter(new DeliveryListSenderAdapter(this.deliveryList));
            this.rvHistory.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), this.rvHistory, new RecyclerTouchListener.ClickListener() {

                @Override
                public void onClick(View view, int position) {
                    if (getActivity() != null)
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, DeliveryDetailsFragment.newInstance(deliveryList.get(position).getDeliveryID())).addToBackStack(null).commit();
                }

                @Override
                public void onLongClick(View view, int position) {
                    //empty
                }
            }));
            Log.d("onEvent", "GetDeliveriesSuccessEvent not empty");
        }
    }

    @Subscribe
    public void onEvent(GetClosedDeliveriesAsSenderFailureEvent event) {
        this.srlHistory.setRefreshing(false);
        this.rvHistory.setVisibility(View.GONE);
        this.tvEmptyList.setVisibility(View.VISIBLE);
        this.tvEmptyList.setText(R.string.error_getting_deliveries);
    }
}
