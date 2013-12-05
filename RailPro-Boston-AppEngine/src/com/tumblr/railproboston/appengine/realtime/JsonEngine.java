package com.tumblr.railproboston.appengine.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonEngine {
    public static MessageList getMessageListFromJson(String json, String route) {
        UpdaterEngine.log.fine("Getting messages from JSON");
        if (json == null || json.isEmpty()) {
            UpdaterEngine.log.warning("json is null or empty; json=" + json);
            return null;
        }
        
        int numNewlines = json.split("\n").length - 1;
        if (numNewlines > 0)
            UpdaterEngine.log.info("There were this many newlines in the message: " + numNewlines);
        json = json.replaceAll("[\r\n]", "");
        
        Pattern p1 = Pattern.compile("\\{\"Messages\":(?<messages>\\[.*\\]),\"UpdateDate\":\"(?<updatedate>.+)\"\\}");
        Matcher m1 = p1.matcher(json);
        if (!m1.matches()) {
            UpdaterEngine.log.info("m1 does not match: " + json);
            return null;
        }
        String messagesList = m1.group("messages");
        String[] jsonMessages = messagesList.substring(1, messagesList.length() - 1).split("\\},?");
        for (int i = 0; i < jsonMessages.length; i++)
            jsonMessages[i] = jsonMessages[i].concat("}");
        //log.info("jsonMessages: " + Arrays.toString(jsonMessages));
        
        String updateDate = m1.group("updatedate");
        
        String lit1p2 = "\\{\"TimeStamp\":\"(?<ts>%1$s)\",\"Trip\":\"(?<tr>%2$s)\",\"Destination\":\"(?<dst>%2$s)\","
                + "\"Stop\":\"(?<st>%2$s)\",\"Scheduled\":\"(?<sch>%1$s)\",\"Flag\":\"(?<flg>%3$s)\","
                + "\"Vehicle\":\"(?<vhc>%4$s)\",\"Latitude\":\"(?<lat>%5$s)\",\"Longitude\":\"(?<lon>%5$s)\","
                + "\"Heading\":\"(?<dir>%4$s)\",\"Speed\":\"(?<spd>%4$s)\",\"Lateness\":\"(?<lte>%4$s)\"\\}";
        String lit3p2 = String.format(lit1p2, "\\d+", "[\\w\\s\\\\/]+", "[\\w\\s\\\\/]*", "\\d*", "(-?\\d+\\.\\d+)?");
        
        Pattern p2 = Pattern.compile(lit3p2);
        List<Message> messageList = new ArrayList<Message>();
        for (String jmsg : jsonMessages) {
            Matcher m2 = p2.matcher(jmsg);
            boolean b = m2.matches();
            if (!b) {
                UpdaterEngine.log.info("m2 does not match: " + jmsg);
                continue;
            }
            
            Message rmsg = new Message(route, m2.group("ts"), m2.group("tr"), m2.group("dst"),
                    m2.group("st"), m2.group("sch"), m2.group("flg"), m2.group("vhc"), m2.group("lat"),
                    m2.group("lon"), m2.group("dir"), m2.group("spd"), m2.group("lte"));
            messageList.add(rmsg);
        }
        
        MessageList messages = new MessageList(messageList, updateDate, route);
        
        return messages;
    }
    
}
