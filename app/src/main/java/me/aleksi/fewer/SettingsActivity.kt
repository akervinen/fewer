package me.aleksi.fewer

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.*
import me.aleksi.fewer.fever.FeverServerService
import me.aleksi.fewer.fever.hashUserPassword

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

            val serverPref = findPreference<EditTextPreference>(getString(R.string.pref_server))
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

            val passPref = findPreference<EditTextPreference>(getString(R.string.pref_password))
            if (passPref != null) {
                passPref.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                passPref.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference: Preference, password: Any ->
                        val textPref = preference as EditTextPreference
                        textPref.text = ""

                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val username =
                            sharedPreferences.getString(getString(R.string.pref_username), "")

                        findPreference<EditTextPreference>(getString(R.string.pref_hash))?.let { pref ->
                            pref.text = hashUserPassword(
                                username!!,
                                password.toString()
                            )
                        }

                        return@OnPreferenceChangeListener false
                    }
            }

            val testButton = findPreference<Preference>(getString(R.string.pref_serverTest))
            if (testButton != null) {
                testButton.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    context?.let { context ->
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val server =
                            sharedPreferences.getString(getString(R.string.pref_server), "")
                        val hash =
                            sharedPreferences.getString(getString(R.string.pref_hash), "")

                        FeverServerService.startActionTestServer(context, server!!, hash)
                    }
                    return@OnPreferenceClickListener true
                }
            }

            val dayNightPref = findPreference<DropDownPreference>(getString(R.string.pref_daynight))
            if (dayNightPref != null) {
                dayNightPref.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        setDefaultNightMode(
                            when (newValue) {
                                "0" -> MODE_NIGHT_FOLLOW_SYSTEM
                                "1" -> MODE_NIGHT_NO
                                "2" -> MODE_NIGHT_YES
                                else -> MODE_NIGHT_FOLLOW_SYSTEM
                            }
                        )
                        return@OnPreferenceChangeListener true
                    }
            }
        }
    }
}
