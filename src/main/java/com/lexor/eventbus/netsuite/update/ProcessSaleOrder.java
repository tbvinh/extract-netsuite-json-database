/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lexor.eventbus.netsuite.update;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vinh
 */
public class ProcessSaleOrder extends BaseProcess {

    List<String> sqlField = new ArrayList<>();
    List<String> sqlVal = new ArrayList<>();

    List<String> sqlChilds = new ArrayList<>();

    public ProcessSaleOrder(String filePath, File output) {
        super(filePath, output);
    }
    
    public ProcessSaleOrder(String filePath, File output, int beginAutonumber) {
        super(filePath, output, beginAutonumber);
    }
    @Override
    public void process(String file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(this.output, true), 32768);) {

            //Array
            JsonNode saleList = objectMapper.readValue(new File(file), JsonNode.class);

            if (saleList.isArray()) {
                for (int i = 0; i < saleList.size(); i++) {

                    JsonNode saleOrder = saleList.get(i); //Sale Order
//                    out.write("/*============================================");
//                    out.write(String.format("\nObject index: %d ", i + 1));
//                    out.write("==============================================*/");


                    if(this.getMap().containsKey("autoNumberId")){
                        this.sqlField.add("autoNumberId");
                        this.sqlVal.add(String.format("%d", ++this.beginAutonumber));
                    }
                    
                    for (Iterator<String> iter = saleOrder.fieldNames(); iter.hasNext();) {

                        String key = iter.next();

                        if (!this.getMap().containsKey(key)) {
                            continue;
                        }

                        switch (key) {
                            case "billingAddress":
                                processJsonLevel1(saleOrder, key);
                                break;
                                
                            case "entity":
                                processJsonLevel1Entity(saleOrder, key);
                                break;
                                
                            case "discountItem":
                            case "shipMethod":
                            case "shippingAddress":
                            case "subsidiary":
                            case "terms":
                                processJsonLevel1(saleOrder, key);
                                break;
                                
                            case "itemList":
                                processItemlist(saleOrder);
                                break;
                                
                            case "location":
                                processJsonLevel1(saleOrder, key);
                                break;
                                
                            case "salesRep":
                                processJsonLevel1Entity(saleOrder, key);
                                break;
                                
                            case "customFieldList":
                                processCustomFieldList(saleOrder);
                                break;
                                
                            case "salesTeamList":
                                processsalesTeamList(saleOrder);
                                break;
                                
                            case "partner":
                                processsalesPartner(saleOrder);
                                break;
                                
                            default:
                                //Root properties

                                String value = printKeyValue(key, key, saleOrder);
                                SQLField field = getSQLColumn(this.getMap().get(key));

                                if (field != null && !field.fname.isEmpty()) {
                                    this.sqlField.add(field.fname);

                                    this.sqlVal.add(this.getValue(field, value));
                                }
                                break;
                        }
                    }

                    String sql = getSqlInsert();
                    System.out.print(sql);
                    
                    out.write(String.format("\nprint '{%s}'\n", java.util.UUID.randomUUID().toString()));
                    
                    out.write(sql);

                    sqlChilds.forEach((s) -> {
                        try {
                            System.out.print(s);
                            out.write(s);
                        } catch (IOException ex) {

                        }
                    });

                    out.write("\ngo\n\n");
                    this.sqlField.clear();
                    this.sqlVal.clear();
                    this.sqlChilds.clear();

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//end function

    String getSqlInsert() {
        String ret;
        String sqlDeclareID = "declare @pid int\n";

        ret = sqlDeclareID + "exec @pid = z_import_sale_order ";

        for (int i = 0; i < this.sqlField.size(); i++) {
            if(i > 0) ret +=", ";
            ret += String.format("\n  @%s = %s ", this.sqlField.get(i), this.sqlVal.get(i));
        }
        ret += ";\n";

        return ret;
    }

    String printKeyValue(String mapkey, String jsonKey, JsonNode jsonNode) {
        String des = this.getMap().get(mapkey);
        String value = "";
        JsonNode obj = jsonNode.get(jsonKey);
        if (obj != null) {
            value = jsonNode.get(jsonKey).asText();
        }

        //System.out.println(String.format(": %-30s\t", des) + value);
        return value;
    }

    void processJsonLevel1(JsonNode jsonNode, String jsonName) {

        JsonNode newNode = jsonNode.get(jsonName);
        if (newNode.isNull()) {
            return;
        }

        String value = this.getMap().get(jsonName);

        if (value == null) {
            return;
        }

        for (Iterator<String> iter = newNode.fieldNames(); iter.hasNext();) {

            String key = iter.next();
            String newKey = jsonName + "." + key;

            if (!this.getMap().containsKey(newKey)) {
                continue;
            }

            value = this.getMap().get(newKey);
            SQLField field = this.getSQLColumn(value);

            if (field == null) {
                continue;
            }

            value = newNode.get(key).asText();

            this.sqlField.add(this.getStandardName(jsonName + "_" + field.fname));

            this.sqlVal.add(this.getValue(field, value));
        }

    }

    void processJsonLevel1Entity(JsonNode jsonNode, String jsonName) {

        JsonNode newNode = jsonNode.get(jsonName);
        if (newNode.isNull()) {
            return;
        }

        String value = this.getMap().get(jsonName);

        if (value == null) {
            return;
        }

        for (Iterator<String> iter = newNode.fieldNames(); iter.hasNext();) {

            String key = iter.next();
            String newKey = jsonName + "." + key;

            if (!this.getMap().containsKey(newKey)) {
                continue;
            }

            value = this.getMap().get(newKey);
            SQLField field = this.getSQLColumn(value);

            if (field == null) {
                continue;
            }

            value = newNode.get(key).asText();

            this.sqlField.add(this.getStandardName(jsonName + "_" + field.fname));

            if(field.splitItem !=null){
                String arr[] = value.split(" ");
                
                this.sqlVal.add(this.getValue(field, arr[field.splitItem].trim()));
                
            }else{
                this.sqlVal.add(this.getValue(field, value));
            }
        }

    }
    
    String processJsonItemListCustomfield(JsonNode jsonNode, String jsonName) {
        String sql = "";

        JsonNode customFieldArr = jsonNode.get("customField");

        for (JsonNode ctf : customFieldArr) {
            if (!ctf.get("scriptId").asText().equals("custcol_sold_price")) {
                continue;
            }

            for (Iterator<String> iter = ctf.fieldNames(); iter.hasNext();) {

                String keyChild = iter.next();
                String mapKey = jsonName + "." + keyChild;

                if (this.getMap().containsKey(mapKey)) {

                    SQLField field = this.getSQLColumn(this.getMap().get(mapKey));
                    if (field.fname.isEmpty()) {
                        continue;
                    }

                    String value = ctf.get(keyChild).asText();
                    
                    sql += String.format("\n    , @%s=%s", this.getStandardName(mapKey), getValue(field, value));
                    

                }

            }
        }
        return sql;
    }

    String processJsonItemListItem(JsonNode jsonNode, String jsonName) {
        String sql = "";
        for (Iterator<String> iter = jsonNode.fieldNames(); iter.hasNext();) {

            String keyChild = iter.next();
            String mapKey = jsonName + "." + keyChild;

            if (this.getMap().containsKey(mapKey)) {

                SQLField field = this.getSQLColumn(this.getMap().get(mapKey));
                if (field.fname.isEmpty()) {
                    continue;
                }

                String value = jsonNode.get(keyChild).asText();
                if ("itemList.item.item.name".equals(mapKey)) {
                    String arr[] = value.split(":");
                    String color = "null";

                    if (arr.length == 2) {
                        color = arr[1];
                    } else if (arr.length == 3) {
                        color = arr[2];
                    }
                    sql += String.format("\n    , @%s=%s", this.getStandardName(mapKey), getValue(field, arr[0].trim()));
                    if (arr.length > 0) {
                        sql += String.format("\n    , @%s=%s", this.getStandardName("itemList.item.item.color"), getValue(field, color.trim()));
                    }
                } else {
                    sql += String.format("\n    , @%s=%s", this.getStandardName(mapKey), getValue(field, value));
                }

            }

        }
        return sql;
    }

    
    
    private void processItemlist(JsonNode jsonNode) {
        JsonNode itemList = jsonNode.get("itemList");
        JsonNode items = itemList.get("item");

        if (!items.isArray()) {
            return;
        }

        String sTemp;
        for (int i = 0; i < items.size(); i++) {
            JsonNode jsonItem = items.get(i);

            String sql = "exec z_import_item @parentId = @pid";

            for (Iterator<String> iter = jsonItem.fieldNames(); iter.hasNext();) {
                String keyChild = iter.next();

                String mapKey = "itemList.item." + keyChild;
                JsonNode subItems = jsonItem.get(keyChild);
                switch (keyChild) {

                    case "item":
                    case "units":
                    case "location":
                    case "taxCode":

                        sTemp = processJsonItemListItem(subItems, "itemList.item." + keyChild);
                        if (!sTemp.isEmpty()) {
                            sql += sTemp;
                        }
                        break;
                    case "customFieldList":
                        sTemp = processJsonItemListCustomfield(subItems, "itemList.item.customFieldList");
                        if (!sTemp.isEmpty()) {
                            sql += sTemp;
                        }

                        break;
                    default:
                        if (this.getMap().containsKey(mapKey)) {
                            SQLField field = this.getSQLColumn(this.getMap().get(mapKey));
                            if (field.fname.isEmpty()) {
                                continue;
                            }

                            String value = jsonItem.get(keyChild).asText();
                            sql += String.format(", @%s=%s", this.getStandardName(mapKey), getValue(field, value));
                        }
                        break;
                }

            }
            this.sqlChilds.add(sql + "\n");
        }

    }

    private void processCustomFieldList(JsonNode jsonNode) {
        JsonNode newNode = jsonNode.get("customFieldList");
        
        if(newNode.isNull()) return;
        
        JsonNode customField = newNode.get("customField");
        if(customField.isNull() || !customField.isArray()) return;
        
        boolean bIsLexorCare = false;
        String caseNo = "";
        outer: 
        for(JsonNode item: customField){
            if(item.has("scriptId") && item.get("scriptId").asText().equals("custbody39") ){
                JsonNode value = item.get("value");
                if(!value.isNull() && !value.get("name").isNull() ){
                    String name = value.get("name").asText().toUpperCase();
                    String arr[] = name.split(" ");
                    for(String sTemp : arr){
                        if(sTemp.startsWith("#SO") || (sTemp.startsWith("SO") && sTemp.length() > 2) 
                                || name.contains("SO ") || name.contains("SO#")
                                || name.contains("SO: ") //ONLINE ORDER
                                || name.contains("LX-SSU") 
                                || name.contains("ONLINE ORDER") 
                                ){
                            
                            bIsLexorCare = true;
                            caseNo = name;
                            
                            continue outer;
                        }
                    }
                }
            }
        }
        if(bIsLexorCare){
            this.sqlField.add(this.getStandardName("caseNo"));
            this.sqlVal.add("'" + caseNo.replace("'", "") +"'");
        }
        
    }

    private void processsalesTeamList(JsonNode saleOrder) {
        JsonNode newNode = saleOrder.get("salesTeamList");
        if (newNode.isNull()) {
            return;
        }
        newNode = newNode.get("salesTeam");
        
        if(newNode.isNull() || newNode.isArray()==false)
        {
            return;
        }

        int idx = 1;
        for(JsonNode aItem : newNode){

            JsonNode empl = aItem.get("employee");
            if(empl.isNull()) continue;
            
            this.sqlField.add(this.getStandardName("salesTeamList" + "_" + "internalId" + idx));
            this.sqlVal.add("'" + empl.get("internalId").asText() +"'");
            
            JsonNode percentage = aItem.get("contribution");
            
            if(percentage.isNull() == false){
                this.sqlField.add(this.getStandardName("salesTeamList" + "_" + "percentage" + idx));
                this.sqlVal.add(percentage.asText());
            }
            
            idx += 1;
            if(idx > 2) break; //Support 2 salesRep
        }
    }

    private void processsalesPartner(JsonNode saleOrder) {
        JsonNode newNode = saleOrder.get("partner");
        if (newNode.isNull()) {
            return;
        }
        
        
        this.sqlField.add(this.getStandardName("partner" + "_" + "id"));
        this.sqlVal.add(newNode.get("internalId").asText());
        
    }

}
