package com.tumblr.railproboston.appengine.realtime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.*;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

public class CsvDatabaseEngine {
    private static final Logger log = Logger.getLogger(CsvDatabaseEngine.class.getName());
    public static final String DATABASE_KIND = "real_time_update_csv";
    
    private DatastoreService datastore;
    
    public CsvDatabaseEngine() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }
    
    private static Key getAncestorKey() {
        return KeyFactory.createKey("real_time_update_csv_ancestor", "root");
    }
    
    public boolean putCsv(Map<String, String> csvMap) {
        boolean b = true;
        Entity entity = getEntity(csvMap);
        Transaction txn = datastore.beginTransaction();
        try {
            datastore.put(txn, entity);
            txn.commit();
        } catch (OverQuotaException e) {
            log.log(Level.WARNING, "Out of quota on putting CSV in the database", e);
            b = false;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                b = false;
            }
        }
        
        return b;
    }
    
    private static Entity getEntity(Map<String, String> jsonMap) {
        Entity e = new Entity(DATABASE_KIND, getAncestorKey());
        e.setProperty("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        e.setProperty("time", new SimpleDateFormat("kk:mm:ss.SSSS (Z)").format(new Date()));
        
        for (String route : jsonMap.keySet()) {
            Text t = new Text(jsonMap.get(route));
            e.setProperty(route, t);
        }
        
        return e;
    }
}
