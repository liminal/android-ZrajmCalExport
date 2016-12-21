package se.lightside.zrajmcalexport.db;

import android.database.Cursor;

import org.apache.commons.csv.CSVRecord;

import javax.inject.Inject;

import biweekly.component.VEvent;
import rx.functions.Func1;

public class CsvReadMapper implements Func1<Cursor, CSVRecord> {

    @Inject CsvReadMapper() {}

    @Override
    public CSVRecord call(Cursor cursor) {
        final VEvent event = new VEvent();
        return null;
    }

    private static String getString(final Cursor c, final String colName) {
        return c.getString(c.getColumnIndex(colName));
    }
}
