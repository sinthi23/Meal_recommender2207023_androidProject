package com.example.mealrecmmenderandroid.activities.consumer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ActivityAnalyticsBinding;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private ActivityAnalyticsBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private List<CookHistory> cookHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        cookHistoryList = new ArrayList<>();

        setupToolbar();
        loadCookingData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCookingData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String userId = sessionManager.getUserId();

        firebaseHelper.getUserCookHistoryRef(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cookHistoryList.clear();
                        for (DataSnapshot historySnapshot : snapshot.getChildren()) {
                            CookHistory history = historySnapshot.getValue(CookHistory.class);
                            if (history != null) {
                                cookHistoryList.add(history);
                            }
                        }

                        updateStatistics();
                        setupMostCookedMealsChart();
                        setupCaloriesTrendChart();

                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void updateStatistics() {
        int thisWeekCount = 0;
        int thisMonthCount = 0;

        Calendar calendar = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();

        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long weekStart = calendar.getTimeInMillis();

        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long monthStart = calendar.getTimeInMillis();

        for (CookHistory history : cookHistoryList) {
            long timestamp = history.getTimestamp();
            if (timestamp >= weekStart) {
                thisWeekCount++;
            }
            if (timestamp >= monthStart) {
                thisMonthCount++;
            }
        }

        binding.thisWeekCountTextView.setText(String.valueOf(thisWeekCount));
        binding.thisMonthCountTextView.setText(String.valueOf(thisMonthCount));
    }

    private void setupMostCookedMealsChart() {
        Map<String, Integer> mealCounts = new HashMap<>();

        for (CookHistory history : cookHistoryList) {
            String recipeName = history.getRecipeName();
            if (recipeName != null) {
                mealCounts.put(recipeName, mealCounts.getOrDefault(recipeName, 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> sortedMeals = new ArrayList<>(mealCounts.entrySet());
        sortedMeals.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (sortedMeals.isEmpty()) {
            binding.mostCookedChart.setVisibility(View.GONE);
            binding.noMostCookedDataTextView.setVisibility(View.VISIBLE);
            return;
        }

        binding.mostCookedChart.setVisibility(View.VISIBLE);
        binding.noMostCookedDataTextView.setVisibility(View.GONE);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int limit = Math.min(5, sortedMeals.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedMeals.get(i);
            entries.add(new BarEntry(i, entry.getValue()));

            String name = entry.getKey();
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
            }
            labels.add(name);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Times Cooked");
        dataSet.setColors(new int[]{
                Color.parseColor("#4CAF50"),
                Color.parseColor("#66BB6A"),
                Color.parseColor("#81C784"),
                Color.parseColor("#A5D6A7"),
                Color.parseColor("#C8E6C9")
        });
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        binding.mostCookedChart.setData(barData);
        binding.mostCookedChart.setFitBars(true);
        binding.mostCookedChart.animateY(1000, Easing.EaseInOutQuad);

        Description description = new Description();
        description.setText("");
        binding.mostCookedChart.setDescription(description);

        XAxis xAxis = binding.mostCookedChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = binding.mostCookedChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);

        binding.mostCookedChart.getAxisRight().setEnabled(false);
        binding.mostCookedChart.getLegend().setEnabled(false);

        binding.mostCookedChart.invalidate();
    }

    private void setupCaloriesTrendChart() {
        Calendar calendar = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();

        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long sevenDaysAgo = calendar.getTimeInMillis();

        Map<String, Integer> dailyCalories = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            calendar.setTimeInMillis(currentTime);
            calendar.add(Calendar.DAY_OF_YEAR, -6 + i);
            String dateKey = dateFormat.format(calendar.getTime());
            dailyCalories.put(dateKey, 0);
        }

        for (CookHistory history : cookHistoryList) {
            long timestamp = history.getTimestamp();
            if (timestamp >= sevenDaysAgo) {
                calendar.setTimeInMillis(timestamp);
                String dateKey = dateFormat.format(calendar.getTime());
                int currentCalories = dailyCalories.getOrDefault(dateKey, 0);
                dailyCalories.put(dateKey, currentCalories + history.getCalories());
            }
        }

        if (dailyCalories.isEmpty() || dailyCalories.values().stream().allMatch(v -> v == 0)) {
            binding.caloriesTrendChart.setVisibility(View.GONE);
            binding.noCaloriesDataTextView.setVisibility(View.VISIBLE);
            return;
        }

        binding.caloriesTrendChart.setVisibility(View.VISIBLE);
        binding.noCaloriesDataTextView.setVisibility(View.GONE);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (int i = 0; i < 7; i++) {
            calendar.setTimeInMillis(currentTime);
            calendar.add(Calendar.DAY_OF_YEAR, -6 + i);
            String dateKey = dateFormat.format(calendar.getTime());
            int calories = dailyCalories.getOrDefault(dateKey, 0);

            entries.add(new Entry(index, calories));
            labels.add(dateKey);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Calories");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFE0B2"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);

        binding.caloriesTrendChart.setData(lineData);
        binding.caloriesTrendChart.animateX(1000, Easing.EaseInOutQuad);

        Description description = new Description();
        description.setText("");
        binding.caloriesTrendChart.setDescription(description);

        XAxis xAxis = binding.caloriesTrendChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        YAxis leftAxis = binding.caloriesTrendChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        binding.caloriesTrendChart.getAxisRight().setEnabled(false);
        binding.caloriesTrendChart.getLegend().setEnabled(false);

        binding.caloriesTrendChart.invalidate();
    }
}