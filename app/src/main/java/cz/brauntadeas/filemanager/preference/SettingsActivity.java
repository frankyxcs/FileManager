package cz.brauntadeas.filemanager.preference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cz.brauntadeas.filemanager.R;

public class SettingsActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new PreferenceFragment()).commit();
    }

    public static class PreferenceFragment extends android.support.v14.preference.PreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
