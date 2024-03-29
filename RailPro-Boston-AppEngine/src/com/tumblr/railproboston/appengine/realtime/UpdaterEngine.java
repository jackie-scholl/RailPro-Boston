package com.tumblr.railproboston.appengine.realtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
                boolean b = engine.updateCsvForAll();
                
                if (!b) {
                    throw new RuntimeException("Updates failed");
                }
            }
        };
    }
    
    private boolean updateCsvForAll() {
        Date time = new Date();
        Map<String, String> csvMap = getCsvForAll();
        PredictionEngine e = new PredictionEngine();
        e.updatePredictionsAsync(csvMap);
        CsvDatabaseEngine db = new CsvDatabaseEngine();
        boolean b = db.putCsv(csvMap, time);
        
        return b;
    }
    
    private Map<String, String> getCsvForAll() {
        List<String> routes = getRoutes();
        Map<String, FutureTask<String>> tasks = new HashMap<>();
        for (String route : routes) {
            FutureTask<String> t = new FutureTask<>(getCsvForRouteAsync(route));
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
    
    private Callable<String> getCsvForRouteAsync(String route) {
        final String rt = route;
        return new Callable<String>() {
            public String call() {
                return getCsvForRouteSync(rt);
            }
        };
    }
    
    private String getCsvForRouteSync(String route) {
        return fetchUrl(getCsvFeedUrl(route));
    }
    
    private static URL getCsvFeedUrl(String route) {
        try {
            return new URL(String.format("http://developer.mbta.com/lib/RTCR/RailLine_%d.csv",
                    PredictServlet.getLineNumber(route)));
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "Failed to parse feed url", e);
        }
        return null;
    }
    
    /*private static URL getJsonFeedUrl(String route) {
        try {
            return new URL(String.format("http://developer.mbta.com/lib/RTCR/RailLine_%d.json",
                    PredictServlet.getLineNumber(route)));
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "Failed to parse feed url", e);
        }
        return null;
    }*/
    
    private static String fetchUrl(URL url) {
        if (url == null) {
            log.warning("url is null");
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openStream(url)))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
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
}
