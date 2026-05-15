package com.maze; // Mude para o nome do seu pacote

import android.content.Intent;
import android.net.Uri; // Importe esta classe
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MazeLoginActivity extends AppCompatActivity {

    private EditText etHost, etUsername, etPassword, etDatabase, etTeam;
    private Button btnConnect;
    private OkHttpClient client;
    private AppProperties appProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze_login);

        etHost = findViewById(R.id.etHost);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etDatabase = findViewById(R.id.etDatabase);
        // etTeam = findViewById(R.id.etTeam);
        btnConnect = findViewById(R.id.btnConnect);

        appProperties = AppProperties.load(this);
        etHost.setText(appProperties.resolveLoginHost());

        client = new OkHttpClient();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        String host = etHost.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String database = etDatabase.getText().toString().trim();

        if (host.isEmpty()) {
            host = appProperties.resolveLoginHost();
        }

        if (host.isEmpty() || username.isEmpty() || password.isEmpty() || database.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalLoginUrl = ApiConfig.buildEndpoint(host, "/api/login");

        Uri.Builder uriBuilder = Uri.parse(finalLoginUrl).buildUpon();
        uriBuilder.appendQueryParameter("username", username);
        uriBuilder.appendQueryParameter("password", password);
        uriBuilder.appendQueryParameter("database", database);
        String requestUrl = uriBuilder.build().toString();

        Log.d("MazeLoginDebug", "GET URL: " + requestUrl);

        Request request = new Request.Builder()
                .url(requestUrl)
                .get()
                .build();

        // Final copies for lambda use
        final String hostFinal = host;
        final String usernameFinal = username;
        final String passwordFinal = password;
        final String databaseFinal = database;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast
                        .makeText(MazeLoginActivity.this, "Erro de conexão: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show());
                // Log.e("MazeLogin", "Erro na requisição: " + e.getMessage());
                Log.e("MazeLoginDebug", "Falha total na requisição", e); // O 'e' no final imprime o erro completo
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("MazeLoginDebug", "Resposta API: " + response);
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("MazeLogin", "Resposta da API: " + responseData);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            String idGrupo = jsonResponse.getString("IDGrupo");
                            runOnUiThread(() -> {
                                // etTeam.setText(idGrupo);
                                Toast.makeText(MazeLoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT)
                                        .show();
                                Intent intent = new Intent(MazeLoginActivity.this, MainActivity.class);
                                intent.putExtra("IDGrupo", idGrupo);
                                intent.putExtra("host", hostFinal);
                                intent.putExtra("database", databaseFinal);
                                intent.putExtra("username", usernameFinal);
                                intent.putExtra("password", passwordFinal);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            String message = jsonResponse.getString("message");
                            runOnUiThread(
                                    () -> Toast.makeText(MazeLoginActivity.this, message, Toast.LENGTH_LONG).show());
                        }
                    } catch (JSONException e) {
                        Log.e("MazeLogin", "Erro ao parsear JSON: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(MazeLoginActivity.this,
                                "Erro no formato da resposta do servidor.", Toast.LENGTH_LONG).show());
                    }
                } else {
                    runOnUiThread(() -> Toast
                            .makeText(MazeLoginActivity.this, "Erro do servidor: " + response.code(), Toast.LENGTH_LONG)
                            .show());
                }
            }
        });
    }
}