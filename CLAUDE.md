# Claude Context File

This file contains information to help Claude understand your Android Studio project better.

## Project Information
- **Project Type**: Android Studio Project
- **Location**: C:\Users\braed\AndroidStudioProjects\CP3406-Finance-Tracker
- **Platform**: Android

## Build Commands
- Build: `./gradlew build`
- Clean: `./gradlew clean`
- Test: `./gradlew test`
- Lint: `./gradlew lint`

## Common Directories
- `app/src/main/java/` - Main Java/Kotlin source code
- `app/src/main/res/` - Android resources (layouts, strings, etc.)
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumentation tests
- `gradle/` - Gradle wrapper and configuration

## Notes
- This file is local only and should not be committed to version control
- Value Proposition
  - This proposed app combines the strengths of popular financial
    apps while addressing their notable limitations. Unlike Mint, it offers full budget
    category customisation and enhanced data privacy, eliminating intrusive
    advertisements and unnecessary data sharing. It integrates YNAB’s proactive budgeting
    philosophy without the steep learning curve and mandatory subscription fees,
    providing a simpler yet equally effective user experience. Furthermore, compared to
    PocketGuard, it includes comprehensive budgeting tools, detailed financial analytics,
    and extensive customisation options, all accessible without premium upgrade
    constraints. The app aims to provide personalised financial coaching, seamless
    budgeting integration, and predictive insights, ensuring that users easily manage their
    finances to achieve their long-term goals.
- User Features:
  • Secure authentication (Firebase: Email, Google, Facebook)
  • Profile personalisation with custom budget preferences
  • Goal setting and milestone tracking (short/long-term targets)
- Core Features:
  • Real-time bank integration (Plaid API)
  • User-defined, zero-based budgeting model
  • Automated, AI-enhanced transaction categorisation
  • Predictive bill reminders with direct payment integration
  • Goal-oriented savings tracking with predictive analytics
  • Personalised insights and actionable financial recommendations
  • Historical-data-driven expense forecasting
- UI Elements:
  • Interactive financial dashboard (charts, disposable income visualisation)
  • Streamlined tab-based navigation
  • Dark/light theme support
- Goals for Development:
  - Use of Android Studio & Java/Kotlin
  - Firebase Authentication
  - Plaid API
  - Room Database
  - MVVM Architecture
  - Push Notifications
  - Chart Libraries