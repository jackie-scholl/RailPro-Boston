package com.tumblr.railproboston.appengine.realtime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

public class CsvDatabaseEngine {
    private static final Logger log = Logger.getLogger(CsvDatabaseEngine.class.getName());
    public static final String DATABASE_KIND = "real_time_update_csv";
    
    private DatastoreService datastore;
    
    public CsvDatabaseEngine() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }
    
    private static Key getAncestorKey() {
        return KeyFactory.createKey("real_time_update_csv_ancestor2", "root");
    }
    
    private static Key getAncestorKeyOld() {
        return KeyFactory.createKey("real_time_update_csv_ancestor", "root");
    }
    
    public boolean putCsv(Map<String, String> csvMap, Date time) {
        boolean success = true;
        Entity entity = getEntity(csvMap, time);
        Transaction txn = datastore.beginTransaction();
        try {
            datastore.put(txn, entity);
            txn.commit();
        } catch (OverQuotaException e) {
            log.log(Level.WARNING, "Out of quota on putting CSV in the database", e);
            success = false;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                success = false;
            }
        }
        
        return success;
    }
    
    public Map<Date, String> getCsv(Date start, Date end, String route) {
        Filter startFilter = new FilterPredicate("dateTime", FilterOperator.GREATER_THAN_OR_EQUAL, start);
        Filter endFilter = new FilterPredicate("dateTime", FilterOperator.LESS_THAN_OR_EQUAL, end);
        
        //Use CompositeFilter to combine multiple filters
        Filter startEndFilter = CompositeFilterOperator.and(startFilter, endFilter);
        
        // Use class Query to assemble a query
        Query q = new Query(DATABASE_KIND).setFilter(startEndFilter).setAncestor(getAncestorKey())
                .addProjection(new PropertyProjection(route, Text.class))
                .addProjection(new PropertyProjection("dateTime", Date.class));
        
        // Use PreparedQuery interface to retrieve results
        PreparedQuery pq = datastore.prepare(q);
        
        Map<Date, String> map = new HashMap<>();
        for (Entity result : pq.asIterable()) {
            Text csv = (Text) result.getProperty(route);
            Date dateTime = (Date) result.getProperty("dateTime");
            if (start.compareTo(dateTime) < 0 && end.compareTo(dateTime) > 0)
                map.put(dateTime, csv.getValue());
        }
        return map;
    }
    
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat timeFormat = new SimpleDateFormat("kk:mm:ss.SSSS (Z)");
    private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSSS (Z)");
    
    public Map<Date, String> getCsvOld(Date start, Date end, String route) {
        Filter startFilter = new FilterPredicate("date", FilterOperator.GREATER_THAN_OR_EQUAL, dateFormat.format(start));
        Filter endFilter = new FilterPredicate("date", FilterOperator.LESS_THAN_OR_EQUAL, dateFormat.format(end));
        
        //Use CompositeFilter to combine multiple filters
        Filter startEndFilter = CompositeFilterOperator.and(startFilter, endFilter);
        
        // Use class Query to assemble a query
        Query q = new Query(DATABASE_KIND).setFilter(startEndFilter).setAncestor(getAncestorKeyOld())
                .addProjection(new PropertyProjection(route, Text.class))
                .addProjection(new PropertyProjection("date", String.class))
                .addProjection(new PropertyProjection("time", String.class));
        
        // Use PreparedQuery interface to retrieve results
        PreparedQuery pq = datastore.prepare(q);
        
        Map<Date, String> map = new HashMap<>();
        for (Entity result : pq.asIterable()) {
            Text csv = (Text) result.getProperty(route);
            String dateString = (String) result.getProperty("date");
            String timeString = (String) result.getProperty("time");
            String dateTimeString = dateString + " " + timeString;
            Date dateTime;
            try {
                dateTime = dateTimeFormat.parse(dateTimeString);
                if (start.compareTo(dateTime) < 0 && end.compareTo(dateTime) > 0)
                    map.put(dateTime, csv.getValue());
            } catch (ParseException e) {
                log.log(Level.SEVERE, "Failed to parse date time string " + dateTimeString, e);
            }
            
        }
        return map;
    }
    
    private static Entity getEntity(Map<String, String> csvMap, Date time) {
        Entity e = new Entity(DATABASE_KIND, getAncestorKey());
        //e.setProperty("date", dateFormat.format(new Date()));
        //e.setProperty("time", timeFormat.format(new Date()));
        e.setProperty("dateTime", time);
        
        for (String route : csvMap.keySet()) {
            Text t = new Text(csvMap.get(route));
            e.setProperty(route, t);
        }
        
        return e;
    }
}
