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

Settle the ToothyProgress somewhere in your XML like this:

```xml
<com.goodayapps.widget.ToothyProgress
	android:id="@+id/toothyProgress"
	android:layout_width="match_parent"
	android:layout_height="80dp"
	app:progress=".5"
	app:progressColor="#ffffff"
	app:progressBackgroundColor="#959595"
	app:progressWidth="3dp"
	app:trackWidth="3dp"
	app:trackColor="#959595"
	app:strokeLineCapProgress="round"
	app:strokeLineCapProgressBackground="square"
	app:strokeLineCapTrack="square"
	app:progressBackgroundWidth="3dp"
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
