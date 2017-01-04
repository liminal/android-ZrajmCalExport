package se.lightside.zrajmcalexport.model;

import java.util.ArrayList;
import java.util.List;

import se.lightside.zrajmcalexport.db.Event;

public class CalendarModel {


    private final int         mId;
    private final String      mName;
    private final List<Event> mEventList;

    public CalendarModel(final int id, final String name) {
        mId = id;
        mName = name;
        mEventList = new ArrayList<>();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public List<Event> getEventList() {
        return mEventList;
    }
}
