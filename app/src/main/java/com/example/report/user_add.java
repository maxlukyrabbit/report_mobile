package com.example.report;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class user_add extends AppCompatActivity {
    private Spinner role;
    private EditText fio, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        password = findViewById(R.id.password);
        fio = findViewById(R.id.login);
        role = findViewById(R.id.role);
        String[] typeUserOptions = {"Администратор", "Рабочий"};
        ArrayAdapter<String> typeUserAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, typeUserOptions);
        typeUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        role.setAdapter(typeUserAdapter);
    }
    public void reg(View v){
        String fio_str = fio.getText().toString();
        String password_str = password.getText().toString();
        String role_str = role.getSelectedItem().toString().equals("Администратор") ? "admin" : "installer";



        if (fio_str.isEmpty() || password_str.isEmpty()) {
            Toast.makeText(this, "Введите фамилию, имя и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://77.222.47.209:3000/api/user_add";

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("fio", fio_str);
            jsonData.put("role", role_str);
            jsonData.put("password", password_str);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при создании JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonData.toString());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(this, "Успех", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(this, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Ошибка запроса: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void back(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}