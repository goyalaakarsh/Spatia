# Spatia - Android E-Commerce Application

## Overview
Spatia is a modern e-commerce Android application designed to provide users with a seamless shopping experience. The app features a clean and intuitive user interface, secure authentication, product browsing, cart management, and user profile functionality.

## Features

### User Authentication
- User registration with email and password
- Secure login process
- Persistent login state management
- Password recovery option

### Onboarding Experience
- Welcoming splash screen
- Informative onboarding sequence for first-time users
- Automatic skip to home for returning users

### Product Management
- Browse products by category
- View detailed product information
- Search functionality
- Product filtering and sorting

### Shopping Experience
- Add products to cart
- Adjust product quantities
- Remove items from cart
- Save items for later

### User Profile
- View and edit personal information
- View order history
- Manage payment methods
- Address management

### Additional Features
- Order tracking
- Wishlist functionality
- Settings customization
- Secure checkout process

## Technical Implementation

### Architecture
- Activity-based navigation
- Firebase Authentication for user management
- Firebase Firestore for database storage
- Google Material Design components

### Dependencies
- Firebase Authentication
- Firebase Firestore
- Material Components
- RecyclerView for list displays
- Glide for image loading

## Getting Started

### Setup Instructions
1. Clone the repository
2. Open the project in Android Studio
3. Connect your Firebase project by adding the `google-services.json` file
4. Build and run the application

## Project Structure
- `/app/src/main/java/com/example/spatia/` - Contains all Java code
  - `/activities/` - All activity classes
  - `/adapters/` - RecyclerView adapters
  - `/model/` - Data models
  - `/utils/` - Utility classes
- `/app/src/main/res/` - Contains all resources
  - `/layout/` - XML layout files
  - `/drawable/` - Images and drawable resources
  - `/values/` - Strings, colors, and styles

