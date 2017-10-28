package logbook.plugin.attendance.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import logbook.api.API;
import logbook.api.APIListenerSpi;
import logbook.bean.Mission;
import logbook.bean.MissionCollection;
import logbook.internal.Logs;
import logbook.internal.log.LogWriter;
import logbook.plugin.attendance.bean.AttendanceEntry;
import logbook.plugin.attendance.log.AttendanceLog;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

@API("/kcsapi/api_req_mission/start")
public class ApiReqMissionStart implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {

        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            Map<String, List<String>> param = req.getParameterMap();
            Integer deckId = Integer.valueOf(param.get("api_deck_id").get(0));
            Integer missionId = Integer.valueOf(param.get("api_mission_id").get(0));

            Mission mission = MissionCollection.get()
                    .getMissionMap()
                    .get(missionId);
            if (mission != null) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));

                new LogWriter<AttendanceEntry>()
                        .format(new AttendanceLog())
                        .write(AttendanceEntry.builder()
                                .fleet(deckId)
                                .name(mission.getName())
                                .start(Logs.DATE_FORMAT.format(now))
                                .end(Logs.DATE_FORMAT.format(now.plusMinutes(mission.getTime())))
                                .build());
            }
        }
    }
}
