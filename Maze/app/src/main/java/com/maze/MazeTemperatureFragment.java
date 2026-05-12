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
import com.maze.models.TempData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MazeTemperatureFragment extends Fragment {

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

    public MazeTemperatureFragment() {
        // Required empty public constructor
    }

    public static MazeTemperatureFragment newInstance(String host, String database, String username, String password) {
        MazeTemperatureFragment fragment = new MazeTemperatureFragment();
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
        View view = inflater.inflate(R.layout.fragment_maze_temperature, container, false);
        lineChart = view.findViewById(R.id.lineChartTemperature);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChart();
        fetchTemperatureData();
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
        leftAxis.setAxisMinimum(0f); // Inicia o eixo Y em 0
        //leftAxis.setAxisMaximum(someValue); // Pode definir um máximo, ou deixar o chart auto-escalar
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);

        lineChart.setNoDataText("A carregar dados de temperatura..."); // Mensagem inicial
    }

    private void fetchTemperatureData() {
        String finalUrl = ApiConfig.buildEndpoint(host, "/api/temperature") + "?limit=1000";
        Request request = new Request.Builder()
                .url(finalUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erro de conexão ao buscar dados de temperatura: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        lineChart.setNoDataText("Erro de conexão ao carregar dados.");
                        lineChart.invalidate();
                    });
                }
                Log.e("MazeTemp", "Erro na requisição de temperatura: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String responseData2 = jsonResponse.getJSONArray("data").toString();
                        Gson gson = new Gson();
                        Type tempListType = new TypeToken<List<TempData>>(){}.getType();
                        List<TempData> tempDataList = gson.fromJson(responseData2, tempListType);

                        if (tempDataList != null && !tempDataList.isEmpty()) { // Verificar se a lista não é nula/vazia
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> updateChart(tempDataList));
                            }
                        } else {
                            Log.w("MazeTemp", "Lista de dados de temperatura é nula ou vazia após o parsing.");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Dados de temperatura vazios ou inválidos.", Toast.LENGTH_LONG).show();
                                    lineChart.setNoDataText("Nenhum dado de temperatura encontrado.");
                                    lineChart.invalidate();
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MazeTemp", "Erro ao parsear JSON de dados de temperatura: " + e.getMessage());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Erro no formato da resposta da temperatura.", Toast.LENGTH_LONG).show();
                                lineChart.setNoDataText("Erro ao interpretar dados.");
                                lineChart.invalidate();
                            });
                        }
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e("MazeTemp", "Erro do servidor ao buscar dados de temperatura: " + response.code() + " - " + errorBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Erro do servidor ao buscar dados de temperatura: " + response.code(), Toast.LENGTH_LONG).show();
                            lineChart.setNoDataText("Erro do servidor: " + response.code());
                            lineChart.invalidate();
                        });
                    }
                }
            }
        });
    }

    private void updateChart(List<TempData> tempDataList) {
        // Limpar o gráfico antes de adicionar novos dados
        lineChart.clear();
        lineChart.invalidate();

        // Se não houver dados de temperatura, exibe uma mensagem
        if (tempDataList == null || tempDataList.isEmpty()) {
            lineChart.setNoDataText("Nenhum dado de temperatura encontrado para exibir.");
            lineChart.invalidate(); // Garante que a mensagem é mostrada
            return;
        }

        ArrayList<Entry> tempEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < tempDataList.size(); i++) {
            TempData data = tempDataList.get(i);
            tempEntries.add(new Entry(i, data.getValue()));
            labels.add(data.getHora());
        }

        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature Value");
        tempDataSet.setColor(Color.BLUE);
        tempDataSet.setDrawCircles(true);
        tempDataSet.setDrawValues(false);
        tempDataSet.setLineWidth(2f);
        tempDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(tempDataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        // Ajustar o eixo Y dinamicamente para os valores do gráfico
        // Com base nos dados, o chart deve auto-escalar, mas pode ajudar a forçar um pouco
        // Se os valores são sempre positivos, setAxisMinimum(0f) é bom.
        // Se quiser que o gráfico se ajuste um pouco mais, pode calcular min/max dos dados:
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        lineChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setLabelCount(Math.min(labels.size(), 8), false);

        lineChart.invalidate(); // Atualizar o gráfico
    }
}