package com.segx01.testfirenase.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.segx01.testfirenase.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {


    private val viewModel: ProfileViewModel by viewModels()
    private val binding by lazy { FragmentProfileBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

}