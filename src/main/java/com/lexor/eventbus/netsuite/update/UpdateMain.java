/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lexor.eventbus.netsuite.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vinh
 */
public class UpdateMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
//        processJson("./jsonfiles/data-14-11-2019/contact-2019-11-14", 
//                new ProcessCustomer("./jsonfiles/mapping/customer-map.txt", new File("./jsonfiles/out-sql/customer.sql")));

//        processJson("./jsonfiles/data-14-11-2019/employee-2019-11-14", 
//                new ProcessSalesman("./jsonfiles/mapping/employee-map.txt", new File("./jsonfiles/out-sql/employee.sql")));
        
        processJson("./jsonfiles/data-14-11-2019/salesOrder", 
                new ProcessSaleOrder("./jsonfiles/mapping/saleorder-map.txt", new File("./jsonfiles/out-sql/saleOrder.sql"), 450000 ));
        
//        processJson("./jsonfiles/data-14-11-2019/message-2019-11-15", 
//                new ProcessMessages("./jsonfiles/mapping/message-map.txt", new File("./jsonfiles/out-sql/order-messages.sql")));

//          processJson("./jsonfiles/data-14-11-2019/note-2019-11-14", 
//                  new ProcessActionNote("./jsonfiles/mapping/action-note-map.txt", new File("./jsonfiles/out-sql/order-action-notes.sql")));
        
    }

   
    static private void processJson(String Path, BaseProcess clxx) {

        try {

            
            List<String> list = listFolders(Path);

            int idx = 0;

            
            for (String fileName : list) {

                if (idx++ == 1) {
                    //break; //Process one file
                }
                clxx.process(Path + "/" + fileName);
            }
            
            

        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(UpdateMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static private List<String> listFolders(String path) {
        List<String> ret = new ArrayList<>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println("File " + listOfFiles[i].getName());
                ret.add(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        Collections.sort(ret);
        return ret;
    }
}
