# 📨 YouTrack → Telegram Notifier

![Banner](# "Optional banner image here")

A Kotlin-based integration that connects **YouTrack** with **Telegram**, sending notifications about new issues and allowing interactive commands to create or view issues directly from your chat.  

This project is part of an internship application task focused on improving collaboration workflows and exploring real-world API integrations.

---

## 🚀 Features

- **Real-time Notifications**
  - Sends YouTrack issue notifications to Telegram with Markdown formatting:
    - **Issue ID**
    - **Title**
    - **Status / State**
    - **Description** (truncated to 500 characters)
    - **Timestamp**

- **Interactive Commands**
  -  `/new "Title" "Description"` → Creates a new issue  
  - `/list` → Lists the latest 5 issues  
  - `/info ISSUE_ID` → Shows detailed info of a specific issue  
  - `/help` → Displays all available commands  

- **Configurable Polling Interval**
  - Set the polling interval for YouTrack notifications (default: 60 seconds)

- **Robust Error Handling**
  - Handles invalid data, network errors, and Telegram API issues gracefully

---

## 📸 Screenshots / Demo



**🛠 Technology Stack**

  - Kotlin — main language
  
  - Ktor — HTTP client for REST APIs
  
  - kotlinx.serialization — JSON parsing
  
  - Coroutines — for polling & messaging
  
  - Telegram Bot API — for messaging

**💡 Notes & Suggestions**

  - Poll interval and other settings can be adjusted via .env

  - Ensure your YouTrack token has the necessary API permissions

  - Telegram bot must have chat permissions with the target user/group

  - Optional: Extend to Microsoft Teams integration as per internship goals

**📄 References**

 - YouTrack REST API Documentation

 - Telegram Bot API Documentation
