# ToothyProgress

[![](https://jitpack.io/v/TalbotGooday/ToothyProgress.svg)](https://jitpack.io/#TalbotGooday/ToothyProgress)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)]()

A polyline determinated ProgressBar written in Kotlin

<img src="/screenshots/Screenshot_1599544460.png" width=32%/>

## Getting started

Add to your root build.gradle:
```Groovy
allprojects {
	repositories {
	    ...
	    maven { url "https://jitpack.io" }
	}
}
```

Add the dependency:
```Groovy
dependencies {
      implementation 'com.github.TalbotGooday:ToothyProgress:x.x.x'
}
```

## Code example

Settle the wave somewhere in your XML like this:

```xml
<rm.com.audiowave.AudioWaveView
    android:id="@+id/wave"
    android:layout_width="match_parent"
    android:layout_height="32dp"
    android:layout_margin="16dp"
    app:animateExpansion="false"
    app:chunkWidth="3dp"
    app:chunkHeight="24dp"
    app:minChunkHeight="2dp"
    app:chunkSpacing="1dp"
    app:chunkRadius="1dp"
    app:touchable="true"
    app:waveColor="@android:color/white"
    />
```
Seekbar-like listener:
```kotlin
toothyProgress.setListener(object : ToothyProgress.Listener {
	override fun onProgressChanged(progress: Float, fromUser: Boolean) {
		// invokes every time the progress's been changed
	}
	override fun onStartTrackingTouch(progress: Float) {
		// invokes when user touches the view
	}
	override fun onStopTrackingTouch(progress: Float) {
		// invokes when user releases the touch
	}
})
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
