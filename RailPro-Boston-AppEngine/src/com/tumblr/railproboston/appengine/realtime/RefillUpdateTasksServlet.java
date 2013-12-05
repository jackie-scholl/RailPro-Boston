package com.tumblr.railproboston.appengine.realtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.QueueStatistics;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TransientFailureException;

public class RefillUpdateTasksServlet extends HttpServlet {
    private static final Logger log = PredictServlet.getLogger(new Object() {});
    private static final long serialVersionUID = -8113582487529458893L;
    
    public static final String UPDATES_QUEUE_NAME = "updates";
    
    private static final int QUEUE_LENGTH_SECONDS = 60 * 10;
    private static final double MAX_RATE = 1.0 / 60.0;
    
    private Queue queue;
    
    public RefillUpdateTasksServlet() {
        queue = QueueFactory.getQueue(UPDATES_QUEUE_NAME);
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Task update refiller cron started");
        refillTasks();
        
        resp.setContentType("text/plain");
        resp.getWriter().print("cron");
    }
    
    private boolean refillTasks() {
        int tasksNeeded = tasksNeeded();
        
        log.info("Tasks needed: " + tasksNeeded);
        TaskOptions task = RefillUpdateTasksServlet.getTaskOptionsUpdateMessages();
        List<TaskHandle> taskHandles = new ArrayList<>();
        for (int i = 0; i < tasksNeeded; i++) {
            TaskOptions t = new TaskOptions(task);
            TaskHandle handle = queue.add(t);
            log.fine("Added task to queue; etaMillis=" + handle.getEtaMillis());
            taskHandles.add(handle);
        }
        
        return true;
    }
    
    private int tasksNeeded() throws TransientFailureException {
        CapabilityStatus status = CapabilitiesServiceFactory.getCapabilitiesService().getStatus(Capability.TASKQUEUE)
                .getStatus();
        
        log.info("Status of task queue: " + status);
        
        QueueStatistics stats = null;
        int currentTasks = 0;
        try {
            stats = queue.fetchStatistics();
            currentTasks = stats.getNumTasks();
        } catch (TransientFailureException e) {
            log.log(Level.WARNING, "Could not fetch statistics", e);
        }
        
        double neededTasks = QUEUE_LENGTH_SECONDS * MAX_RATE - currentTasks;
        
        return (int) Math.ceil(neededTasks);
    }
    
    public static TaskOptions getTaskOptionsUpdateMessages() {
        return TaskOptions.Builder.withPayload(UpdaterEngine.getDeferredTaskUpdateAll());
    }
    
}
