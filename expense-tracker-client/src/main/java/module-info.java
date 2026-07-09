
module expense.tracker.client {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    requires com.google.gson;

    requires org.apache.pdfbox;
    requires org.apache.fontbox;   
    requires java.desktop;         

    requires java.net.http;

    exports org.example;
    exports org.example.controllers;
    exports org.example.views;
    exports org.example.dialogs;
    exports org.example.components;
    exports org.example.models;
    exports org.example.utils;
}