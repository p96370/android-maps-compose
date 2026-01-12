package com.isi.sameway.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.isi.sameway.theme.backgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomSheetScreen(
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetContent: @Composable (ColumnScope.() -> Unit),
    content: @Composable () -> Unit,
) {
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 48.dp,
        sheetContent = sheetContent,
        sheetContainerColor = backgroundColor
    ) {
        content()
    }
}

