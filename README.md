<a href="https://helpcrunch.com/"><p align="center"><img alt="flow" width="400" src="/screenshots/2.png"></p></a>

<p align=center>
<a href="https://jitpack.io/#TalbotGooday/ToothyProgress"><img src="https://jitpack.io/v/TalbotGooday/ToothyProgress.svg" /></a>
<img alt="Platform" src="https://img.shields.io/badge/platforms-Android-green.svg" />
<img alt="Languages" src="https://img.shields.io/badge/languages-Kotlin-F18E33.svg" />
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="http://img.shields.io/badge/license-MIT-green.svg?style=flat" /></a>
</p>

A polyline determinated ProgressBar written in Kotlin. Inspired by this [Reddit post](https://www.reddit.com/r/androidthemes/comments/dymczf/theme_blurred_forest/)

<img src="/screenshots/demo.gif" width=32%/>

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
## Visual Editor
In recent versions, the ability to customize the type of progress using the Visual Editor has been added.
<p><img src="/screenshots/3.png" width=32%/></p>

### How to use Visual Editor
1. Open editor
2. Move the apexes as you like. Add new apexes
3. Click **Load into demo player** to check the result
4. Check the Logcat for the **FractureData** tag and copy the initialization code from the logs

## TODO
- [x] Indeterminate Progress
- [ ] Style initialization from a `.json` file
- [ ] Visual Editor improvements


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
