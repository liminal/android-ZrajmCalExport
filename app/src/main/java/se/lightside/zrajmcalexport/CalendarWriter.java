package se.lightside.zrajmcalexport;

import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.inject.Inject;

import se.lightside.zrajmcalexport.db.Event;
import se.lightside.zrajmcalexport.model.CalendarModel;
import timber.log.Timber;

public class CalendarWriter {

    @Inject ContentResolver mContentResolver;

    @Inject CalendarWriter() {}

    public void writeCalendar(final DocumentFile tree, final CalendarModel calendar) {
        try {
            final String filename = URLEncoder.encode(calendar.getName(), "UTF-8") + ".txt";
            Timber.v("CALENDAR: %s", filename);
            final DocumentFile         file = tree.createFile("text/plain", filename);
            final ParcelFileDescriptor pfd  = mContentResolver.openFileDescriptor(file.getUri(), "w");
            final FileOutputStream     fos  = new FileOutputStream(pfd.getFileDescriptor());
            for (Event event : calendar.getEventList()) {
                fos.write(event.toString().getBytes());
            }
            fos.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
