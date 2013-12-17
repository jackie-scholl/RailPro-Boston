package com.tumblr.railproboston.appengine.realtime;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.appengine.api.urlfetch.*;


public class CsvExporter {
    private static final Logger log = Logger.getLogger(CsvExporter.class.getName());
    
    private CsvDatabaseEngine dbEngine;
    
    public CsvExporter() {
        dbEngine = new CsvDatabaseEngine();
    }
    
    public boolean exportToFusionTables(Date start, Date end, String route) {
        URL url = null;
        try {
            url = new URL("https://www.googleapis.com/upload/fusiontables/v1/tables/" +
                    "1Jpr7h_nx4oK1G7JASIYaUeUU509jxpSq4nuJVg8/import");
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Failed to parse URL", e);
            return false;
        }
        
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();
        
        List<String> csvStrings = getCsvStrings(start, end, route);
        for (String csv : csvStrings) {
            HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
            request.setPayload(csv.getBytes());
            
            try {
                HTTPResponse response = service.fetch(request);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to send request " + request, e);
            }
        }
        
        return true;
    }
    
    
    public List<String> getCsvStrings(Date start, Date end, String route) {
        Map<Date, String> csvMap = dbEngine.getCsv(start, end, route);
        csvMap.putAll(dbEngine.getCsvOld(start, end, route));
        
        List<String> csvStrings = new ArrayList<>();
        csvStrings.addAll(csvMap.values());
        return csvStrings;
    }
    
    public byte[] getGZippedData(Date start, Date end, String route) {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        
        try (OutputStream gzipOut = new GZIPOutputStream(bytesOut)) {
            getZip(start, end, route, gzipOut);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to write zip stream", e);
        }
        
        return bytesOut.toByteArray();
    }
    
    private ZipOutputStream getZip(Date start, Date end, String route, OutputStream os) throws IOException {
        Map<Date, String> csvMap = dbEngine.getCsv(start, end, route);
        csvMap.putAll(dbEngine.getCsvOld(start, end, route));
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
        
        try {
            for (Date date : csvMap.keySet()) {
                String filename = getFilename(date, route);
                byte[] bytes = csvMap.get(date).getBytes();
                ZipEntry entry = new ZipEntry(filename);
                zos.write(bytes);
                zos.closeEntry();
            }
        } finally {
            zos.close();
        }
        
        return zos;
    }
    
    private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd//kk-mm-ss-SSSS");
    
    private static String getFilename(Date date, String route) {
        return String.format("%s//%s", route, dateTimeFormat.format(date));
    }
}
