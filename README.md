# CollageView

## Changelog:

### v4.0.1:
- Fixed a bug when rebuilding grid, which was caused by items ArrayList not being populated correctly.
### v0.4.0:
- Added rebuildGrid() method. It rebuilds the current grid with a different GridAttributes instance while keeping the content;
- Introduced Item, Image, Video and Button private classes. They're used to keep track of current content type, which is useful when calling rebuildGrid();
- Added methods to release MediaRecorder instances;
- Stopped supporting any View object;
- Switched from LinearLayout to FrameLayout to let a black view behind the content. That is to guarantee that the border will appear correctly;
- Changed removeItem() to work correctly with FrameLayout;
- Fixed button size not scaling to drawable size. The buttons were matching their parent's size;
- Updated to Kotlin v1.3.1.
