package logbook.plugin.attendance.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.json.JsonObject;

import logbook.api.API;
import logbook.api.APIListenerSpi;
import logbook.bean.AppCondition;
import logbook.bean.BattleTypes.CombinedType;
import logbook.bean.MapinfoMst;
import logbook.bean.MapinfoMstCollection;
import logbook.internal.Logs;
import logbook.plugin.attendance.bean.AttendanceEntry;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * /kcsapi/api_req_map/start
 *
 */
@API("/kcsapi/api_req_map/start")
public class ApiReqMapStart implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {

        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            Integer deckId = Integer.valueOf(req.getParameterMap()
                    .get("api_deck_id")
                    .get(0));
            Integer mapareaId = Integer.valueOf(req.getParameterMap()
                    .get("api_maparea_id")
                    .get(0));
            Integer mapInfoNo = Integer.valueOf(req.getParameterMap()
                    .get("api_mapinfo_no")
                    .get(0));

            MapinfoMst mst = MapinfoMstCollection.get()
                    .getMapinfo()
                    .values().stream()
                    .filter(i -> mapareaId.equals(i.getMapareaId()))
                    .filter(i -> mapInfoNo.equals(i.getNo()))
                    .findAny()
                    .orElse(null);
            if (mst != null) {
                {
                    AttendanceEntry bean = AttendanceEntry.builder()
                            .fleet(deckId)
                            .start(Logs.DATE_FORMAT.format(ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))))
                            .name(mst.getName())
                            .build();
                    ApiPortPort.getQueue().offer(bean);
                }
                if (CombinedType.未結成 != CombinedType.toCombinedType(AppCondition.get().getCombinedType())) {
                    AttendanceEntry bean = AttendanceEntry.builder()
                            .fleet(2)
                            .start(Logs.DATE_FORMAT.format(ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))))
                            .name(mst.getName())
                            .build();
                    ApiPortPort.getQueue().offer(bean);
                }
            }
        }
    }

}
