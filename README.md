# Finance Tracker

A comprehensive Android application for personal finance management built with Jetpack Compose and Firebase.

## Overview

Finance Tracker is a modern Android app designed to help users manage their personal finances through intuitive budgeting, goal tracking, and transaction monitoring. The app provides a complete financial management solution with user authentication, data persistence, and real-time synchronisation.

## Core Features

### Authentication
- Firebase email/password authentication
- Google Sign-In integration
- Secure user sessions with automatic logout
- Account creation and management

### Dashboard
- Real-time financial overview
- Current balance calculation from transaction history
- Monthly income and expense summaries
- Budget progress indicators
- Recent transaction display
- Responsive dark/light theme support

### Budget Management
- Create custom budget categories
- Set monthly spending limits
- Real-time expense tracking
- Automatic budget synchronisation with transactions
- Visual progress indicators and spending alerts
- Category-wise expenditure analysis

### Transaction Tracking
- Manual transaction entry with categorisation
- Income and expense classification
- Date-based transaction filtering
- Comprehensive transaction history
- Search and filter capabilities
- Integration with budget and goal systems

### Goal Setting
- Create financial savings goals
- Track progress toward targets
- Multiple goal categories (Emergency Fund, Vacation, Home, etc.)
- Add or remove funds from goals
- Automatic transaction creation for goal activities
- Visual progress tracking and completion status

### Profile Management
- User profile with authentication details
- Dark/light theme toggle
- Data management controls
- User-specific data isolation
- Secure sign-out functionality

## Technical Implementation

### Architecture
- MVVM (Model-View-ViewModel) pattern
- Repository pattern for data abstraction
- Reactive UI with Jetpack Compose
- LiveData for state management

### Technologies
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: Android Architecture Components
- **Database**: Room for local data persistence
- **Authentication**: Firebase Authentication
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL

### Database Schema
- **Users**: Managed through Firebase Authentication
- **Transactions**: Local storage with user isolation
- **Budgets**: Monthly budget categories with spending tracking
- **Goals**: Savings goals with progress monitoring

### Key Components
- **ViewModels**: Handle business logic and state management
- **Repositories**: Abstract data layer interactions
- **DAOs**: Direct database access objects
- **Entities**: Data models for Room database
- **Compose Screens**: Modern UI implementation

## Data Management

### User Data Isolation
All user data is strictly isolated by Firebase user ID, ensuring complete privacy and security between different user accounts.

## Testing

The application includes comprehensive testing coverage:
- Unit tests for ViewModels and business logic
- Repository testing for data layer validation
- UI testing for critical user flows

## Build Configuration

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Jetpack Compose**: Latest stable version
- **Kotlin Compiler Extension**: Latest stable version