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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.capstonemainproject.R;
import com.example.capstonemainproject.StationActivity;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.UserMainService;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BookmarkFragment extends Fragment {

    private static final long USER_MAIN_SERVICE_GET_BOOKMARKS = -22;
    private static final long USER_MAIN_SERVICE_DELETE_BOOKMARK = -24;

    private Context currentContext;
    private View currentView;

    private ListView listViewBookmark;
    private TextView textBookmarkFound, textBookmarkNotFound;

    private UserMainService userMainService;

    private String loginAccessToken;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentContext = context;

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }

        Log.i("BOOKMARK", "-onAttach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_bookmark, container, false);

        listViewBookmark = currentView.findViewById(R.id.listView_bookmark);
        textBookmarkFound = currentView.findViewById(R.id.textView_bookmark_found);
        textBookmarkNotFound = currentView.findViewById(R.id.textView_bookmark_notFound);

        Log.i("BOOKMARK", "-onCreateView");

        return currentView;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onResume() {
        super.onResume();
        loadUserBookmarks();

        Log.i("BOOKMARK", "-onResume");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadUserBookmarks() {
        userMainService = new UserMainService(loginAccessToken);

        try {
            CommonResponse commonResponse = userMainService.execute(USER_MAIN_SERVICE_GET_BOOKMARKS).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<UserBookmarkDto> listResultResponse = (ListResultResponse<UserBookmarkDto>) commonResponse;
                List<UserBookmarkDto> userBookmarkList = listResultResponse.getDataList();

                if (userBookmarkList.size() == 0) {
                    textBookmarkFound.setVisibility(View.GONE);
                    listViewBookmark.setVisibility(View.GONE);
                    textBookmarkNotFound.setVisibility(View.VISIBLE);

                } else {
                    textBookmarkNotFound.setVisibility(View.GONE);
                    listViewBookmark.setVisibility(View.VISIBLE);
                    textBookmarkFound.setVisibility(View.VISIBLE);

                    listViewBookmark.setAdapter(new CustomBookmarkList((Activity) currentContext, this, userBookmarkList));
                }

            } else {
                String loadUserBookmarksFailedMsg = "즐겨찾기를 불러올 수 없습니다.";

                SnackBarManager.showMessage(currentView, loadUserBookmarksFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Bookmark", "Loading user bookmarks failed.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class CustomBookmarkList extends ArrayAdapter<UserBookmarkDto> {

        private final Activity context;
        private final Fragment fragment;
        private final List<UserBookmarkDto> userBookmarkList;

        public CustomBookmarkList(Activity context, Fragment fragment, List<UserBookmarkDto> userBookmarkList) {
            super(context, R.layout.listview_bookmark, userBookmarkList);
            this.context = context;
            this.fragment = fragment;

            this.userBookmarkList =
                    userBookmarkList.stream()
                            .sorted(Comparator.comparing(UserBookmarkDto::getRegisteredAt).reversed())
                            .collect(Collectors.toList());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_bookmark, null, true);

            TextView stationName = rowView.findViewById(R.id.listView_bookmark_station_name);
            TextView chargerCount = rowView.findViewById(R.id.listView_bookmark_charger_count);
            CheckBox bookmarked = rowView.findViewById(R.id.checkbox_bookmark);

            UserBookmarkDto userBookmark = userBookmarkList.get(position);

            stationName.setText(userBookmark.getStation().getStationName());
            chargerCount.setText(String.valueOf(userBookmark.getChargerCount()));

            rowView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), StationActivity.class);
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                intent.putExtra("StationId", userBookmark.getStation().getId());
                intent.putExtra("Record", false);

                startActivity(intent);
            });

            bookmarked.setOnClickListener(v -> {
                if (!bookmarked.isChecked()) {
                    userMainService = new UserMainService(loginAccessToken);
                    userMainService.execute(USER_MAIN_SERVICE_DELETE_BOOKMARK, userBookmark.getStation().getId());

                    userBookmarkList.remove(position);

                    if (userBookmarkList.size() == 0) {
                        textBookmarkFound.setVisibility(View.GONE);
                        listViewBookmark.setVisibility(View.GONE);
                        textBookmarkNotFound.setVisibility(View.VISIBLE);

                    } else {
                        textBookmarkNotFound.setVisibility(View.GONE);
                        listViewBookmark.setVisibility(View.VISIBLE);
                        textBookmarkFound.setVisibility(View.VISIBLE);

                        listViewBookmark.setAdapter(new CustomBookmarkList(context, fragment, userBookmarkList));
                    }

                    getParentFragmentManager()
                            .beginTransaction()
                            .detach(fragment)
                            .attach(fragment)
                            .commit();
                }
            });

            return rowView;
        }
    }
}
