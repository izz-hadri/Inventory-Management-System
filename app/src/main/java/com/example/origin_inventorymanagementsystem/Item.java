package com.example.origin_inventorymanagementsystem;

import java.io.Serializable;

public class Item implements Serializable {
    private String item_id = null;
    private String item_name = null;
    private String item_borrowDate = null;
    private String item_returnDate = null;
    private String item_staffId = null;
    private String item_studentId = null;
    private String item_QRcodeDynamic = null;
    private String item_category = null;


    public Item() {

    }

    public Item(String item_id, String item_name, String item_borrowDate, String item_returnDate, String item_staffId, String item_studentId, String item_QRcodeDynamic, String item_category) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_borrowDate = item_borrowDate;
        this.item_returnDate = item_returnDate;
        this.item_staffId = item_staffId;
        this.item_studentId = item_studentId;
        this.item_QRcodeDynamic = item_QRcodeDynamic;
        this.item_category = item_category;
    }

    public Item(String item_id, String item_name, String item_borrowDate, String item_returnDate) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_borrowDate = item_borrowDate;
        this.item_returnDate = item_returnDate;
    }

    public Item(String item_id, String item_name, String item_category, String item_studentId, String item_staffId) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_category = item_category;
        this.item_studentId = item_studentId;
        this.item_staffId = item_staffId;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_borrowDate() {
        return item_borrowDate;
    }

    public void setItem_borrowDate(String item_borrowDate) {
        this.item_borrowDate = item_borrowDate;
    }

    public String getItem_returnDate() {
        return item_returnDate;
    }

    public void setItem_returnDate(String item_returnDate) {
        this.item_returnDate = item_returnDate;
    }

    public String getItem_category() {
        return item_category;
    }

    public String getItem_staffId() {
        return item_staffId;
    }

    public void setItem_staffId(String item_staffId) {
        this.item_staffId = item_staffId;
    }

    public String getItem_studentId() {
        return item_studentId;
    }

    public void setItem_studentId(String item_studentId) {
        this.item_studentId = item_studentId;
    }

    public String getItem_QRcodeDynamic() {
        return item_QRcodeDynamic;
    }

    public void setItem_QRcodeDynamic(String item_QRcodeDynamic) {
        this.item_QRcodeDynamic = item_QRcodeDynamic;
    }
}

