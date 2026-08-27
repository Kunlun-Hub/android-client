package io.cloink.client.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.cloink.client.PlatformUtils
import io.cloink.client.ServiceAccessor
import io.cloink.client.StateListenerRegistry
import io.cloink.client.ui.theme.CloinkTheme

class HomeFragment : Fragment() {
    private lateinit var serviceAccessor: ServiceAccessor
    private lateinit var listenerRegistry: StateListenerRegistry
    private lateinit var viewModel: HomeViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceAccessor = context as? ServiceAccessor
            ?: error("$context must implement ServiceAccessor")
        listenerRegistry = context as? StateListenerRegistry
            ?: error("$context must implement StateListenerRegistry")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gateway = HomeServiceGateway(serviceAccessor, listenerRegistry)
        viewModel = ViewModelProvider(this, HomeViewModel.Factory(gateway))[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            CloinkTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    isTelevision = PlatformUtils.isAndroidTV(requireContext()),
                    onToggleConnection = viewModel::toggleConnection,
                    onOpenPeers = ::showPeers,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()
    }

    override fun onStop() {
        viewModel.stop()
        super.onStop()
    }

    private fun showPeers() {
        BottomDialogFragment().show(parentFragmentManager, BottomDialogFragment::class.java.name)
    }
}
