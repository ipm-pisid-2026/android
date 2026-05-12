package com.maze;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.maze.models.HighValue;
import com.maze.models.SoundData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
public class MazeSoundFragment extends Fragment {

    private static final String ARG_HOST = "host";
    private static final String ARG_DATABASE = "database";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_PASSWORD = "password";

    private String host;
    private String database;
    private String username;
    private String password;
    private LineChart lineChart;
    private OkHttpClient client;

    public MazeSoundFragment() {
        // Required empty public constructor
    }

    public static MazeSoundFragment newInstance(String host, String database, String username, String password) {
        MazeSoundFragment fragment = new MazeSoundFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOST, host);
        args.putString(ARG_DATABASE, database);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            host = getArguments().getString(ARG_HOST);
            database = getArguments().getString(ARG_DATABASE);
            username = getArguments().getString(ARG_USERNAME);
            password = getArguments().getString(ARG_PASSWORD);
        }
        client = new OkHttpClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maze_sound, container, false);
        lineChart = view.findViewById(R.id.lineChartSound);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChart();
        fetchSoundData();
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
    }

    private void fetchSoundData() {
        String finalUrl = ApiConfig.buildEndpoint(host, "/api/sound") + "?limit=1000";
        Request request = new Request.Builder()
                .url(finalUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erro de conexão ao buscar dados de som: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
                Log.e("MazeSound", "Erro na requisição de som: " + e.getMessage());
            }


            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();

                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (jsonResponse.optBoolean("success")) {
                            String responseData2 = jsonResponse.getJSONArray("data").toString();
                            Gson gson = new Gson();
                            Type soundListType = new TypeToken<List<SoundData>>(){}.getType();
                            List<SoundData> soundDataList = gson.fromJson(responseData2, soundListType);

                            if (soundDataList != null && !soundDataList.isEmpty()) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> updateChart(soundDataList));
                                }
                            } else {
                                Log.w("MazeSound", "Lista de sons vazia.");
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "Nenhum dado de som encontrado.", Toast.LENGTH_SHORT).show());
                                }
                            }
                        } else {
                            String msgErro = jsonResponse.optString("message", "Erro desconhecido no servidor");
                            Log.e("MazeSound", "Erro PHP: " + msgErro);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), msgErro, Toast.LENGTH_LONG).show());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MazeSound", "Erro ao parsear JSON: " + e.getMessage());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Erro no processamento dos dados.", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No body";
                    Log.e("MazeSound", "Erro " + response.code() + ": " + errorBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Erro do servidor: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void updateChart(List<SoundData> soundDataList) {
        lineChart.clear();
        lineChart.invalidate();

        if (soundDataList == null || soundDataList.isEmpty()) {
            lineChart.setNoDataText("Nenhum dado de som encontrado para exibir.");
            lineChart.invalidate();
            return;
        }

        ArrayList<Entry> soundEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < soundDataList.size(); i++) {
            SoundData data = soundDataList.get(i);
            soundEntries.add(new Entry(i, data.getValue()));
            labels.add(data.getHora());
        }

        if (soundEntries.isEmpty()) {
            lineChart.setNoDataText("Erro: Dados de som inválidos para exibir após processamento.");
            lineChart.invalidate();
            return;
        }

        LineDataSet soundDataSet = new LineDataSet(soundEntries, "Sound Value");
        soundDataSet.setColor(Color.BLUE);
        soundDataSet.setDrawCircles(true);
        soundDataSet.setDrawValues(false);
        soundDataSet.setLineWidth(2f);
        soundDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(soundDataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(soundEntries.size() - 0.5f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 8), false);

        lineChart.invalidate();
    }
}