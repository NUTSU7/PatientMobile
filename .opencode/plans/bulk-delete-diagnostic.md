# Diagnostic Plan: Bulk Delete Button Not Responding

## Problem
In the "Analize Încărcate" screen, after long-pressing to enter selection mode and selecting multiple files, the "Șterge" button appears but does nothing when tapped. No confirmation dialog shows, no API calls are made.

## Investigation Steps

### Step 1: Add logging to trace the execution chain
Add `android.util.Log.d` in three key places to identify exactly where the chain breaks:

**A) In `UploadedAnalysesScreen.kt` — inside the clickable lambda (line 175):**
```kotlin
.clickable {
    android.util.Log.d("BulkDeleteDebug", "Sterge button clicked, selected=${state.selectedDocumentIds}")
    viewModel.onShowBulkDeleteDialog(state.selectedDocumentIds)
}
```

**B) In `UploadedAnalysesViewModel.kt` — at the start of `onShowBulkDeleteDialog`:**
```kotlin
fun onShowBulkDeleteDialog(ids: Set<String>) {
    android.util.Log.d("BulkDeleteDebug", "onShowBulkDeleteDialog called ids=$ids, empty=${ids.isEmpty()}")
    if (ids.isEmpty()) return
    _state.update { it.copy(bulkDeleteTargetIds = ids) }
    android.util.Log.d("BulkDeleteDebug", "State updated, bulkDeleteTargetIds=${_state.value.bulkDeleteTargetIds}")
}
```

**C) In `UploadedAnalysesScreen.kt` — before the bulk delete `AnimatedVisibility`:**
```kotlin
android.util.Log.d("BulkDeleteDebug", "Recomposing bulk delete dialog, show=${state.showBulkDeleteDialog}")
AnimatedVisibility(
    visible = state.showBulkDeleteDialog,
    ...
)
```

### Step 2: Build and run the app
Run the app, enter selection mode, select multiple files, and tap "Șterge".

### Step 3: Check logcat
Filter logs with `tag:BulkDeleteDebug` to see which messages appear.

### Step 4: Interpret results
- **If only (A) appears** → Click is registered but ViewModel method is not called (very unlikely, but would indicate a lambda capture issue).
- **If (A) and (B) appear, but (C) does not** → State is updated but `AnimatedVisibility` is not recomposing. This would indicate a Compose state tracking issue.
- **If (A), (B), and (C) all appear with `show=true`** → The dialog SHOULD be visible. This would indicate a rendering/layout issue (e.g., dialog is behind another composable, or `Crossfade` is interfering).
- **If (B) shows `empty=true`** → The selection set is somehow empty when the button is tapped. This would be the root cause.
- **If NONE appear** → The click is not being registered at all. This would indicate a hit-testing/layout issue (e.g., the button's clickable area is zero, or something is overlaying it).

## Expected Fix Based on Most Likely Root Cause
The most likely scenario is that the click IS registering but `state.selectedDocumentIds` is empty when read inside the lambda, causing the guard to silently return. If logs confirm this, the fix would be to ensure the selection state is properly persisted or read at the correct time.

If logs show the state is correct but the dialog doesn't show, the issue is likely that the `AnimatedVisibility` for bulk delete is placed at the wrong nesting level (outside the `Crossfade`'s `else` branch but not properly scoped), causing it to not recompose when expected.
