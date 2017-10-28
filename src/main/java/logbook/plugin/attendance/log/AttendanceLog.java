package logbook.plugin.attendance.log;

import java.util.StringJoiner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import logbook.internal.log.LogFormatBase;
import logbook.plugin.attendance.bean.AttendanceEntry;

public class AttendanceLog extends LogFormatBase<AttendanceEntry> {

    @Override
    public String format(AttendanceEntry entry) {
        try {
            return new StringJoiner(",")
                    .add(nowString())
                    .add("遠征")
                    .add(String.valueOf(entry.getFleet()))
                    .add(new ObjectMapper().writeValueAsString(entry))
                    .toString();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String header() {
        return new StringJoiner(",")
                .add("日付")
                .add("種類")
                .add("艦隊")
                .add("データ")
                .toString();
    }

    @Override
    public String name() {
        return "勤務記録";
    }
}
