package com.tumblr.railproboston.appengine.realtime;

import java.util.*;
import java.util.logging.Logger;

public class CsvEngine {
    private static final Logger log = PredictServlet.log;
    
    public static MessageList getMessageListFromCsv(String csv, String route) {
        log.fine("Getting messages from CSV");
        log.warning("CSV: " + csv);
        if (csv == null || csv.isEmpty()) {
            log.warning("csv is null or empty; csv=" + csv);
            return null;
        }
        
        //int numNewlines = csv.split("\n").length - 1;
        //log.info("There were this many newlines in the message: " + numNewlines);
        
        String[] csvMessages = csv.split("\n", 2);
        //log.warning("CSV messages 1: " + Arrays.toString(csvMessages));
        csvMessages = csvMessages[1].split("\n"); // Removes first line before splitting
        //log.warning("CSV messages 2: " + Arrays.toString(csvMessages));
        
        List<Message> messageList = new ArrayList<Message>();
        for (String cmsg : csvMessages) {
            String[] c = cmsg.split(",", -1);
            
            if (c.length != 12) {
                log.warning("wrong length: " + c.length);
            }
            
            Message rmsg = new Message(route, c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], c[9], c[10], c[11]);
            messageList.add(rmsg);
        }
        
        String updateDate = "not available";
        
        MessageList messages = new MessageList(messageList, updateDate, route);
        
        return messages;
    }
}