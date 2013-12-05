package com.tumblr.railproboston.appengine.realtime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

public class JsonDatabaseEngine {
    private static final Logger log = Logger.getLogger(JsonDatabaseEngine.class.getName());
    public static final String DATABASE_KIND = "real_time_update_json";
    
    private DatastoreService datastore;
    
    public JsonDatabaseEngine() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }
    
    private static Key getAncestorKey() {
        return KeyFactory.createKey("real_time_update_json_ancestor", "root");
    }
    
    public boolean putJson(Map<String, String> jsonMap) {
        boolean b = true;
        
        Entity entity = getEntity(jsonMap);
        
        Transaction txn = datastore.beginTransaction();
        try {
            datastore.put(txn, entity);
            txn.commit();
        } catch (OverQuotaException e) {
            log.log(Level.WARNING, "Out of quota on putting JSON in the database", e);
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
