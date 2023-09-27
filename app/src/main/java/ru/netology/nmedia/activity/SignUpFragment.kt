package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.registrationError)
                Snackbar.make(binding.root, R.string.error_loading, LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        viewModel.registrationUser(
                            binding.login.editText?.text.toString(),
                            binding.password.editText?.text.toString(),
                            binding.name.editText?.text.toString()
                        )
                    }.show()
        }

        with(binding) {
            signUpButton.setOnClickListener {
                if (password.editText?.text.toString() == repeatPassword.editText?.text.toString()) {
                    viewModel.registrationUser(
                        login.editText?.text.toString(),
                        password.editText?.text.toString(),
                        name.editText?.text.toString()
                    )
                } else {
                    repeatPassword.error = getString(R.string.wrong_password)
                }
            }
        }
        return binding.root
    }
}