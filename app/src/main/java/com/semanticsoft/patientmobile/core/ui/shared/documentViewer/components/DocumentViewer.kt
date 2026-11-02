package com.semanticsoft.patientmobile.core.ui.shared.documentViewer.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.semanticsoft.patientmobile.core.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.core.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 5f
private const val ZOOM_THRESHOLD = 1.01f

@Composable
fun DocumentViewer(
    file: File,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val renderMutex = remember { Mutex() }

    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var singleImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var renderError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(file) {
        var descriptor: ParcelFileDescriptor? = null
        val job = scope.launch(Dispatchers.IO) {
            try {
                val ext = file.extension.lowercase()
                if (ext == "jpg" || ext == "jpeg" || ext == "png") {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    withContext(Dispatchers.Main) {
                        if (bmp != null) {
                            singleImageBitmap = bmp
                            isLoading = false
                        } else {
                            renderError = true
                            isLoading = false
                        }
                    }
                    return@launch
                }

                descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(descriptor!!)
                withContext(Dispatchers.Main) {
                    renderer = pdfRenderer
                    pageCount = pdfRenderer.pageCount
                    isLoading = false
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    renderError = true
                    isLoading = false
                }
            }
        }

        onDispose {
            job.cancel()
            val r = renderer
            val d = descriptor
            scope.launch(Dispatchers.IO) {
                renderMutex.withLock {
                    try {
                        r?.close()
                        d?.close()
                    } catch (_: Exception) { }
                }
            }
        }
    }

    var scale by remember { mutableFloatStateOf(MIN_SCALE) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "Se încarcă documentul...")
                }
            }

            renderError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorDialog(
                        message = "Nu s-a putut deschide documentul",
                        onDismiss = { },
                        onRetry = null,
                        title = "Eroare"
                    )
                }
            }

            singleImageBitmap != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                                if (newScale > MIN_SCALE) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                                scale = newScale
                                if (scale <= MIN_SCALE) {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = singleImageBitmap!!.asImageBitmap(),
                        contentDescription = "Document",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            pageCount > 0 -> {
                val pages = remember(pageCount) { List(pageCount) { it } }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                                if (newScale > MIN_SCALE) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                                scale = newScale
                                if (scale <= MIN_SCALE) {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = scale <= ZOOM_THRESHOLD
                    ) {
                        itemsIndexed(pages) { index, _ ->
                            PdfPage(
                                pageIndex = index,
                                pdfRenderer = renderer,
                                renderMutex = renderMutex,
                                density = density,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPage(
    pageIndex: Int,
    pdfRenderer: PdfRenderer?,
    renderMutex: Mutex,
    density: Float,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(pageIndex, pdfRenderer) {
        val job = scope.launch(Dispatchers.IO) {
            pdfRenderer?.let { renderer ->
                try {
                    val bmp = renderMutex.withLock {
                        val page = renderer.openPage(pageIndex)
                        val width = (page.width * density).toInt()
                        val height = (page.height * density).toInt()
                        val createdBmp = Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(
                            createdBmp,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        page.close()
                        createdBmp
                    }
                    withContext(Dispatchers.Main) {
                        bitmap = bmp
                    }
                } catch (_: Exception) {
                    // Silently skip pages that fail to render
                }
            }
        }

        onDispose {
            job.cancel()
            bitmap = null
        }
    }

    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Page ${pageIndex + 1}",
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }
}
