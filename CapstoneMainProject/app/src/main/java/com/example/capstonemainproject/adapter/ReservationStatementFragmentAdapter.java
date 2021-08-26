package com.example.capstonemainproject.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReservationStatementFragmentAdapter extends FragmentStateAdapter {

    private List<Fragment> fragmentItems;

    public ReservationStatementFragmentAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        fragmentItems = new ArrayList<>();
    }

    @Override
    public Fragment createFragment(int position) {
        return fragmentItems.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentItems.size();
    }

    public void addFragment(Fragment fragment) {
        fragmentItems.add(fragment);
    }
}
