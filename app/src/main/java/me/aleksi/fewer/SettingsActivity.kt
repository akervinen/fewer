package me.aleksi.fewer

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val settings = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, settings)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val serverPref = findPreference<EditTextPreference>("server")
            if (serverPref != null) {
                serverPref.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
                }
                serverPref.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        val textPref = preference as EditTextPreference
                        var newVal = newValue.toString().trim()
                        if (!newVal.startsWith("http")) {
                            newVal = "http://$newVal"
                        }
                        if (newVal.endsWith("?api")) {
                            newVal = newVal.dropLast(4)
                        }
                        textPref.text = newVal
                        return@OnPreferenceChangeListener false
                    }
            }

            val testButton = findPreference<Preference>("serverTest")
            if (testButton != null) {
                testButton.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    context?.let { context ->
                        findPreference<EditTextPreference>("server")?.text?.let { text ->
                            FeverServerService.startActionTestServer(context, text)
                        }
                    }
                    return@OnPreferenceClickListener true
                }
            }
        }
    }
}
