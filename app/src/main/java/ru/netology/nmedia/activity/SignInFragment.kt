package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.loginError) {
                binding.password.error = getString(R.string.wrong_password)
            }
        }

        with(binding) {
            login.requestFocus()
            signInButton.setOnClickListener {
//                println("pushed button")
                viewModel.loginAttempt(
                    login.editText?.text.toString(),
                    password.editText?.text.toString()
                )
            }
        }
        return binding.root
    }
}