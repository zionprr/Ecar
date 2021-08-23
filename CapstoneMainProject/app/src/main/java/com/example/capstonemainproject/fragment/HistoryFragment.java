package com.example.capstonemainproject.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.capstonemainproject.R;
import com.example.capstonemainproject.StationActivity;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.dto.response.custom.user.UserHistoryDto;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.UserMainService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoryFragment extends Fragment {

    private static final long USER_MAIN_SERVICE_GET_HISTORIES = -21;

    private Context currentContext;
    private View currentView;

    private ListView listViewHistory;
    private TextView textHistoryFound, textHistoryNotFound;

    private UserMainService userMainService;

    private String loginAccessToken;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentContext = context;

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_history, container, false);

        listViewHistory = currentView.findViewById(R.id.listView_history);
        textHistoryFound = currentView.findViewById(R.id.textView_history_found);
        textHistoryNotFound = currentView.findViewById(R.id.textView_history_notFound);

        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserHistories();
    }

    private void loadUserHistories() {
        userMainService = new UserMainService(loginAccessToken);

        try {
            CommonResponse commonResponse = userMainService.execute(USER_MAIN_SERVICE_GET_HISTORIES).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<UserHistoryDto> listResultResponse = (ListResultResponse<UserHistoryDto>) commonResponse;
                List<UserHistoryDto> userHistoryList = listResultResponse.getDataList();

                if (userHistoryList.size() == 0) {
                    textHistoryFound.setVisibility(View.GONE);
                    listViewHistory.setVisibility(View.GONE);
                    textHistoryNotFound.setVisibility(View.VISIBLE);

                } else {
                    textHistoryNotFound.setVisibility(View.GONE);
                    listViewHistory.setVisibility(View.VISIBLE);
                    textHistoryFound.setVisibility(View.VISIBLE);

                    listViewHistory.setAdapter(new CustomHistoryList((Activity) currentContext, userHistoryList));
                }

            } else {
                String loadUserHistoriesFailedMsg = "최근 검색 목록을 불러올 수 없습니다.";

                SnackBarManager.showMessage(currentView, loadUserHistoriesFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("History", "Loading user histories failed.");
        }
    }

    private class CustomHistoryList extends ArrayAdapter<UserHistoryDto> {

        private final Activity context;
        private final List<UserHistoryDto> userHistoryList;

        public CustomHistoryList(Activity context, List<UserHistoryDto> userHistoryList) {
            super(context, R.layout.listview_history, userHistoryList);
            this.context = context;

            this.userHistoryList =
                    userHistoryList.stream()
                            .sorted(Comparator.comparing(UserHistoryDto::getSearchedAt).reversed())
                            .collect(Collectors.toList());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_history, null, true);

            TextView stationName = rowView.findViewById(R.id.listView_history_station_name);
            TextView chargerCount = rowView.findViewById(R.id.listView_history_charger_count);
            TextView date = rowView.findViewById(R.id.listView_history_date);

            UserHistoryDto userHistory = userHistoryList.get(position);

            stationName.setText(userHistory.getStation().getStationName());
            chargerCount.setText(String.valueOf(userHistory.getChargerCount()));
            date.setText(userHistory.getSearchedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

            rowView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), StationActivity.class);
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                intent.putExtra("StationId", userHistory.getStation().getId());
                intent.putExtra("Record", false);

                startActivity(intent);
            });

            return rowView;
        }
    }
}
