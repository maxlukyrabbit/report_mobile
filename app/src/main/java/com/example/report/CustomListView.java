package com.example.report;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomListView extends BaseAdapter {
    private final Context context;
    private final ArrayList<reports> items;
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CustomListView(Context context, ArrayList<reports> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.activity_castom_list_view, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        reports report = items.get(position);
        holder.name.setText(report.report_name);
        holder.reportDescription.setText(report.report_description);
        holder.status.setText(report.status == 0 ? "Ожидает просмотра" : "Одобрен");

        if (Objects.equals(MainActivity.role_get, "admin")) {
            holder.nameInst.setVisibility(View.VISIBLE);
            holder.installer.setVisibility(View.VISIBLE);
            holder.installer.setText(report.report_name);
            holder.conf.setVisibility(View.VISIBLE);
        } else {
            holder.nameInst.setVisibility(View.GONE);
            holder.installer.setVisibility(View.GONE);
            holder.conf.setVisibility(View.GONE);
        }

        holder.conf.setOnClickListener(v -> {
            int id_report = report.id_report;
            executorService.execute(() -> {
                String url = "http://77.222.47.209:3000/api/change_status?status=1&id_report=" + id_report;
                Request request = new Request.Builder().url(url).build();

                try (Response response = client.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (success) {
                            Toast.makeText(context, "Успешно", Toast.LENGTH_SHORT).show();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Intent intent = new Intent(context, confirm.class);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView name, reportDescription, nameInst, installer, status;
        Button conf;

        ViewHolder(View view) {
            name = view.findViewById(R.id.name);
            reportDescription = view.findViewById(R.id.message);
            nameInst = view.findViewById(R.id.name_inst);
            installer = view.findViewById(R.id.installer);
            conf = view.findViewById(R.id.conf);
            status = view.findViewById(R.id.status);
        }
    }
}
