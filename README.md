# Lost and Found App

A comprehensive Android application that helps people reconnect with lost items by allowing users to report lost or found items, upload images, filter by category, and remove adverts once items are reunited with their owners.

## App Overview

The Lost and Found App is designed to connect lost items with their owners. Users can easily create adverts for lost or found items, upload photos, and browse through existing posts. The app features category filtering, timestamp tracking, and the ability to remove resolved adverts.

## Features

### Core Features
- **Create Adverts** - Post lost or found items with images
- **Image Upload** - Attach photos to each advert
- **Category Filtering** - Filter items by Electronics, Pets, Wallets, Documents, or Other
- **Timestamp Display** - Shows relative time (e.g., "2 days ago", "Just now")
- **Remove Adverts** - Delete items once resolved with confirmation dialog
- **SQLite Database** - Persistent local storage
- **Real-time Search** - Filter by category instantly

### Technical Features
- Material Design UI components
- RecyclerView with custom adapter
- ViewBinding for type safe layout references
- Relative timestamp formatting
- Image compression and local storage
- Date picker dialog

## Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 33 (Android 13)
- **Database**: SQLite
- **Image Loading**: Glide
- **UI Components**: Material Design Components
- **Architecture**: MVVM pattern

