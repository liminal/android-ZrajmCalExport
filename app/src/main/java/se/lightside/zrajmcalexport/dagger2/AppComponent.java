package se.lightside.zrajmcalexport.dagger2;

import dagger.Component;
import se.lightside.zrajmcalexport.ui.MainActivity;

@Component(
    modules = {
        ApplicationModule.class,
        AppModule.class
    }
)
public interface AppComponent {
    void inject(MainActivity activity);
}
