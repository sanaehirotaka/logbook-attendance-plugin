package logbook.plugin.attendance.gui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.SourceGridView;
import com.calendarfx.view.page.DayPage;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.fxml.FXML;
import logbook.bean.AppConfig;
import logbook.internal.LoggerHolder;
import logbook.internal.Logs;
import logbook.internal.gui.WindowController;
import logbook.internal.log.LogWriter;
import logbook.plugin.attendance.bean.AttendanceEntry;
import logbook.plugin.attendance.log.AttendanceLog;

public class AttendanceController extends WindowController {

    /** タイムゾーン */
    private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Tokyo");

    @FXML
    private SourceGridView grid;

    @FXML
    private DayPage view;

    private ObjectMapper mapper = new ObjectMapper();

    @FXML
    void initialize() {
        view.getDetailedDayView().setShowAllDayView(false);
        CalendarSource cs = new CalendarSource();
        Path logFile = Paths.get(AppConfig.get().getReportPath(), new AttendanceLog().fileName());
        try (Stream<String> lines = Files.lines(logFile, LogWriter.DEFAULT_CHARSET)) {
            LocalDate limit = LocalDate.now().minusDays(14);
            Map<Integer, List<Entry<Integer>>> log = lines.skip(1)
                    .map(this::parseEntry)
                    .filter(Objects::nonNull)
                    .filter(e -> e.getStartDate().compareTo(limit) > 0)
                    .collect(Collectors.groupingBy(Entry<Integer>::getUserObject));
            log.keySet()
                    .stream()
                    .sorted()
                    .map(i -> {
                        Calendar cal = new Calendar("第" + i + "艦隊");
                        cal.setStyle(Calendar.Style.values()[i]);
                        cal.addEntries(new ArrayList<>(log.get(i)));
                        return cal;
                    })
                    .forEach(cs.getCalendars()::add);

        } catch (Exception e) {
            LoggerHolder.get().warn("勤務記録を読み込み中に例外", e);
        }
        grid.getCalendarSources().add(cs);
        view.getCalendarSources().add(cs);
    }

    private Entry<Integer> parseEntry(String line) {
        try {
            String[] columns = line.split(",", 4);
            Integer fleet = Integer.valueOf(columns[2]);
            AttendanceEntry readEntry = mapper.readValue(columns[3], AttendanceEntry.class);
            Entry<Integer> entry = new Entry<>(readEntry.getName());
            ZonedDateTime start = ZonedDateTime.of(LocalDateTime.from(Logs.DATE_FORMAT.parse(readEntry.getStart())), TIME_ZONE);
            ZonedDateTime end = ZonedDateTime.of(LocalDateTime.from(Logs.DATE_FORMAT.parse(readEntry.getEnd())), TIME_ZONE);
            entry.setInterval(new Interval(start, end));
            entry.setUserObject(fleet);
            return entry;
        } catch (Exception e) {
            LoggerHolder.get().warn("勤務記録を読み込み中に例外", e);
            return null;
        }
    }
}
