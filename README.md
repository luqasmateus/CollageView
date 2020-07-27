# CollageView

## Newest Additions:
### v1.1.0:
- Added rebuildGrid() method. It rebuilds the current grid with a different GridAttributes instance while keeping the content;
- Introduced Item, Image, Video and Button private classes. They're used to keep track of current content type, which is useful when calling rebuildGrid();
- Added methods to release MediaRecorder instances;
- Stopped supporting any View object;
- Updated to Kotlin v1.3.1.
