package logbook.plugin.attendance.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.json.JsonObject;

import logbook.api.API;
import logbook.api.APIListenerSpi;
import logbook.internal.log.LogWriter;
import logbook.plugin.attendance.bean.AttendanceEntry;
import logbook.plugin.attendance.log.AttendanceLog;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * /kcsapi/api_port/port
 *
 */
@API("/kcsapi/api_port/port")
public class ApiPortPort implements APIListenerSpi {

    private static Queue<AttendanceEntry> queue = new ArrayBlockingQueue<>(10);

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            AttendanceEntry entry;
            while ((entry = getQueue().poll()) != null) {
                entry.setEnd(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .format(ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))));
                new LogWriter<AttendanceEntry>()
                        .format(new AttendanceLog())
                        .write(entry);
            }
        }
    }

    public static Queue<AttendanceEntry> getQueue() {
        return queue;
    }
}
