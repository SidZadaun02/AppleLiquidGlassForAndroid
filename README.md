# Apple Liquid Glass for Android 🍎💧

A high-performance, **Apple-style Liquid Glass** (VisionOS/iOS blur) library for Android. 
This library creates a stunning, interactive glass effect with **real-time background blur**, **magnification (zoom)**, and **vibrancy boost**, mimicking the premium materials found in modern Apple interfaces.

![Liquid Glass Demo](https://via.placeholder.com/800x400?text=Liquid+Glass+Preview) 

<!-- 
To add a video demo:
1. Record your screen and save it as 'demo.mp4' or 'demo.gif'.
2. Place the file in the 'assets' folder of this repository.
3. Uncomment the line below (GitHub supports drag-and-drop video uploads in issues too, which gives you a link).
-->


https://github.com/user-attachments/assets/d6effb78-f478-4ebd-aa71-a4faaf371c09


<video src="assets/demo.mp4" width="800" controls></video> 
<!-- 
OR for GIF:
![Demo](assets/demo.gif)
-->

## ✨ Features

*   **Frosted Glass Effect**: High-quality downsampled blur that looks natural and performs well.
*   **Magnification (Refraction)**: Simulates the optical properties of thick glass by magnifying the content behind it.
*   **Vibrancy Boost**: Automatically increases saturation of the background content to make it "pop" through the glass.
*   **Interactive**: Optimized for dragging and moving over dynamic backgrounds.
*   **Dual Support**: Works with both **XML (View System)** and **Jetpack Compose**.
*   **Lightweight**: No heavy external dependencies required for the core effect.

## 📦 Installation

Add the JitPack repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.SidZadaun02:AppleLiquidGlassForAndroid:1.0.0")
}
```

## 🚀 Usage

### 1. XML / View System

Simply add `LiquidGlassView` to your layout. It acts as a `FrameLayout`, so you can add children (text, icons) inside it.

```xml
<com.sidzadaun.liquidglass.LiquidGlassView
    android:id="@+id/glassView"
    android:layout_width="300dp"
    android:layout_height="200dp"
    app:lg_blurRadius="15dp"
    app:lg_cornerRadius="24dp"
    app:lg_overlayColor="#15FFFFFF" />
```

**Important**: In your Activity/Fragment, you must initialize the view with the root content you want to blur:

```kotlin
val glassView = findViewById<LiquidGlassView>(R.id.glassView)

// Option 1: Automatically find the Activity root
glassView.setupWithActivityRoot()

// Option 2: Specify a specific view to blur (e.g., a background image container)
glassView.setupWithRoot(binding.rootContainer)
```

### 2. Jetpack Compose

Use the `AndroidView` interoperability to use the glass effect in Compose (native Compose blur is limited on older Android versions).

```kotlin
AndroidView(
    factory = { context ->
        LiquidGlassView(context).apply {
            setupWithActivityRoot()
            setRadius(15f)
            setZoom(1.2f) // 1.2x magnification
            setGlassColor(android.graphics.Color.parseColor("#15FFFFFF"))
            
            // Add child views programmatically if needed, or overlay Composables on top
        }
    },
    modifier = Modifier.size(300.dp, 200.dp)
)
```

## ⚙️ Customization

| Attribute | Method | Description | Default |
| :--- | :--- | :--- | :--- |
| `app:lg_blurRadius` | `setRadius(float)` | Controls the intensity of the blur. | `15f` |
| `app:lg_cornerRadius` | `setCornerRadius(float)` | Roundness of the glass corners. | `0dp` |
| `app:lg_overlayColor` | `setGlassColor(int)` | The tint color of the glass surface. | `#25FFFFFF` |
| N/A | `setZoom(float)` | Magnification level (1.0 = no zoom). | `1.1f` |

## 🔧 How it Works

Unlike standard blur libraries that use heavy RenderScript or require Android 12+ APIs for `RenderEffect`, this library uses a smart **downsampling technique**:
1.  It captures a snapshot of the view behind it.
2.  It downscales the snapshot (e.g., to 10% size), which naturally "blurs" the details.
3.  It scales it back up with bilinear filtering to create a smooth, frosted look.
4.  It applies a color matrix filter to boost saturation, mimicking the "Vibrancy" material in iOS.

## 📄 License

```
MIT License

Copyright (c) 2025 Siddhartha Singh Jadaun

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
