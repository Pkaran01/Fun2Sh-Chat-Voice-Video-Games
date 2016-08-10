package com.ss.fun2sh.ui.fragments.search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.adapters.search.SearchViewPagerAdapter;
import com.ss.fun2sh.utils.KeyboardUtils;

import butterknife.Bind;

public class SearchFragment extends BaseLoggableActivity implements SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

    @Bind(R.id.search_viewpager)
    ViewPager searchViewPager;

    @Bind(R.id.search_radiogroup)
    RadioGroup searchRadioGroup;

    private SearchViewPagerAdapter searchViewPagerAdapter;

    @Override
    protected int getContentResId() {
        return R.layout.fragment_search;
    }

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, SearchFragment.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewPagerAdapter();
        initCustomListeners();
        setUpActionBarWithUpButton();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;

        if (searchMenuItem != null) {
            searchView = (SearchView) searchMenuItem.getActionView();
            MenuItemCompat.expandActionView(searchMenuItem);
            MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String searchQuery) {
        KeyboardUtils.hideKeyboard(this);
        search(searchQuery);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchQuery) {
        search(searchQuery);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KeyboardUtils.hideKeyboard(this);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        cancelSearch();
        launchDialogsListFragment();
        return true;
    }

    private void initViewPagerAdapter() {
        searchViewPagerAdapter = new SearchViewPagerAdapter(getSupportFragmentManager());
        searchViewPager.setAdapter(searchViewPagerAdapter);
        searchViewPager.setOnPageChangeListener(new PageChangeListener());
        searchRadioGroup.check(R.id.local_search_radiobutton);
    }

    private void initCustomListeners() {
        searchRadioGroup.setOnCheckedChangeListener(new RadioGroupListener());
    }

    private void launchDialogsListFragment() {
        this.finish();
    }

    private void search(String searchQuery) {
        if (searchViewPagerAdapter != null && searchViewPager != null) {
            searchViewPagerAdapter.search(searchViewPager.getCurrentItem(), searchQuery);
        }
    }

    private void cancelSearch() {
        if (searchViewPagerAdapter != null && searchViewPager != null) {
            searchViewPagerAdapter.cancelSearch(searchViewPager.getCurrentItem());
        }
    }

    private class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch (checkedId) {
                case R.id.local_search_radiobutton:
                    searchViewPager.setCurrentItem(SearchViewPagerAdapter.LOCAl_SEARCH);
                    searchViewPagerAdapter.prepareSearch(SearchViewPagerAdapter.LOCAl_SEARCH);
                    break;
                case R.id.global_search_radiobutton:
                    searchViewPager.setCurrentItem(SearchViewPagerAdapter.GLOBAL_SEARCH);
                    searchViewPagerAdapter.prepareSearch(SearchViewPagerAdapter.GLOBAL_SEARCH);
                    break;
            }
        }
    }

    private class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case SearchViewPagerAdapter.LOCAl_SEARCH:
                    searchRadioGroup.check(R.id.local_search_radiobutton);
                    break;
                case SearchViewPagerAdapter.GLOBAL_SEARCH:
                    searchRadioGroup.check(R.id.global_search_radiobutton);
                    break;
            }
        }
    }
}