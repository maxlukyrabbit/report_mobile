package com.example.report;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    public static int root = 0;
    public static String id_user = "1";
    private EditText name;
    private EditText password;
    public static String role_get = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
    }


    public void enter(View v) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());


        executor.execute(() -> {
            String apiUrl = "http://77.222.47.209:3000/api/user_add?fio=" + name.getText().toString();
            String resultMessage = "";
            String password_get = null;

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray jsonResponse = new JSONArray(response.toString());
                    if (jsonResponse.length() > 0) {
                        JSONObject firstObject = jsonResponse.getJSONObject(0);
                        password_get = firstObject.optString("password", null);
                        id_user = firstObject.optString("id_user", null);
                        role_get = firstObject.optString("role", null);
                    } else {
                        resultMessage = "Пользователь не найден";
                    }
                } else {
                    resultMessage = "Ошибка сервера: " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultMessage = "Ошибка при выполнении запроса: " + e.getMessage();
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String finalResultMessage = resultMessage;
            String finalPassword = password_get;

            handler.post(() -> {
                if (finalPassword != null) {
                    if(password.getText().toString().equals(finalPassword)){
                        if(Objects.equals(role_get, "admin")){
                            Intent intent = new Intent(this, confirm.class);
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(this, WorkerActivity.class);
                            startActivity(intent);
                        }
                    }
                    else{
                        Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), finalResultMessage.isEmpty() ? "Пользователь не найден" : finalResultMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public void registration(View v){
        Intent intent = new Intent(this, user_add.class);
        startActivity(intent);
    }
}
