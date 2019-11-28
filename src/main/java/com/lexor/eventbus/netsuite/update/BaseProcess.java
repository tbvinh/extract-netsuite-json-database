/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lexor.eventbus.netsuite.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author vinh
 */
public abstract class BaseProcess {
    
    private HashMap<String, String> map;
    protected File output;
    protected int beginAutonumber;
    
    public BaseProcess(String filePath, File file) {
        map = doGetMap(filePath);
        output = file;
        if(output.exists()) output.delete();
        
        this.beginAutonumber = 0;
    }
    public BaseProcess(String filePath, File file, int beginAutonumber) {
        map = doGetMap(filePath);
        output = file;
        if(output.exists()) output.delete();
        
        this.beginAutonumber = beginAutonumber;
    }

    public abstract void process(String fileName) throws IOException;

    protected HashMap<String, String> getMap() {
        return map;
    }

    private HashMap<String, String> doGetMap(String filePath) {
        HashMap<String, String> ret = new HashMap<>();

        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            while ((line = reader.readLine()) != null) {
                int index = line.indexOf(":");
                if (index > 0) {
                    String key = line.substring(0, index).trim();
                    String value = line.substring(index + 1).trim();
                    ret.put(key, value);
                } else {
//                    System.out.println("ignoring line: " + line);
                }
            }
            reader.close();
        } catch (Exception ex) {

        }
        return ret;
    }
    protected SQLField getSQLColumn(String value) {
        SQLField ret = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ret = objectMapper.readValue(value, SQLField.class);
        } catch (JsonProcessingException ex) {
            //ex.printStackTrace();
        }
        return ret;
    }
    protected String getValue(SQLField field, String value){
        if (field.ftype.equals("int") || value.equals("null")) {
            return value;            
        }else if(field.ftype.equals("date")){
            return "'" + value.replaceAll("T", " ").substring(0, 19)+"'";
        }else{
            if(field.bExtractHtml != null && field.bExtractHtml){
                value = extractHTML(value);
            }
            
            return "'" + value.replaceAll("'", "''") +"'";
            
        }
    }
    protected String extractHTML(String value){
        
        String ret = value.replace("\\/", "/").replace("\\\"", "\"");
        int idxBodyStart = value.indexOf("<BODY");
        int idxBodyEnd = value.indexOf("</BODY");
        if(idxBodyStart >= 0 && idxBodyEnd > idxBodyStart){
            int idx = value.indexOf(">", idxBodyStart+1);
            if(idx > 0){
                ret = value.substring(idx+1, idxBodyEnd).replace("<a", "");
                
            }
        }
        
        return ret;
    }
    protected String getStandardName(String name){
        return name.replace(".", "_");
    }
}
