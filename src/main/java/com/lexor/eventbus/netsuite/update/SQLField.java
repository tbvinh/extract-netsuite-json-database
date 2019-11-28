/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lexor.eventbus.netsuite.update;

/**
 *
 * @author vinh
 */
public class SQLField {

    String table;
    String fname;
    String ftype;
    String pid;
    Integer splitItem;
    Boolean bExtractHtml;
    
    public SQLField() {
    }

    public SQLField(String table, String fname, String ftype, String pid, int splitItem, Boolean bExtractHtml) {
        this.table = table;
        this.fname = fname;
        this.ftype = ftype;
        this.pid = pid;
        this.splitItem = splitItem;
        this.bExtractHtml = bExtractHtml;
    }

    public String getFtype() {
        return ftype;
    }

    public void setFtype(String ftype) {
        this.ftype = ftype;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Integer getSplitItem() {
        return splitItem;
    }

    public void setSplitItem(Integer splitItem) {
        this.splitItem = splitItem;
    }

    public Boolean getbExtractHtml() {
        return bExtractHtml;
    }

    public void setbExtractHtml(Boolean bExtractHtml) {
        this.bExtractHtml = bExtractHtml;
    }

}
