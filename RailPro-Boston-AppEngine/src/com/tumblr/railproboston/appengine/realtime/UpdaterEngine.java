package com.tumblr.railproboston.appengine.realtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.DeferredTask;

public class UpdaterEngine {
    static final Logger log = Logger.getLogger(UpdaterEngine.class.getName());
    
    public UpdaterEngine() {}
    
    public static DeferredTask getDeferredTaskUpdateAll() {
        return new DeferredTask() {
            private static final long serialVersionUID = 1L;
            
            public void run() {
                log.fine("Running update task");
                UpdaterEngine engine = new UpdaterEngine();
                boolean b = engine.updateJsonForAll();
                
                if (!b) {
                    throw new RuntimeException("Updates failed");
                }
            }
        };
    }
    
    private boolean updateJsonForAll() {
        Map<String, String> jsonMap = getJsonForAll();
        PredictionEngine e = new PredictionEngine();
        e.updatePredictionsAsync(jsonMap);
        JsonDatabaseEngine db = new JsonDatabaseEngine();
        boolean b = db.putJson(jsonMap);
        
        return b;
    }
    
    private Map<String, String> getJsonForAll() {
        List<String> routes = getRoutes();
        Map<String, FutureTask<String>> tasks = new HashMap<>();
        for (String route : routes) {
            FutureTask<String> t = new FutureTask<>(getJsonForRouteAsync(route));
            t.run();
            tasks.put(route, t);
        }
        
        Map<String, String> results = new HashMap<>();
        for (String route : routes) {
            FutureTask<String> t = tasks.get(route);
            String res;
            try {
                res = t.get();
                results.put(route, res);
            } catch (InterruptedException | ExecutionException e) {
                log.log(Level.WARNING, "Failed to fetch result for route " + route, e);
                results.put(route, null);
            }
        }
        
        return results;
    }
    
    
    private static List<String> getRoutes() {
        return PredictServlet.getLineNames();
        //return Arrays.asList(new String[] { "CR-Providence" });
    }
    
    private Callable<String> getJsonForRouteAsync(String route) {
        final String rt = route;
        return new Callable<String>() {
            public String call() {
                return getJsonForRouteSync(rt);
            }
        };
    }
    
    private String getJsonForRouteSync(String route) {
        return fetchUrl(getJsonFeedUrl(route));
    }
    
    private static URL getJsonFeedUrl(String route) {
        try {
            return new URL(String.format("http://developer.mbta.com/lib/RTCR/RailLine_%d.json",
                    PredictServlet.getLineNumber(route)));
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "Failed to parse feed url", e);
        }
        return null;
    }
    
    private static String fetchUrl(URL url) {
        if (url == null) {
            log.warning("url is null");
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openStream(url)))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            return sb.toString();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to fetch URL " + url, e);
        }
        
        return null;
    }
    
    private static InputStream openStream(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(20 * 1000);
        connection.setReadTimeout(20 * 1000);
        return connection.getInputStream();
    }
    
    
    /*public boolean updateMessagesForAll() {
        List<Thread> threads = new ArrayList<>();
        for (String route : getRoutes()) {
            Runnable r = getUpdateMessagesForRouteRunnable(route);
            Thread t = ThreadManager.currentRequestThreadFactory().newThread(r);
            threads.add(t);
        }
        
        final int WAIT_SECONDS = 60 * 5;
        return startJoinAllThreads(threads, 1000 * WAIT_SECONDS);
    }
    
    private Runnable getUpdateMessagesForRouteRunnable(String route) {
        final String rt = route;
        return new Runnable() {
            public void run() {
                updateMessagesForRouteSync(rt);
            }
        };
    }
    
    private static boolean startJoinAllThreads(List<Thread> threads, long millis) {
        for (Thread t : threads)
            t.start();
        
        for (Thread t : threads) {
            try {
                t.join(millis);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Interrupted while waiting on thread", e);
                return false;
            }
        }
        
        for (Thread t : threads)
            if (t.isAlive())
                return false;
        
        return true;
    }
    
    public void updateMessagesForRouteSync(String route) {
        DatabaseEngine databaseEngine = new DatabaseEngine();
        List<Message> messages = getMessagesForRoute(route);
        log.fine("About to store messages for the route " + route + " in the database");
        boolean b = databaseEngine.putMessagesSync(messages, route);
        if (!b)
            log.info("Failed to store messages for the route " + route + " in the database");
    }
    
    private List<Message> getMessagesForRoute(String route) {
        String json = fetchUrl(getJsonFeedUrl(route));
        return JsonEngine.getMessageListFromJson(json, route).getMessages();
    }*/
}
