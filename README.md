# Whatsapp-Like-PhotoEditor
It is a library module that tries to mimic the whatsapp photoeditor.
There are many things on which work needs to be done.
 
 **TODO**
 * Fix cropping issue (needs to mimic whatsapp on this)
 * Remove ugly code
 * Convert code to kotlin
 * Publish on maven

#Usage
```kotlin
 ImageEditor.Builder(this, imagePath)
                .setStickerAssets("stickers")
                .disable(ImageEditor.EDITOR_TEXT) //to disable something
                .open()
```

Here `setStickerAssets()` methods takes folder name of stickers in the assets. Checkout sample if confused. You will get the result in
`onActivityResult`

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      ImageEditor.RC_IMAGE_EDITOR ->
        if (resultCode == Activity.RESULT_OK && data != null) {
          val imagePath: String = data.getStringExtra(ImageEditor.EXTRA_EDITED_PATH)
          edited_image.setImageBitmap(BitmapFactory.decodeFile(imagePath))
        }
    }
  }
```

#Projects that helped a lot
  [ImageEditor-Android](https://github.com/siwangqishiq/ImageEditor-Android/)
  
  [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper)