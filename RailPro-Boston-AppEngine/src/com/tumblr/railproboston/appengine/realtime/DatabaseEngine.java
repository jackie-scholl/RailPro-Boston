package com.tumblr.railproboston.appengine.realtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

public class DatabaseEngine {
    private static final Logger log = PredictServlet.log;
    public static final String DATABASE_KIND = "real_time_update";
    
    private DatastoreService datastore;
    
    public DatabaseEngine() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }
    
    private static Key getAncestorKey(String route) {
        return KeyFactory.createKey("real_time_update_ancestors", route);
    }
    
    
    public Message getMessage(CheckIn checkin) {
        List<Message> messages = getMessages(checkin, 1);
        if (messages.size() != 1)
            log.warning("Size of returned messages list is not 1; messages.size()=" + messages.size());
        if (messages.size() == 0)
            return null;
        return messages.get(0);
    }
    
    public List<Message> getMessages(CheckIn checkin, int limit) {
        log.info("About to get the messages for the checkin " + checkin + " from the database");
        List<Message> messages = getRealTimeMessagesSync(checkin, limit);
        log.info("Done getting the messages for the checkin " + checkin + " from the database");
        return messages;
    }
    
    private List<Message> getRealTimeMessagesSync(CheckIn checkin, int limit) {
        PreparedQuery pq = getMessagesSync(checkin);
        List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(limit));
        List<Message> messages = new ArrayList<>();
        for (Entity e : entities) {
            messages.add(getRealTimeMessage(e));
        }
        return messages;
    }
    
    private PreparedQuery getMessagesSync(CheckIn checkin) {
        return getMessagesSync(checkin.getRoute(), checkin.getTripId(), checkin.getStation());
    }
    
    private PreparedQuery getMessagesSync(String route, String tripId, String station) {
        Filter f = CompositeFilterOperator.and(FilterOperator.EQUAL.of("route", route),
                FilterOperator.EQUAL.of("trip", tripId), FilterOperator.EQUAL.of("station", station));
        Query q = new Query(DATABASE_KIND, getAncestorKey(route))
                .addSort("dateCreated", Query.SortDirection.DESCENDING).setFilter(f);
        
        return datastore.prepare(q);
    }
    
    
    public boolean putMessagesSync(Iterable<Message> messages, String route) {
        log.info("About to store messages for the route " + route + " in the database");
        
        boolean b = true;
        
        List<Entity> updates = new ArrayList<Entity>();
        
        for (Message m : messages) {
            Entity update = getEntity(m);
            updates.add(update);
        }
        
        Transaction txn = datastore.beginTransaction();
        try {
            datastore.put(txn, updates);
            txn.commit();
        } catch (OverQuotaException e) {
            log.log(Level.WARNING, "Out of quota on putting messages for route " + route + " in the database", e);
            b = false;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                b = false;
            }
        }
        
        return b;
    }
    
    private static Message getRealTimeMessage(Entity e) {
        Message m = new Message(
                (String) e.getProperty("route"),
                (String) e.getProperty("timestamp"),
                (String) e.getProperty("trip"),
                (String) e.getProperty("destination"),
                (String) e.getProperty("stop"),
                (String) e.getProperty("scheduled"),
                (String) e.getProperty("flag"),
                (String) e.getProperty("vehicle"),
                (String) e.getProperty("latitude"),
                (String) e.getProperty("longitude"),
                (String) e.getProperty("heading"),
                (String) e.getProperty("speed"),
                (String) e.getProperty("lateness"),
                (Date) e.getProperty("dateFetched"));
        return m;
    }
    
    private static Entity getEntity(Message m) {
        String route = m.getRoute();
        Entity e = new Entity(DATABASE_KIND, getAncestorKey(route));
        e.setProperty("dateFetched", m.getDateFetched());
        e.setProperty("route", m.getRoute());
        e.setProperty("timestamp", m.getTimeStamp());
        e.setProperty("trip", m.getTrip());
        e.setProperty("destination", m.getDestination());
        e.setProperty("stop", m.getStation());
        e.setProperty("scheduled", m.getScheduled());
        e.setProperty("flag", m.getFlag());
        e.setProperty("vehicle", m.getVehicle());
        e.setProperty("latitude", m.getLatitude());
        e.setProperty("longitude", m.getLongitude());
        e.setProperty("heading", m.getHeading());
        e.setProperty("speed", m.getSpeed());
        e.setProperty("lateness", m.getLateness());
        return e;
    }
}
