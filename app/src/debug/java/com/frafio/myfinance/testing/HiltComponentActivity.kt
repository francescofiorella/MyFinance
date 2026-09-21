package com.frafio.myfinance.testing

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/** Host for `setContent { }` tests that need Hilt (`hiltViewModel()`); see https://github.com/google/dagger/issues/3394. */
@AndroidEntryPoint
class HiltComponentActivity : ComponentActivity()
