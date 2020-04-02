package com.nullbyte.covid_19support.ui.country_stat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.nullbyte.covid_19support.LoaderDialog;
import com.nullbyte.covid_19support.LoaderDialogPunchCorona;
import com.nullbyte.covid_19support.R;
import com.nullbyte.covid_19support.api.CasesByCountryDateAPI;
import com.nullbyte.covid_19support.api.SearchByCountryAPI;
import com.nullbyte.covid_19support.databinding.FragmentCountryStatBinding;
import com.nullbyte.covid_19support.utilities.GraphUtility;
import com.nullbyte.covid_19support.utilities.ISOCodeUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class CountryStatFragment extends Fragment {

    private CountryStatViewModel mViewModel;
    private FragmentCountryStatBinding mCountryStatBinding;
    private ArrayList<String> mDateListTotal;
    private ArrayList<String> mCasesListTotal;
    private ArrayList<String> mDeceasedListTotal;
    private ArrayList<String> mRecoveredListTotal;
    private String countryName;
    private Long mDeceased, mRecovered;
    private DialogFragment dialogFragment;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(CountryStatViewModel.class);
        mCountryStatBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_country_stat, container, false);
        mCountryStatBinding.setCountryStatViewModel(mViewModel);

        initViews();


        mCountryStatBinding.tvCountryName.setText(countryName);
        mCountryStatBinding.toolbarCountryStat.setNavigationOnClickListener(view -> Objects.requireNonNull(getActivity()).onBackPressed());
        mCountryStatBinding.countryRefreshLayout.setOnRefreshListener(() -> getCountryStat(countryName));
        getCountryStat(countryName);
        getCountryDateWiseData();

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        Fragment prev = getParentFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment = new LoaderDialogPunchCorona();
        dialogFragment.show(ft, "dialog");


        return mCountryStatBinding.getRoot();
    }

    private void getCountryStat(String countryName) {
        SearchByCountryAPI searchByCountryAPI = new SearchByCountryAPI(countryName, data -> {
            if (data == null) {
                Snackbar snackbar = Snackbar.make(mCountryStatBinding.getRoot(), "Please check your network connection", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundTintList(ContextCompat.getColorStateList(Objects.requireNonNull(getActivity()), R.color.red));
                snackbar.show();
                mCountryStatBinding.countryRefreshLayout.setRefreshing(false);
            } else {
                int splitPoint = 0;
                for (int i = 0; i < data.length(); i++) {
                    if (data.charAt(i) == '[')
                        splitPoint = i;
                }
                data = data.substring(splitPoint, data.length() - 1);
                try {
                    JSONArray jsonArr = new JSONArray(data);
                    for (int i = 0; i < data.length(); ++i) {
                        JSONObject dataObject = jsonArr.getJSONObject(i);
                        mCountryStatBinding.tvTotalCasesCount.setText(dataObject.getString("total_cases"));
                        mCountryStatBinding.tvTotalDeceasedCount.setText(dataObject.getString("total_deaths"));
                        mCountryStatBinding.tvTotalRecoveredCount.setText(dataObject.getString("total_recovered"));
                        mCountryStatBinding.tvNewCasesCount.setText(dataObject.getString("new_cases"));
                        mCountryStatBinding.tvNewDeceasedCount.setText(dataObject.getString("new_deaths"));
                        mCountryStatBinding.tvActiveCount.setText(dataObject.getString("active_cases"));
                        mCountryStatBinding.tvCasesPerMillionCount.setText(dataObject.getString("total_cases_per1m"));
                        mCountryStatBinding.countryRefreshLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        searchByCountryAPI.execute();
    }

    private void getCountryDateWiseData() {
        String countryCode = ISOCodeUtility.getIsoCode(countryName);
        CasesByCountryDateAPI casesByCountryDateAPI = new CasesByCountryDateAPI(countryCode, data -> {
            if (data == null) {
                Snackbar snackbar = Snackbar.make(mCountryStatBinding.getRoot(), "Please check your network connection", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundTintList(ContextCompat.getColorStateList(Objects.requireNonNull(getActivity()), R.color.red));
                snackbar.show();
                mCountryStatBinding.countryRefreshLayout.setRefreshing(false);
            } else {
                int splitPoint = 0;
                for (int i = 1; i < data.length(); i++) {
                    if (data.charAt(i) == '{') {
                        splitPoint = i;
                        break;
                    }
                }
                data = data.substring(splitPoint, data.length() - 1);
                try {
                    Log.i("DataTry", data);
                    JSONObject response = new JSONObject(data);
                    Iterator<String> keys = response.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        mDateListTotal.add(key);
                        JSONObject value = response.getJSONObject(key);
                        mCasesListTotal.add(value.getString("confirmed"));
                        mRecoveredListTotal.add(value.getString("recovered"));
                        mDeceasedListTotal.add(value.getString("deaths"));
                        Log.i("Cases", value.getString("confirmed"));
                        Log.i("Recovered", value.getString("recovered"));
                        Log.i("Deceased", value.getString("deaths"));
                    }
                } catch (JSONException e) {
                    Log.i("Catch", String.valueOf(e));
                    e.printStackTrace();
                }

                dialogFragment.dismiss();
                drawGraphs();

            }

            Log.i("Date", mDateListTotal.toString());
            Log.i("Cases", mCasesListTotal.toString());
            Log.i("Recovered", mRecoveredListTotal.toString());
            Log.i("Deceased", mDeceasedListTotal.toString());

        });
        casesByCountryDateAPI.execute();
    }

    private void drawGraphs() {
        Log.i("anant", "pie");
        drawPie();
        drawGraphforTotalCases();
        drawGraphForDeath();
        drawGraphForRecovered();
    }

    private void drawPie() {

        mDeceased = Long.valueOf(mDeceasedListTotal.get(mDeceasedListTotal.size() - 1));
        mRecovered = Long.valueOf(mRecoveredListTotal.get(mRecoveredListTotal.size() - 1));
        PieChart mPieChart = mCountryStatBinding.piechartoverall;
        ArrayList<PieEntry> sessDataPie1 = new ArrayList<>();
        Log.i("anant", mDeceased + " " + mRecovered);
        sessDataPie1.add(new PieEntry(mRecovered, "Recovered"));
        sessDataPie1.add(new PieEntry(mDeceased, "Deceased"));
        GraphUtility.piechart(mPieChart, sessDataPie1);
        mPieChart.setVisibility(View.VISIBLE);
    }

    private void drawGraphforTotalCases() {
        LineChart lineChart = mCountryStatBinding.lineChart1;
        LineDataSet lineDataSet = new LineDataSet(getDataforTotalCases(), "Total Cases");
        lineDataSet.setColor(ContextCompat.getColor(requireActivity(), R.color.primary));
        lineDataSet.setValueTextColor(ContextCompat.getColor(requireActivity(), R.color.design_default_color_primary_dark));
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        String[] months = new String[mDateListTotal.size()];
        months = mDateListTotal.toArray(months);
        String[] xAsisValue = months;

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return xAsisValue[(int) value];
            }
        };
        xAxis.setGranularity(0f);
        xAxis.setValueFormatter(formatter);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        xAxis.setLabelRotationAngle(-90);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);

        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
        data.setDrawValues(false);
        lineChart.animateX(2500);
        lineChart.invalidate();

    }

    private ArrayList<Entry> getDataforTotalCases() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < mDateListTotal.size(); i++) {
            entries.add(new Entry((float) i, Float.parseFloat(mCasesListTotal.get(i))));
        }
        Log.i("yaxis", entries.toString());

//        entries.add(new Entry(1f, 10f));
//        entries.add(new Entry(2f, 20f));
//        entries.add(new Entry(3f, 40f));
        return entries;
    }

    private void drawGraphForDeath() {
        LineChart lineChart = mCountryStatBinding.lineChart2;
        LineDataSet lineDataSet = new LineDataSet(getDataforTotalDeath(), "Deceased");
        lineDataSet.setColor(ContextCompat.getColor(requireActivity(), R.color.orange));
        lineDataSet.setValueTextColor(ContextCompat.getColor(requireActivity(), R.color.design_default_color_primary_dark));
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        String[] months = new String[mDateListTotal.size()];
        months = mDateListTotal.toArray(months);
        String[] xAsisValue = months;

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return xAsisValue[(int) value];
            }
        };
        xAxis.setGranularity(0f);
        xAxis.setValueFormatter(formatter);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        xAxis.setLabelRotationAngle(-90);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);

        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
        data.setDrawValues(false);
        lineChart.animateX(2500);
        lineChart.invalidate();

    }

    private ArrayList<Entry> getDataforTotalDeath() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < mDateListTotal.size(); i++) {
            entries.add(new Entry((float) i, Float.parseFloat(mDeceasedListTotal.get(i))));
        }
        Log.i("yaxis", entries.toString());
        return entries;
    }

    private void drawGraphForRecovered() {
        LineChart lineChart = mCountryStatBinding.lineChart3;
        LineDataSet lineDataSet = new LineDataSet(getDataforTotalRecovered(), "Recovered");
        lineDataSet.setColor(ContextCompat.getColor(requireActivity(), R.color.blue));
        lineDataSet.setValueTextColor(ContextCompat.getColor(requireActivity(), R.color.design_default_color_primary_dark));
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        String[] months = new String[mDateListTotal.size()];
        months = mDateListTotal.toArray(months);
        String[] xAsisValue = months;

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return xAsisValue[(int) value];
            }
        };
        xAxis.setGranularity(0f);
        xAxis.setValueFormatter(formatter);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        xAxis.setLabelRotationAngle(-90);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);

        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
        data.setDrawValues(false);
        lineChart.animateX(2500);
        lineChart.invalidate();

    }

    private ArrayList<Entry> getDataforTotalRecovered() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < mDateListTotal.size(); i++) {
            entries.add(new Entry((float) i, Float.parseFloat(mRecoveredListTotal.get(i))));
        }
        Log.i("yaxis", entries.toString());
        return entries;
    }

    private void initViews() {
        countryName = getTag();
        mCasesListTotal = new ArrayList<>();
        mRecoveredListTotal = new ArrayList<>();
        mDeceasedListTotal = new ArrayList<>();
        mDateListTotal = new ArrayList<>();
    }

}
