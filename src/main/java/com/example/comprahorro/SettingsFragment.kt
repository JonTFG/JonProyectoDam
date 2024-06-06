package com.example.comprahorro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        val modeAppSwitch = findPreference<SwitchPreference>("modeApp")
        val flashSwitch = findPreference<SwitchPreference>("flash")
        val termsPreference = findPreference<Preference>("terms")
        val sinCodPreference = findPreference<Preference>("sinCod")
        val creadorPreference = findPreference<Preference>("creador")
        val objetivoPreference = findPreference<Preference>("objetivo")
        val logoutPreference = findPreference<Preference>("logout")
        val licenciaPreference = findPreference<Preference>("licencia")

        modeAppSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val isDarkMode = newValue as Boolean
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean("modeApp", isDarkMode)
                .apply()


            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            requireActivity().recreate()
            true
        }

        flashSwitch?.setOnPreferenceChangeListener { _, newValue ->

            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean("flash", newValue as Boolean)
                .apply()
            true
        }

        termsPreference?.setOnPreferenceClickListener {

            showDialogWithTermsAndConditions()
            true
        }

        sinCodPreference?.setOnPreferenceClickListener {

            showSinCodBar()
            true
        }

        creadorPreference?.setOnPreferenceClickListener {
            showCreador()
            true
        }

        licenciaPreference?.setOnPreferenceClickListener {
            showLicences()
            true
        }
        objetivoPreference?.setOnPreferenceClickListener {
            showObjetivo()
            true
        }

        logoutPreference?.setOnPreferenceClickListener {
            logout()
            true
        }
    }

    private fun showDialogWithTermsAndConditions() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Términos y condiciones")
            .setMessage("**Términos y Condiciones de Uso**\n" +
                    "\n" +
                    "1. **Aceptación de los Términos**\n" +
                    "Al utilizar nuestra aplicación, aceptas estos términos y condiciones en su totalidad; en consecuencia, si no estás de acuerdo con estos términos y condiciones o cualquier parte de estos términos y condiciones, no debes utilizar nuestra aplicación.\n" +
                    "\n" +
                    "2. **Licencia para usar la aplicación**\n" +
                    "A menos que se indique lo contrario, nosotros o nuestros licenciantes somos propietarios de los derechos de propiedad intelectual de la aplicación y del material de la aplicación. Sujeto a la licencia a continuación, todos estos derechos de propiedad intelectual están reservados.\n" +
                    "\n" +
                    "3. **Uso Aceptable**\n" +
                    "No debes utilizar nuestra aplicación de ninguna manera que cause, o pueda causar, daño a la aplicación o deterioro de la disponibilidad o accesibilidad de la aplicación.\n" +
                    "\n" +
                    "4. **Limitación de Responsabilidad**\n" +
                    "Nuestra aplicación y el contenido de nuestra aplicación se proporcionan gratuitamente, no podemos ser responsables de ninguna pérdida o daño de cualquier naturaleza.\n" +
                    "\n" +
                    "5. **Variación**\n" +
                    "Podemos revisar estos términos y condiciones en cualquier momento.\n" +
                    "\n" +
                    "6. **Ley y Jurisdicción**\n" +
                    "Estos términos y condiciones se regirán e interpretarán de acuerdo con las leyes de España, y cualquier disputa relacionada con estos términos y condiciones estará sujeta a la jurisdicción exclusiva de los tribunales de Murcia, España.")
            .setPositiveButton("Aceptar", null)
            .create()

        alertDialog.show()
    }

    private fun showSinCodBar() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("Si el producto no tiene código de barras no podrá ser escaneado, por lo tanto no se podrá usar esta aplicación para buscar el precio de ese producto.")
            .setPositiveButton("Aceptar", null)
            .create()

        alertDialog.show()
    }

    private fun showCreador() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("El creador de esta aplicación es un simple estudiante con el deseo de aprobar su TFG.")
            .setPositiveButton("Aceptar", null)
            .create()
        alertDialog.show()
    }

    private fun showObjetivo() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("El objetivo de esta aplicación es conseguir una mayor eficacia a la hora de realizar la compra de alimentos, pudiendo hacer diferentes listas para cada supermercado y con los productos más baratos.")
            .setPositiveButton("Aceptar", null)
            .create()
        alertDialog.show()
    }

    private fun logout() {
        // Cerrar sesión de Firebase
        FirebaseAuth.getInstance().signOut()

        // Redirigir a la pantalla de inicio de sesión
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    private fun showLicences() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("compraHorro© 2024 by Jonathan Vélez Ordóñez is licensed under Creative Commons Attribution-ShareAlike 4.0 International\n")
            .setPositiveButton("Aceptar", null)
            .create()
        alertDialog.show()
    }
}
