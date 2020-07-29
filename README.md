# CollageView

## Changelog:
### v0.5.0:
- [Enhancement] Switch items from ArrayList to Array;
- [New Feature] Introduce add() method. It receives a variable number of Item objects, creates the views accordinly and appends them;
- [Deprecated] The methods addImage(), addVideo() and addButton() are now deprecated and will be removed in version 1.0.0;
- [New Feature] Introduce fill() method. It fills the CollageView with the same Item and hooks an OnItemClickListener;
- [Deprecated] The methods fillWithImages(), fillWithVideos() and fillWithButtons() are now deprecated and will be removed in version 1.0.0;
- [New Feature] Introduce fillEmptySlots() method. It fills only CollageView's empty slots with the same Item and hooks an OnItemClickListener.

### v0.4.1:
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
