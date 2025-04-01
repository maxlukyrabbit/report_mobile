package com.example.report;

public class reports {
    int id_report;
    String report_name;
    String report_description;
    int status;
    String fio;
    public reports(int id_report, String report_name, String report_description, int status, String fio){
        this.id_report = id_report;
        this.report_name = report_name;
        this.report_description = report_description;
        this.status = status;
        this.fio = fio;
    }
}
