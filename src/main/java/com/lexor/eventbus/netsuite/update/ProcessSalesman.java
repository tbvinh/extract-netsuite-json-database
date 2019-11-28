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
public class ProcessSalesman extends BaseProcess {

    List<String> sqlField = new ArrayList<>();
    List<String> sqlVal = new ArrayList<>();

    List<String> sqlChilds = new ArrayList<>();

    public ProcessSalesman(String filePath, File output) {
        super(filePath, output);
    }

    @Override
    public void process(String file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(this.output, true), 32768);){

            //Array
            JsonNode saleList = objectMapper.readValue(new File(file), JsonNode.class);

            if (saleList.isArray()) {
                for (int i = 0; i < saleList.size(); i++) {

                    JsonNode saleOrder = saleList.get(i); //Sale Order
                    out.write("\n\n/*============================================");
                    out.write(String.format("\nObject index: %d ", i + 1));
                    out.write("\n==============================================*/");
                    for (Iterator<String> iter = saleOrder.fieldNames(); iter.hasNext();) {

                        String key = iter.next();

                        if (!this.getMap().containsKey(key)) {
                            continue;
                        }

                        switch (key) {
                            
                            default:
                                //Root properties

                                String value = printKeyValue(key, key, saleOrder);
                                SQLField field = getSQLColumn(this.getMap().get(key));

                                if (field != null) {
                                    this.sqlField.add(field.fname);
                                    if (!field.ftype.equals("int") && !value.equals("null")) {
                                        value = "'" + value.replaceAll("'", "''") + "'";
                                    }
                                    this.sqlVal.add(value);
                                }
                                break;
                        }
                    }

                    String sql = getSqlInsert();
                    System.out.print(sql);
                    out.write(String.format("\nprint '{%s}'\n", java.util.UUID.randomUUID().toString()));
                    
                    out.write(sql);
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
        
        String ret = "\nexec z_import_sales_man ";
        

        for (int i = 0; i < this.sqlField.size(); i++) {
            if (i > 0) {
                ret += ", ";
            }
            
            ret += String.format("@%s= %s", this.sqlField.get(i) , this.sqlVal.get(i));
            
        }
        
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

    


}
