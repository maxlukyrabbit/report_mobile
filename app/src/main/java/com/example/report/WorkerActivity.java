package com.example.report;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerActivity extends AppCompatActivity {
    private EditText name, message;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ArrayList<reports> reports = new ArrayList<>();
    private ListView history;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        name = findViewById(R.id.report_name);
        message = findViewById(R.id.report_description);
        loadReport();
    }

    public void sendReport(View v) {
        String reportName = name.getText().toString().trim();
        String reportDescription = message.getText().toString().trim();
        String userId = MainActivity.id_user;

        if (reportName.isEmpty() || reportDescription.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(() -> {
            String result = sendPutRequest(reportName, reportDescription, userId);
            runOnUiThread(() -> {
                Toast.makeText(WorkerActivity.this, result, Toast.LENGTH_LONG).show();
                if (result.startsWith("Успех")) {
                    name.setText("");
                    message.setText("");
                }
            });
            loadReport();
        });

    }

    private String sendPutRequest(String reportName, String reportDescription, String userId) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://77.222.47.209:3000/api/add_report");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("report_name", reportName);
            jsonInput.put("report_description", reportDescription);
            jsonInput.put("user_id", userId);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return "Успех: отчёт отправлен";
            } else {
                return "Error: Server returned HTTP " + responseCode;
            }
        } catch (Exception e) {
            return "Error: " + e.getLocalizedMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
    private void  reloadListView(){
        CustomListView customListViewAdapter = new CustomListView(this, reports);
        history = findViewById(R.id.history);
        history.setAdapter(customListViewAdapter);
    }
    private void loadReport() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String apiUrl = "http://77.222.47.209:3000/api/add_report?user_id=" + MainActivity.id_user;
            ArrayList<reports> fetched_reports = new ArrayList<>();
            String resultMessage;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject lectureJson = jsonArray.getJSONObject(i);
                        fetched_reports.add(new reports(
                                lectureJson.getInt("id_report"),
                                lectureJson.getString("report_name"),
                                lectureJson.getString("report_description"),
                                lectureJson.getInt("status"),
                                lectureJson.getString("fio")
                        ));
                    }
                    resultMessage = "Отчёты успешно загружены!";
                } else {
                    resultMessage = "Ошибка: Код ответа " + responseCode;
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                resultMessage = "Ошибка при выполнении запроса: " + e.getMessage();
            }
            String finalResultMessage = resultMessage;
            handler.post(() -> {
                if (!fetched_reports.isEmpty()) {
                    reports.clear();
                    reports.addAll(fetched_reports);
                }
                reloadListView();
                Toast.makeText(this, finalResultMessage, Toast.LENGTH_SHORT).show();
            });
        });
    }
    public void back(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}