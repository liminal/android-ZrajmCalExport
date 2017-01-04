package se.lightside.zrajmcalexport;

import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.inject.Inject;

import se.lightside.zrajmcalexport.db.Event;
import se.lightside.zrajmcalexport.model.CalendarModel;
import timber.log.Timber;

public class CalendarWriter {

    @Inject ContentResolver mContentResolver;

    @Inject CalendarWriter() {}

    public void writeCalendar(final DocumentFile tree, final CalendarModel calendar) {
        try {
            final String filename = filenameFor(calendar);
            Timber.v("CALENDAR: %s", filename);
            final DocumentFile         file = tree.createFile("text/plain", filename);
            final ParcelFileDescriptor pfd  = mContentResolver.openFileDescriptor(file.getUri(), "w");
            writeToDisk(pfd, calendar.getEventList());
            if (pfd != null) {
                pfd.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String filenameFor(CalendarModel calendar) throws UnsupportedEncodingException {
        return URLEncoder.encode(calendar.getName(), "UTF-8") + ".txt";
    }

    private void writeToDisk(final ParcelFileDescriptor pfd, final List<Event> eventList) {
        if (pfd == null) return;
        try (FileOutputStream     fos  = new FileOutputStream(pfd.getFileDescriptor())) {
            for (Event event : eventList) {
                fos.write(outputEvent(event));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make this output something useful... like vEvent
     */
    private byte[] outputEvent(Event event) {
        return event.toString().getBytes();
    }
}
