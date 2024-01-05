package com.segx01.testfirenase.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.segx01.testfirenase.databinding.FragmentLogsBinding

class LogsFragment : Fragment() {


    private val viewModel: LogsViewModel by viewModels()
    private val binding by lazy { FragmentLogsBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        return binding.root
    }
}