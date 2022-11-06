# EasyMediaPicker
Anroid library handling media picker from camera and gallery

<img src="[https://github.com/favicon.ico](https://github.com/BasemNasr/EasyMediaPicker/blob/master/screen1.png)" width="48">


- Select Image From Gallery
- Capture Image From Gallery
- Select Video From Gallery
- We can using this library for compressing images in android (befor upload it to server)
- Compressing Image in small sizes witout losing image quality 

# Download
--------
Gradle:

```gradle
repositories {
   maven { url 'https://jitpack.io' }
}

dependencies {
   implementation 'com.github.BasemNasr:EasyMediaPicker:0.0.2'
}
```



# Easy Way To Using Library
```kotlin
val originalImagePath = "/originalImagePath"

val compressedImagePath = BeforeUploadImages()
           .resizeAndCompressImageBeforeSend(this, originalImagePath, File(originalImagePath).name)

```



Pull requests are the best way to propose changes to the codebase (we use [Github Flow](https://guides.github.com/introduction/flow/index.html)). We actively welcome your pull requests:

1. Fork the repository and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code lints.
6. Issue that pull request!
7. Always add a `README` and/or `requirements.txt` to your added code.

## Report bugs using Github's [issues](https://github.com/BasemNasr/CompressingImage/issues)
We use GitHub issues to track public bugs. Report a bug by opening a new issue it's that easy!

*Great Bug Reports* tend to have:

- A quick summary and/or background
- Steps to reproduce
    - Be specific!
    - Give sample code if you can.
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

People love thorough bug reports. I'm not even kidding.

## References
This document was adapted from the our contribution guidelines for [Facebook](https://www.facebook.com/AlalmiyaAlhura)




