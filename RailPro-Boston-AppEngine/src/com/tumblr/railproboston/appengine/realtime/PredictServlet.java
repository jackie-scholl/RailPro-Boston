package com.tumblr.railproboston.appengine.realtime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PredictServlet extends HttpServlet {
    static final Logger log = PredictServlet.getLogger(new Object() {});
    private static final long serialVersionUID = -8113582487529458893L;
    
    public static final Logger getLogger(Object obj) {
        return Logger.getLogger(obj.getClass().getEnclosingClass().getName());
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Query string is: " + req.getQueryString());
        
        CheckIn checkin = getCheckin(req);
        PredictionEngine e = new PredictionEngine();
        String predictedLateness = e.getPredictedLateness(checkin);
        
        resp.setContentType("text/plain");
        resp.getWriter().print(predictedLateness);
    }
    
    private static CheckIn getCheckin(HttpServletRequest req) {
        String route = decode(req.getParameter("routeid"));
        String trip = decode(req.getParameter("tripid"));
        String station = decode(req.getParameter("station"));
        CheckIn checkin = new CheckIn(route, trip, station);
        return checkin;
    }
    
    private static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.log(Level.SEVERE, "Can't decode given parameter string: " + str, e);
            return null;
        }
    }
    
    /*private static CheckIn getCheckin(HttpServletRequest req) {
        @SuppressWarnings("unchecked")
        Map<String, String[]> parameterMap = (Map<String, String[]>) req.getParameterMap();
        
        String route = decode(parameterMap.get("routeid")[0]);
        String trip = decode(parameterMap.get("tripid")[0]);
        String station = decode(parameterMap.get("station")[0]);
        Date scheduled = parseTime(decode(parameterMap.get("scheduled")[0]));
        CheckIn checkin = new CheckIn(route, trip, station, scheduled);
        log.info("Checkin is: " + checkin);
        
        return checkin;
    }
    
    private static Date parseTime(String scheduledTime) {
        DateFormat myTimeFormat = new SimpleDateFormat("HH:mm");
        try {
            return myTimeFormat.parse(scheduledTime);
        } catch (ParseException e) {
            log.log(Level.WARNING, "Can't parse scheduled time: " + scheduledTime, e);
            return null;
        }
    }
    
    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }*/
    
    
    private static final String[] lines = { "CR-Greenbush", "CR-Kingston", "CR-Middleborough", "CR-Fairmount",
            "CR-Providence", "CR-Franklin", "CR-Needham", "CR-Framingham", "CR-Fitchburg", "CR-Lowell", "CR-Haverhill",
            "CR-Newbury" };
    
    public static List<String> getLineNames() {
        return Arrays.asList(lines);
    }
    
    private static Map<String, Integer> lineNumbers;
    
    private static Map<String, Integer> getLineNumberMap() {
        if (lineNumbers == null) {
            lineNumbers = new HashMap<String, Integer>();
            for (int i = 0; i < lines.length; i++) {
                lineNumbers.put(lines[i], i + 1);
            }
        }
        return lineNumbers;
    }
    
    public static int getLineNumber(String route) {
        return getLineNumberMap().get(route);
    }
}
