package se.lightside.zrajmcalexport;

import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.inject.Inject;

import se.lightside.zrajmcalexport.db.Event;
import timber.log.Timber;

public class CalendarWriter {

    @Inject ContentResolver mContentResolver;

    public void writeCalendar(final DocumentFile tree, final Pair<Integer, String> calendar, final List<Event> events) {
        try {
            final String filename = URLEncoder.encode(calendar.second, "UTF-8") + ".txt";
            Timber.v("CALENDAR: %s", filename);
            final DocumentFile         file = tree.createFile("text/plain", filename);
            final ParcelFileDescriptor pfd  = mContentResolver.openFileDescriptor(file.getUri(), "w");
            final FileOutputStream     fos  = new FileOutputStream(pfd.getFileDescriptor());
            for (Event event : events) {
                fos.write(event.toString().getBytes());
            }
            fos.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
