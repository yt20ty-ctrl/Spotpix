# Instalk — Liquid Glass Social Media Upgrade & Pro Creator Suite

This update rebrands the application to **Instalk**, introduces a custom **Feed + Reel + Camera** Liquid Glass launcher icon, adds universal double-tap gestures (including double-tap to like across Feed, Reels, and Stories, plus double-tap to switch accounts), introduces a floating Liquid Glass `+` creation pop-up menu with a full-featured Media Editing & Effects Studio, upgrades transitions to smooth sliding animations, and expands Profile, Direct Messages, Stories, and Settings with professional creator capabilities.

## User Review & Critical Decisions

> [!IMPORTANT]
> All choices confirmed during the clarification phase are incorporated below:

- **Brand & Icon Identity**: App renamed to **Instalk** everywhere (`strings.xml`, `metadata.json`, `settings.gradle.kts`, and UI headers). New adaptive launcher icon combining a **Feed card, Reel clapper/play badge, and Camera lens** in a 3D Liquid Glass aesthetic.
- **Floating `+` Creation Menu**: Tapping the center `+` button in the navigation bar opens a **Floating Liquid Glass pop-up menu** hovering above the bar with four distinct creation modes: **Story**, **Feed Post**, **Reel**, and **Video**.
- **Full Media Editor & Upload Studio**: Includes live color filters/shader presets (ColorMatrix adjustments), aspect ratio crop selector (`1:1`, `4:5`, `9:16`, `16:9`), brightness/contrast/warmth sliders, draggable/customizable text overlays & stickers, audio track mixer, and upload quality controls (HD 4K/1080p, Data Saver, Hide Likes, Allow Comments).
- **Universal Double-Tap Gestures**:
  - **Double-Tap to Like**: Works in **Home Feed**, **Vertical Reels**, and **Full-Screen Stories** with a spring-animated heart burst and haptic feedback.
  - **Double-Tap to Switch Profile Account**: Double-tapping the Profile navigation tab or the Profile header avatar immediately cycles to the next creator account with a sleek Liquid Glass notification banner.
- **Pro Profile, Messages & Settings**:
  - **Profile**: Professional Creator Dashboard & Analytics Insights modal (Reach, Impressions, Engagement, Follower Breakdown).
  - **Messages**: Primary vs. **Message Requests** tabs (with Accept/Decline actions), **Chat Themes** (Sapphire, Cyber Violet, Sunset, Emerald), and **Voice Note** messages.
  - **Stories**: Interactive **Story Polls** (tap to vote and see live percentages) and quick emoji reactions.
  - **Settings**: Multi-category Professional Settings covering **Account Center & Security**, **Upload & Media Quality**, **Liquid Glass Theme & Blur**, and **Privacy & Blocked Users**.

---

## 1. Overview & Core Concept

- **What It Does**: Elevates **Instalk** into a creator-grade social networking platform with tactile gestures, rich media editing before publishing, smooth sliding view transitions, and comprehensive account/privacy management.
- **Target Audience / Persona**: Creators and power users who want instant account switching, granular media effects and upload settings, professional analytics, and a cohesive Liquid Glass interface.
- **Key Value**: Every interaction feels fluid, responsive, and complete—from double-tapping any media to heart it, to applying live filters and text overlays before posting, to switching accounts with a double-tap.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Double-Tap Gestures Flow**:
   - **Feed, Reels & Stories**: Double-tapping any post photo, vertical reel, or full-screen story triggers a vibrant 3D heart burst overlay animation and increments the like counter in Room.
   - **Instant Account Switch**: Double-tapping the Profile icon on the bottom navigation bar (or the user avatar on the Profile screen) cycles to the next user persona in the database and shows a floating glass toast (`Switched to @AvaGlow`).
2. **Floating `+` Creation Pop-Up & Media Studio Flow**:
   - Tapping `+` animates a frosted glass radial/card pop-up above the bottom bar offering **Add Story**, **New Post**, **New Reel**, and **New Video**.
   - Selecting any option opens the **Create & Edit Studio** tailored to that format:
     - **Filter & Effect Bar**: Live preview using Compose `ColorFilter.colorMatrix` for presets (*Original, Liquid Cyan, Neon Nights, Golden Hour, Vintage Warm, Cyber Noir, High Contrast*).
     - **Adjustments & Crop**: Sliders for Brightness, Contrast, and Saturation, plus aspect ratio pills (`1:1`, `4:5`, `9:16`).
     - **Overlays & Audio**: Add styled text banners, interactive poll/emoji stickers, and select background music tracks.
     - **Upload Settings Accordion**: Toggle **Upload in Highest Quality (HD/4K)**, **Data Saver Compression**, **Allow Remixing**, and **Turn Off Commenting**.
3. **Smooth Slide Navigation**:
   - Switching between Home, Explore, Create, Reels, and Profile uses `AnimatedContent` with directional horizontal slide + fade transitions (`slideInHorizontally` + `fadeIn` with spring physics) for a 60–120 FPS native feel.
