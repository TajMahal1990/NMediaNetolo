package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseMessaging.token.addOnSuccessListener {
            println("Your current token is: $it")
        }

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.navigation)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        checkGoogleApiAvailability()

        var currentMenuProvider: MenuProvider? = null
        viewModel.data.observe(this) {
            currentMenuProvider?.also(::removeMenuProvider)

            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_main, menu)
                    val authorized = viewModel.authorized
                    menu.setGroupVisible(R.id.authorized, authorized)
                    menu.setGroupVisible(R.id.unauthorized, !authorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signIn -> {
//                            appAuth.setAuth(5, "x-token")
                            findNavController(R.id.navigation).navigate(R.id.action_feedFragment_to_signInFragment)
                            true
                        }
                        R.id.signUp -> {
//                            appAuth.setAuth(5, "x-token")
                            findNavController(R.id.navigation).navigate(R.id.action_feedFragment_to_signUpFragment)
                            true
                        }
                        R.id.logout -> {
                            appAuth.removeAuth()
                            true
                        }
                        else -> false
//                        else -> onMenuItemSelected(menuItem)
                    }
            }.apply {
                currentMenuProvider = this
            })
        }
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }
    }
}