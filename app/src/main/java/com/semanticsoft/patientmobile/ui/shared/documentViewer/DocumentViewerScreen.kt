package com.semanticsoft.patientmobile.ui.shared.documentViewer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.shared.documentViewer.components.DocumentViewer
import com.semanticsoft.patientmobile.ui.theme.SurfaceWhite
import java.io.File

@Composable
fun DocumentViewerScreen(
    file: File,
    fileName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                titlePrefix = "",
                titleHighlight = fileName,
                titleSuffix = "",
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        },
        containerColor = SurfaceWhite
    ) { innerPadding ->
        DocumentViewer(
            file = file,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
