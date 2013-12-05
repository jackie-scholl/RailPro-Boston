package com.tumblr.railproboston.appengine.realtime;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

public class PredictionEngine {
    private static final Logger log = Logger.getLogger(PredictionEngine.class.getName());
    
    private static final int MAX_TIME_SECONDS = 60 * 60 * 5;
    
    private Cache cache;
    
    public PredictionEngine() {
        Map<String, Integer> props = new HashMap<String, Integer>();
        props.put(GCacheFactory.EXPIRATION_DELTA, MAX_TIME_SECONDS);
        
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(props);
        } catch (CacheException e) {
            log.log(Level.SEVERE, "Could not create cache", e);
        }
    }
    
    public String getPredictedLateness(CheckIn checkin) {
        String key = checkin.getTripNumber() + ":" + checkin.getStation();
        if (cache.containsKey(key))
            return (String) cache.get(key);
        else
            return "No record";
    }
    
    
    public void updatePredictionsAsync(Map<String, String> jsonMap) {
        final Map<String, String> m = jsonMap;
        Runnable r = new Runnable() {
            public void run() {
                updatePredictionsSync(m);
            }
        };
        com.google.appengine.api.ThreadManager.currentRequestThreadFactory().newThread(r).run();
    }
    
    private void updatePredictionsSync(Map<String, String> jsonMap) {
        for (String route : jsonMap.keySet()) {
            String json = jsonMap.get(route);
            MessageList messageList = JsonEngine.getMessageListFromJson(json, route);
            if (messageList == null) {
                log.warning("messageList is null");
                return;
            }
            for (Message m : messageList) {
                updatePredictionSync(m);
            }
        }
    }
    
    private void updatePredictionSync(Message message) {
        String key = message.getTrip() + ":" + message.getStation();
        String predicted = getPredicted(message);
        /*log.info("Updating prediction for trip " + message.getTrip() + " and station " + message.getStation()
                + " to prediction " + predicted);*/
        
        cache.put(key, predicted);
    }
    
    private static String getPredicted(Message m) {
        if (m.hasLateness())
            return m.getLateness();
        return "UNK";
    }
}

//private static final int MAX_MESSAGES = 10;

/*UpdaterEngine.updateMessagesForRouteSync(checkin.getRoute());
List<Message> messages = new DatabaseEngine().getMessages(checkin, MAX_MESSAGES);
return getPredicted(messages);*/
/*private static String getPredicted(List<Message> messages) {
    if (messages.isEmpty())
        return "NONE";
    for (Message m : messages)
        if (m.hasLateness())
            return m.getLateness();
    
    return "UNK";
}*/