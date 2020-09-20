# CollageView

## Disclaimer
This library is distributed under Apache Software License v2.0, while mobile-ffmpeg is under LGPL 3.0. If you download CollageView from Bintray (instructions below), you agree with using mobile-ffmpeg-min package (which doesn't contain any GPL'd code), version 4.4. If you want to use a different version of mobile-ffmpeg, feel free to download the source code from this repository and change mobile-ffmpeg's version or package.

## Changelog:
### v0.6.0
- [New Feature] Introduce MediaGenerator. It is a class designed to render a CollageView into an image or a video using mobile-ffmpeg;
- [New Feature] Introduce new getters. The methods getItemsList(), getBorderColor() and getBorderSize() are now available;
- [Adjustment] In order to support ffmpeg, minSdkVersion was upped to API 24.
<details><summary>Older Versions Changelog</summary>
  
  ### v0.5.1:
  - [Bug Fix] Calling buildGrid() after adding content was leaving items array populated while leaving CollageView empty;
  - [Adjustment] Update to Kotlin v1.4.0.
  
  ### v0.5.0:
  - [New Feature] Introduce add() method. It receives a variable number of Item objects, creates the views accordinly and appends them;
  - [Deprecated] The methods addImage(), addVideo() and addButton() are now deprecated and will be removed in version 1.0.0;
  - [New Feature] Introduce fill() method. It fills the CollageView with the same Item and hooks an OnItemClickListener;
  - [Deprecated] The methods fillWithImages(), fillWithVideos() and fillWithButtons() are now deprecated and will be removed in version 1.0.0;
  - [New Feature] Introduce fillEmptySlots() method. It fills only CollageView's empty slots with the same Item and hooks an OnItemClickListener.
  - [Adjustment] Switch items from ArrayList to Array.
  
  ### v0.4.1:
  - [Bug Fix] Fixed a bug when rebuilding grid, which was caused by items ArrayList not being populated correctly.

  ### v0.4.0:
  - [New Feature] Added rebuildGrid() method. It rebuilds the current grid with a different GridAttributes instance while keeping the content;
  - [New Feature] Introduced Item, Image, Video and Button private classes. They're used to keep track of current content type, which is useful when calling rebuildGrid();
  - [New Feature] Introduce OnItemClickedListener interface. It is used to notify clicks in CollageView's items, returning the View and its index;
  - [Adjustment] Add methods to release MediaRecorder instances;
  - [Adjustment] Stop supporting any View object;
  - [Adjustment] Switch from LinearLayout to FrameLayout to let a black view behind the content. That is to guarantee that the border will appear correctly;
  - [Adjustment] Change removeItem() to work correctly with FrameLayout;
  - [Bug Fix] Fix button size not scaling to drawable size. The buttons were matching their parent's size;
  - [Adjustment] Update to Kotlin v1.3.1.
  
  ### v0.3.1
  - [Adjustment] Separate inner classes (GridAttributes and Slot) into their own files. So the user doesn't have to import the entire CollageView class when they just need Slot or GridAttributes.
  
  ### v0.3.0
  - [Adjustment] Let the user add more slots to a GridAttributes instance many times without overriding the previous array.
  
  ### v0.2.0
  - [Adjustment] Add, in buildGrid(), a call to removeAllViews() to prevent unexpected behaviour.

  ### v0.1.0:
  - [New Feature] Introduce addVideo(), addImage() and addButton() methods. To add specific types of View, which are instantiated within CollageView class;
  - [New Feature] Introduce addItem() to let the user add any type of View, which need to be instantiated by them;
  - [New Feature] Introduce fillWithItems(), fillWithVideos(), fillWithImages and fillWithButtons() to fill the CollageView with the same View;
  - [New Feature] Add getters for each type of item (VideoView, ImageView, ImageButton and View);
  - [New Feature] Introduce Slot class, used to define the position and the span of one slot inside the CollageView. It also holds two Spec attributes;
  - [New Feature] Introduce GridAttributes class, used to define the size and the slots inside the CollageView;
  - [New Feature] Introduce buildGrid() method. It uses a GridAttributes instance to create the grid inside the CollageView;
  - [New Feature] Introduce setBorderSize() methosd. It defines paddings to the child views, in order to create a border in between and around them.
</details>
