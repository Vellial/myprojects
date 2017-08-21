package homeaccApp.api;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Class for working with date and time.
 */
public class LocalDateReciever {

    // Common function for casting long to LocalDate
    public static LocalDate getLocalDateFromLong(long time) {
        Timestamp timestamp = new Timestamp(time);
        return timestamp.toLocalDateTime().toLocalDate();
    }

    // Common function for casting LocalDate to long (timestamp)
    public static long getLongTimeFromLocalDate(LocalDate date) {
        Timestamp time = Timestamp.valueOf(date.atStartOfDay());
        return time.getTime();
    }

    public static long getDateOfNow() {
        LocalDate localDate = LocalDate.now();
        Timestamp time = Timestamp.valueOf(localDate.atStartOfDay());
        return time.getTime();
    }
}
