package se.lightside.zrajmcalexport.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.MainThreadSubscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.lightside.zrajmcalexport.CalendarWriter;
import se.lightside.zrajmcalexport.R;
import se.lightside.zrajmcalexport.dagger2.AppComponent;
import se.lightside.zrajmcalexport.dagger2.ApplicationModule;
import se.lightside.zrajmcalexport.dagger2.DaggerAppComponent;
import se.lightside.zrajmcalexport.db.Event;
import se.lightside.zrajmcalexport.db.EventService;
import se.lightside.zrajmcalexport.model.CalendarModel;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int    REQUEST_CODE_STORAGE = 123;
    private static final String PREF_OUTPUT_DIR      = "outputDir";

    @Inject SharedPreferences mSharedPreferences;
    @Inject EventService      mEventService;
    @Inject CalendarWriter mCalendarWriter;

    private Button       mBtnListCalendars;
    private Subscription mPrefSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
        setContentView(R.layout.activity_main);

        final RxPermissions rxPermissions = new RxPermissions(this);

        RxView.clicks(findViewById(R.id.btn_pick_output_dir))
              .subscribe(i -> pickOutputDir());

        mBtnListCalendars = (Button) findViewById(R.id.btn_list_calendars);

        RxView.clicks(mBtnListCalendars)
              .compose(rxPermissions.ensure(Manifest.permission.READ_CALENDAR))
              .subscribe(granted -> {
                  Timber.v("clicklick? %b", granted);
                  listEvents();
              });

    }

    @Override protected void onResume() {
        super.onResume();

        mPrefSubscription = listenForOutputDir()
            .subscribe(uri -> mBtnListCalendars.setEnabled(!Uri.EMPTY.equals(uri)));
    }

    @Override protected void onPause() {
        if (mPrefSubscription != null) {
            mPrefSubscription.unsubscribe();
            mPrefSubscription = null;
        }
        super.onPause();
    }

    @Override protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_STORAGE && resultCode == Activity.RESULT_OK) {
            Timber.v("onActivityResult(requestCode=%d, resultCode=%d, data=%s)",
                     requestCode, resultCode, data);
            final int takeFlags = data.getFlags()
                                  & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                     | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            final Uri dirUri = data.getData();
            //noinspection WrongConstant
            getContentResolver().takePersistableUriPermission(dirUri, takeFlags);

            mSharedPreferences.edit().putString(PREF_OUTPUT_DIR, dirUri.toString()).apply();

        }
    }

    private void pickOutputDir() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE);
    }


    private void listEvents() {
        Observable.zip(
            mEventService.listCalendars(),
            mEventService.listCalendarEvents(),
            Pair::create)
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                      plist -> onEventList(plist.first, plist.second),
                      t -> Timber.e(t, t.getLocalizedMessage()));
    }


    private void onEventList(final List<CalendarModel> calendars, List<Event> entities) {

        SparseArray<CalendarModel> calSA = new SparseArray<>();
        for (CalendarModel calendar : calendars) {
            calSA.put(calendar.getId(), calendar);
        }

        for (Event entity : entities) {
            final CalendarModel cal = calSA.get(entity.calendar_id);
            if (cal == null) {
                Timber.v("No calendar for entry: %s", entity);
            } else {
                cal.getEventList().add(entity);
            }
        }

        final Uri outputDir = Uri.parse(mSharedPreferences.getString(PREF_OUTPUT_DIR, ""));

        DocumentFile tree = DocumentFile.fromTreeUri(this, outputDir);

        for (CalendarModel calendar : calendars) {
                mCalendarWriter.writeCalendar(tree, calendar);
        }
    }


    private Observable<Uri> listenForOutputDir() {
        return Observable.create(new OutputDirPreferenceChangeOnSubscribe(mSharedPreferences))
                         .map(Uri::parse);
    }

    static class OutputDirPreferenceChangeOnSubscribe
        implements Observable.OnSubscribe<String> {

        private final SharedPreferences mSharedPreferences;

        OutputDirPreferenceChangeOnSubscribe(final SharedPreferences sharedPreferences) {mSharedPreferences = sharedPreferences;}

        @Override public void call(final Subscriber<? super String> subscriber) {
            MainThreadSubscription.verifyMainThread();

            final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
                    if (!subscriber.isUnsubscribed()) {
                        if (PREF_OUTPUT_DIR.equals(key)) {
                            subscriber.onNext(mSharedPreferences.getString(key, ""));
                        }
                    }
                }
            };


            subscriber.add(new MainThreadSubscription() {
                @Override protected void onUnsubscribe() {
                    mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
                }
            });

            mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
            // Emit initial event
            subscriber.onNext(mSharedPreferences.getString(PREF_OUTPUT_DIR, ""));
        }
    }


    private AppComponent appComponent() {
        return DaggerAppComponent.builder()
                                 .applicationModule(new ApplicationModule(getApplication()))
                                 .build();
    }

}

