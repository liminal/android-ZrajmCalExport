package se.lightside.zrajmcalexport.dagger2;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.sqlbrite.BriteContentResolver;
import com.squareup.sqlbrite.SqlBrite;

import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;

@Module
public class AppModule {

    @Provides
    static SqlBrite provideSqlBrite() {
        return new SqlBrite.Builder().build();
    }

    @Provides
    static BriteContentResolver provideBriteContentResolver(SqlBrite sqlBrite, Application app) {
        return sqlBrite.wrapContentProvider(app.getContentResolver(), Schedulers.io());
    }

    @Provides
    static ContentResolver provideContentResolver(Application app) {
        return app.getContentResolver();
    }

    @Provides
    static SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