4. **Professional Profile & Creator Insights**:
   - Upgraded Profile header with cover glow, mutual follower badges, and a **Professional Dashboard** banner showing 30-day reach (`+148%`).
   - Tapping the dashboard opens a **Creator Analytics Modal** with engagement metrics, top-performing posts, and audience demographics.
5. **Pro Direct Messaging**:
   - Segmented glass tabs: **Primary**, **General**, and **Requests (2)**.
   - Inside a chat room: **Theme Picker** button to customize bubble/backdrop gradients, **Voice Note** button to send simulated waveform audio clips, and photo sharing.
6. **Comprehensive Settings Center**:
   - Structured into clear glass cards:
     - **Account & Security**: Password & 2FA toggle, Login Activity, Switch/Add Account.
     - **Media & Uploading**: Default Upload Quality (Standard / HD 1080p / 4K), Data Saver Mode, Auto-Play Videos on Cellular.
     - **Liquid Glass Appearance**: System Auto / Dark Glass / Light Glass, Specular Glow toggle.
     - **Privacy & Safety**: Private Account, Activity Status, Story Replies permission, Blocked & Restricted Accounts.

---

## 3. Key Product Decisions & Trade-Offs

- **Real-Time Filter Rendering via `ColorMatrix`**:
  - *Chosen Approach*: Apply real hardware-accelerated `ColorFilter.colorMatrix` transformations directly on the Compose `AsyncImage` preview and persist the chosen filter preset name with the created Post/Reel/Story so it renders across the entire app.
  - *Why*: Gives users genuine visual editing feedback with zero lag and persists the visual effect in the feed.
- **Non-Conflicting Single-Tap vs. Double-Tap Handling**:
  - *Chosen Approach*: Use `detectTapGestures(onTap = { ... }, onDoubleTap = { ... })` in Reels, Stories, and Profile Navigation so single-tap (play/pause, advance story, select tab) and double-tap (like burst, quick account switch) coexist cleanly.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                               MainActivity                                 │
│           (InstalkTheme + Smooth Slide AnimatedContent Host)               │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼──────────────────────────────────────┐
│                       InstalkApp (Root Scaffold)                           │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ Floating + Creation Pop-Up (Story | Feed Post | Reel | Video)        │  │
│  ├──────────────────────────────────────────────────────────────────────┤  │
│  │ LiquidGlassNavBar (Single-tap: Navigate | Double-tap Profile: Switch)│  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└──────┬───────────────┬──────────────────┬─────────────────┬────────────────┘
       │               │                  │                 │
┌──────▼──────┐ ┌──────▼──────┐    ┌──────▼───────┐  ┌──────▼────────┐
│ HomeScreen  │ │ ReelsScreen │    │CreateStudio  │  │ ProfileScreen │
│ + StoryModal│ │ • DoubleTap │    │ • ColorMatrix│  │ • DoubleTap   │
│ • DoubleTap │ │   Heart     │    │   Filters    │  │   Switch      │
│   Like      │ │ • Audio Disc│    │ • Crop/Adjust│  │ • Pro Insights│
│ • Story Poll│ │             │    │ • Text/Stick │  │   Dashboard   │
│   & React   │ │             │    │ • Upload Pref│  │ • Highlights  │
└──────┬──────┘ └──────┬──────┘    └──────┬───────┘  └──────┬────────┘
       │               │                  │                 │
       └───────────────┴──────────────────┼─────────────────┘
                                          │
┌─────────────────────────────────────────▼──────────────────────────────────┐
│                           SocialViewModel                                  │
│  (Manages UploadSettings, FilterPresets, StoryLikes, Polls, ChatThemes)    │
└─────────────────────────────────────────┬──────────────────────────────────┘
                                          │
┌─────────────────────────────────────────▼──────────────────────────────────┐
│                     Room Database (InstaLinkDatabase)                      │
│  [UserEntity] [PostEntity] [ReelEntity] [StoryEntity] [DirectMessageEntity]│
└────────────────────────────────────────────────────────────────────────────┘
```

### Data Model Enhancements

1. **`PostEntity` & `ReelEntity` & `StoryEntity`**:
   - Add `filterName: String = "Original"` and `overlayText: String = ""` so posts, stories, and reels created in the Studio render with their chosen visual filter and overlay text everywhere in the app.
   - Add `isLikedByMe: Boolean` and `likeCount: Int` to `StoryEntity` to support double-tap story liking, plus `pollQuestion: String?`, `pollOptionA: String?`, `pollOptionB: String?`, `pollVotesA: Int`, `pollVotesB: Int`, `userVotedOption: Int?` for interactive story polls.
2. **`UploadSettings` State**:
   - `uploadQuality: String` ("HD 1080p", "4K Ultra", "Standard"), `dataSaver: Boolean`, `autoPlayVideo: Boolean`, `hideLikeCounts: Boolean`, `allowComments: Boolean`, `allowRemixing: Boolean`.
3. **`DirectMessageEntity` & Conversation State**:
   - Add `isRequest: Boolean = false` and `isVoiceNote: Boolean = false`, `voiceDurationSec: Int = 0` to support Message Requests and Voice Notes, plus per-chat theme selection.
