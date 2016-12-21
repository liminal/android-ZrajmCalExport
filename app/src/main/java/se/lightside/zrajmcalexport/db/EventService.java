package se.lightside.zrajmcalexport.db;

import android.content.ContentResolver;
import android.content.EntityIterator;
import android.database.Cursor;
import android.support.v4.util.Pair;

import com.squareup.sqlbrite.BriteContentResolver;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.exceptions.Exceptions;

import static android.provider.CalendarContract.Calendars;
import static android.provider.CalendarContract.Events;
import static android.provider.CalendarContract.EventsEntity;

public class EventService {

    @Inject BriteContentResolver mBriteContentResolver;
    @Inject ContentResolver      mContentResolver;

    @Inject EventService() {}


    private static final String[] EVENT_PROJECTION = {
        Events._ID,
        Events.CALENDAR_ID,
        Events.EVENT_TIMEZONE,
        Events.DTSTART,
        Events.DTEND,
        Events.DURATION,
        Events.TITLE,
        Events.DESCRIPTION};


    public Observable<List<Event>> listCalendarEvents() {
        return Observable.fromCallable(() -> {
            try (Cursor cursor = mContentResolver.query(Events.CONTENT_URI, EVENT_PROJECTION, null, null, null)) {
                final EntityIterator iterator = EventsEntity.newEntityIterator(cursor, mContentResolver);

                final List<Event> out = new ArrayList<>();
                while (iterator.hasNext()) {
                    out.add(new Event(iterator.next()));
                }
                return out;
            } catch (SecurityException err) {
                throw Exceptions.propagate(err);
            }
        });
    }

    public Observable<List<Pair<Integer, String>>> listCalendars() {
        return mBriteContentResolver
            .createQuery(Calendars.CONTENT_URI, null, null, null, null, false)
            .mapToList(c -> Pair.create(c.getInt(c.getColumnIndex(Calendars._ID)), c.getString(c.getColumnIndex(Calendars.NAME))));

    }

}
