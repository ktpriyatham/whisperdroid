# WhisperDroid — Keyboard Not Displaying Hypothesis

## Identified Potential Causes

1.  **Lifecycle Management**: The `ComposeView` was being initialized with a `LifecycleOwner` that was not properly managed. Specifically, `ON_START` and `ON_RESUME` were called immediately in `onCreateInputView`, and there was no handling of `ON_PAUSE`, `ON_STOP`, or `ON_DESTROY`. This can lead to Compose disposing the composition or not rendering because it doesn't think the view is active.
2.  **Missing ViewModelStoreOwner**: `ComposeView` requires a `ViewModelStoreOwner` to be present in the view tree for many of its internal components to work correctly. The original implementation was missing this.
3.  **View Tree Owners Order**: The `setViewTreeLifecycleOwner` and other owners were being set *after* `setContent`. It is recommended to set these *before* `setContent` so that the initial composition has access to them.
4.  **Height Calculation**: `InputMethodService` windows sometimes behave unexpectedly with `ComposeView` if the view's height is not clearly determined. While the current layout should wrap content, explicit height constraints or properly handled lifecycle events (which trigger measure/layout) are crucial.

## Proposed Fixes (Implemented/Planned)

1.  **[Implemented] Proper Lifecycle Handling**: 
    - Moved `ON_START` and `ON_RESUME` to `onWindowShown()`.
    - Added `ON_PAUSE` and `ON_STOP` to `onWindowHidden()`.
    - Added `ON_DESTROY` to `onDestroy()` and when the input view is recreated.
    - This ensures that Compose only renders when the keyboard is actually visible to the user.
2.  **[Implemented] Added ViewModelStoreOwner**: 
    - Updated `KeyboardLifecycleOwner` to implement `ViewModelStoreOwner`.
    - Added `setViewTreeViewModelStoreOwner` call.
    - Many Compose components and Material3 features rely on a `ViewModelStoreOwner` being present in the ViewTree.
3.  **[Implemented] Corrected Owners Setup Order**: 
    - Moved `setViewTree...` calls before `setContent`.
    - This ensures the initial composition has access to all necessary owners.
4.  **[Implemented] Added explicit LayoutParams**:
    - Set `MATCH_PARENT` width and `WRAP_CONTENT` height for the `ComposeView`.
    - This helps `InputMethodService` correctly measure the keyboard area.
5.  **[Implemented] Added Debug Logging**: 
    - Added logs to all major lifecycle methods (`onCreate`, `onCreateInputView`, `onWindowShown`, etc.) and the main `KeyboardScreen` Composable to track execution and state.

## Further Investigation

If the keyboard still doesn't show:
- Check if the `ComposeView` requires explicit `LayoutParams`.
- Verify if `MaterialTheme` colors (like `surfaceContainer`) are correctly resolving.
- Test if removing `Surface` or changing the root container affects visibility.
