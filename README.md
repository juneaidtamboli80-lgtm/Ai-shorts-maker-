# Veloce Video AI - Android-First Video Editor & AI Suite

**Veloce Video AI** is a modern, high-performance mobile AI video editing suite built with Kotlin, Jetpack Compose, Android MediaCodec/MediaMuxer, Room Database, and Gemini AI.

---

## 🚀 Core Features

### 1. Video Import & Media Analysis
- Import MP4, MOV, WebM videos directly from device gallery (via zero-permission PhotoPicker) or create from interactive starter presets.
- Real-time video metadata extraction: duration, resolution, framerate, rotation, aspect ratio.
- Non-destructive trimming (Start/End points) and section cropping.

### 2. Video Player & Frame Scrubbing
- Real-time hardware-accelerated playback via Android `MediaPlayer` and `TextureView`.
- Frame-by-frame stepping (`-1 Frame`, `+1 Frame`).
- Jump intervals (`-1s`, `+1s`).
- Draggable timeline needle with sub-millisecond precision.

### 3. AI Auto Captions & Subtitle Engine
- Automatic speech-to-text transcript generation with word-level timestamps.
- Multi-language support (English, Spanish, French, German, Japanese, Portuguese, Hindi, etc.).
- Inline text correction, segment splitting, segment merging, and timing nudging (`±200ms`).
- In-caption text search with instant timestamp jump.

### 4. Word-by-Word Karaoke & Active Word Highlights
- Real-time word highlighting and scaling animation (`Scale Active`, `Karaoke Glow`, `Pop In`, `Bouncy Pop`, `Fade`).
- Fully customizable styles: typography, font size, active word color, text color, background pill opacity, outlines, shadows, and vertical positioning (`positionYRatio`).

### 5. Computer Vision Object & Face Tracking
- User-directed tracking: tap and drag a rectangle over video preview to track any moving target.
- Automatic speaker face detection and trajectory calculation using optical flow and centroid analysis.
- Attach items directly to tracked position:
  - Text labels / Speaker name badges
  - Emojis & stickers (🔥, 🎯, ⭐, etc.)
  - Blur Censor box (privacy blurring for faces/sensitive items)
  - Spotlight highlight & Arrow pointers.
- Trajectory smoothing & sensitivity controls.

### 6. Multi-Track Timeline & Keyframing Engine
- Professional multi-track canvas: Video filmstrip track, Subtitle track, Text track, Sticker track, and Audio waveforms.
- Interactive keyframe diamonds: smooth ease-in-out interpolation for Position X/Y, Scale, Rotation, and Opacity.
- Timeline zoom slider (50% to 400% zoom) with SMPTE ruler markings.

### 7. Effects, Color Grading & Transitions
- 8 Film presets: Original, Cinematic, Vibrant, Sunset Warm, Cool Modern, Vintage 90s, Film Noir, Dramatic.
- Real-time parameter tuning: Brightness, Contrast, Saturation, Exposure, Temperature, Vignette, and Grayscale.

### 8. Royalty-Free Audio & Music Tracks
- Built-in royalty-free music library (Upbeat Synthwave, Cinematic Orchestral, Chill Lo-Fi, Ambient Aura).
- Independent volume sliders for original video audio (0% to 200%) and music tracks.
- Mute toggle.

### 9. AI Shorts & Reels Generator
- Target duration selector: 15s / 30s / 60s.
- Automatic hook retention scoring (e.g., 98% Virality Score).
- One-tap "Apply Short": auto-crops to 9:16 vertical ratio, extracts high-energy speech bursts, and configures animated captions.

### 10. Hardware-Accelerated Export Engine
- High-definition MP4 rendering via Android `MediaCodec` and `MediaMuxer`.
- Output resolutions: 720p HD, 1080p Full HD, 4K Ultra HD.
- Framerate selections: 24 FPS, 30 FPS, 60 FPS.
- Encoding codecs: H.264 (AVC) and H.265 (HEVC).
- Real-time progress bar, status notifications, and export cancellation.

---

## 🏗️ Architecture

```
com.example/
├── MainActivity.kt                  # Edge-to-edge entry point & screen navigation
├── model/
│   ├── ProjectModels.kt            # ProjectEntity, AspectRatio, ExportConfig
│   ├── CaptionModels.kt            # CaptionSegment, WordTiming, SubtitleStyle
│   ├── LayerModels.kt              # TextLayer, StickerLayer, Keyframe
│   ├── TrackingModels.kt           # TrackingObject, BoundingBoxKeyframe
│   ├── EffectsModels.kt            # VideoEffects, FilterPreset
│   ├── AudioModels.kt              # AudioTrack, BuiltInMusicTrack
│   └── AiModels.kt                 # AiAnalysisResult, ShortClipSuggestion
├── data/
│   ├── database/                   # Room database, DAOs, and TypeConverters
│   ├── repository/                 # ProjectRepository for persistence
│   └── sample/                     # Interactive starter video templates
├── engine/
│   ├── VideoPlayerEngine.kt        # Frame-accurate MediaPlayer controller
│   ├── TrackingEngine.kt           # Optical flow & motion tracker
│   ├── KeyframeInterpolator.kt     # Smooth keyframe interpolation
│   ├── VideoExportEngine.kt        # Real MediaCodec/MediaMuxer MP4 exporter
│   ├── AiVideoService.kt           # Speech-to-text & Auto Shorts AI integration
│   └── UndoRedoManager.kt          # Transactional history stack
└── ui/
    ├── theme/                      # Pro Dark Studio palette & typography
    ├── components/                 # VideoPlayerView, TimelineView, TimeDisplay
    ├── panels/                     # EditTrim, Captions, Text, Tracking, Keyframes, Effects, Audio, Shorts, Stickers, Export
    ├── screens/                    # HomeScreen & EditorScreen
    └── viewmodel/                  # HomeViewModel & EditorViewModel
```

---

## 🛠️ Build & Setup Instructions

### Prerequisites
- Android Studio Ladybug / Jellyfish or latest
- Android SDK 36 (minSdk 24)
- JDK 17 / 21

### Secrets & AI Configuration
Configure `GEMINI_API_KEY` in your `.env` or AI Studio Secrets panel:
```
GEMINI_API_KEY=your_gemini_api_key_here
```
When running without an API key, the app automatically switches to built-in smart heuristics so that all captions, tracking, and shorts features remain interactive.

### Compilation
Run:
```bash
gradle :app:assembleDebug
```
