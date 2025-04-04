<div align="center">
  <img src="https://media-hosting.imagekit.io/c28554254034453e/paymentX_logo.png?Expires=1838319405&Key-Pair-Id=K2ZIVPTIP2VGHC&Signature=znkKp0YY2aefQAJ39EuLpVe5wVhm9SoriSi9B37neT20iUqBKerqL7x13cmH4YVvFnlboZH24ntEkYaXeybZT9TAXZVk7gVUVpIj1eSFxkhgN2EcZAuUfcLy6~iEgytSqrJ~F5b57f8j48eoEL6NIuJ4ywCuBQM4bcgSyK~3U1OLjlOSqJEHHnFX6lIM9~OxaQL8OBjNZhoIvmIlvfSDwa~z0WxzX1NLONr0kbIR4zH9uQ2ldUcahPWYugIDMBjrFgJ7DBGf6e9s94JXsPXVzdRG61qyaIiSjhcvYuVXSM29XdHHyUcQaNzmr-Whcoe3c6U~XfHAj9wR8BrO2HkW2A__" alt="PaymentX" width="60%" />
  
  # PaymentX: NFC-Based Offline Payment System
  
  <p align="center">
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
    <img src="https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white" alt="Node.js"/>
    <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB"/>
    <img src="https://img.shields.io/badge/NFC-0052CC?style=for-the-badge&logo=nfc&logoColor=white" alt="NFC"/>
  </p>
  
  <p align="center">
    <b>Campus payments made simple, even without internet!</b>
  </p>
</div>

<!-- Quick Overview Section -->
<p align="center">
  <img src="https://via.placeholder.com/800x400/F59E0B/FFFFFF?text=PaymentX+App+Screenshots" alt="App Screenshots" width="80%"/>
</p>

## 🌟 Overview

**PaymentX** solves a critical problem in campus environments - making payments in areas with limited internet connectivity. Our solution leverages NFC technology to enable seamless transactions between students and campus merchants without requiring internet on the user's device.

<div align="center">
  <table>
    <tr>
      <td width="50%">
        <h3 align="center">💸 For Students</h3>
        <ul>
          <li>Make payments <b>without internet</b></li>
          <li>Link your college ID card via NFC</li>
          <li>Secure transactions with custom PIN</li>
          <li>Easy wallet top-up</li>
          <li>Track transactions (coming soon)</li>
        </ul>
      </td>
      <td width="50%">
        <h3 align="center">🏪 For Merchants</h3>
        <ul>
          <li>Simple storefront setup</li>
          <li>Receive payments instantly</li>
          <li>Comprehensive transaction history</li>
          <li>Withdraw funds easily</li>
          <li>Business analytics dashboard</li>
        </ul>
      </td>
    </tr>
  </table>
</div>

## ✨ Key Features

### 🔄 Payment Flow

1. **Registration** - Sign up with your college email and register your ID card
2. **Setup** - Create transaction PIN and verify phone number
3. **Top-up** - Add funds to your PaymentX wallet
4. **Tap & Pay** - Tap your ID card on merchant's device
5. **Verify** - Authorize with your secure PIN
6. **Complete** - Transaction processed instantly!

> **⚠️ Note:** While users can pay offline, merchants require internet connectivity to process transactions.

### 🛡️ Security Features

<div align="center">
  <table>
    <tr>
      <td align="center">
        <h3>🔐</h3>
        <b>Secure Authentication</b><br>
        <small>Multi-factor authentication for all users</small>
      </td>
      <td align="center">
        <h3>🔒</h3>
        <b>Encrypted Transactions</b><br>
        <small>All payment data is fully encrypted</small>
      </td>
      <td align="center">
        <h3>🔢</h3>
        <b>PIN Protection</b><br>
        <small>Custom PIN for authorizing transactions</small>
      </td>
      <td align="center">
        <h3>📶</h3>
        <b>Secure NFC</b><br>
        <small>Tamper-resistant communication</small>
      </td>
    </tr>
  </table>
</div>

## 🛠️ Technology Stack

<div align="center">
  <table>
    <tr>
      <td align="center"><img src="https://cdn.simpleicons.org/kotlin" width="60px"/><br/>Kotlin</td>
      <td align="center"><img src="https://cdn.simpleicons.org/nodedotjs" width="60px"/><br/>Node.js</td>
      <td align="center"><img src="https://cdn.simpleicons.org/mongodb" width="60px"/><br/>MongoDB</td>
      <td align="center"><img src="https://cdn.simpleicons.org/jsonwebtokens/white" style="filter: invert(1);" width="60px"/><br/>JWT</td>
      <td align="center"><img src="https://api.iconify.design/mdi/nfc.svg?color=white" width="60px"/><br/>NFC</td>
    </tr>
  </table>
</div>


## 📱 Application Architecture

### System Architecture
```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│   User Mobile   │◄────►│  PaymentX API   │◄────►│ Merchant Mobile │
│   Application   │      │    (Backend)    │      │   Application   │
│                 │      │                 │      │                 │
└─────────────────┘      └────────┬────────┘      └─────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │                 │
                         │    MongoDB      │
                         │   Database      │
                         │                 │
                         └─────────────────┘
```

### Mobile Architecture
```
PaymentX/
├── User App/
│   ├── Authentication
│   │   ├── Registration
│   │   ├── Login
│   │   └── PIN Management
│   ├── Wallet Management
│   │   ├── Top-up
│   │   ├── Balance Check
│   │   └── Withdrawal
│   ├── NFC Integration
│   │   ├── Card Registration
│   │   └── Payment Processing
│   └── Transaction History
│
└── Merchant App/
    ├── Business Registration
    ├── Payment Terminal
    ├── Transaction Dashboard
    └── Withdrawal Management
```

## ⚙️ Implementation Status

<table>
  <tr>
    <th>Component</th>
    <th>Status</th>
    <th>Details</th>
  </tr>
  <tr>
    <td>Merchant App</td>
    <td>✅ Complete</td>
    <td>Fully implemented with payment receiving functionality</td>
  </tr>
  <tr>
    <td>Backend API</td>
    <td>✅ Complete</td>
    <td>All endpoints implemented and documented</td>
  </tr>
  <tr>
    <td>User Authentication</td>
    <td>✅ Complete</td>
    <td>Email & PIN verification implemented</td>
  </tr>
  <tr>
    <td>NFC Integration</td>
    <td>✅ Complete</td>
    <td>Card registration and payment processing</td>
  </tr>
  <tr>
    <td>Merchant Transaction History</td>
    <td>✅ Complete</td>
    <td>Full history tracking for merchants</td>
  </tr>
  <tr>
    <td>User App Core Features</td>
    <td>🔄 In Progress</td>
    <td>Basic functionality implemented, UI refinements ongoing</td>
  </tr>
  <tr>
    <td>User Transaction History</td>
    <td>⏳ Pending</td>
    <td>Implementation planned for next sprint</td>
  </tr>
</table>

## 🚀 Getting Started

### Prerequisites
- Android Studio
- Node.js
- MongoDB
- Android device with NFC capability

### Installation & Setup

<details>
<summary><b>User App Setup</b></summary>

1. Clone this repository
   ```bash
   git clone https://github.com/ISTE-VIT/paymentx-app.git
   ```
2. Open the project in Android Studio
3. Connect an NFC-capable Android device
4. Run the application
5. Register with your college email
6. Add your ID card via NFC
</details>

<details>
<summary><b>Merchant App Setup</b></summary>

1. Follow the same steps as User App
2. Select "Register as Merchant" during setup
3. Enter your business details
4. Ensure your device has active internet connection
</details>

<details>
<summary><b>Backend Setup</b></summary>

1. Clone the backend repository
   ```bash
   git clone https://github.com/ISTE-VIT/paymentx-backend.git
   ```
2. Install dependencies
   ```bash
   cd paymentx-backend
   npm install
   ```
3. Set up environment variables
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```
4. Start the server
   ```bash
   npm start
   ```
</details>

## 🔗 Related Repositories

- [PaymentX Backend](https://github.com/ISTE-VIT/paymentx-backend.git)

## 👥 Meet Our Team

<div align="center">
  <table>
    <tr>
      <td align="center">
        <h3>Rudra Gupta</h3>
        <p><i>Android Developer</i></p>
        <a href="https://github.com/Rudragupta8777"><img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" height="22" target="_blank"></a>
        <a href="https://www.linkedin.com/in/rudra-gupta-36827828b/"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=flat-square&logo=linkedin&logoColor=white" height="22" target="_blank"></a>
      </td>
      <td align="center">
        <h3>Pratham Khanduja</h3>
<p><i>Android & Backend Developer</i></p>
<a href="https://github.com/pratham-developer"><img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" height="22" target="_blank"></a>
<a href="https://www.linkedin.com/in/pratham-khanduja/"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=flat-square&logo=linkedin&logoColor=white" height="22" target="_blank"></a>
      </td>
      <td align="center">
        <h3>Saniya Goyal</h3>
        <p><i>UI/UX Designer</i></p>
        <a href="https://github.com/san7iya"><img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" height="22" target="_blank"></a>
        <a href="https://www.linkedin.com/in/saniya7goyal/"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=flat-square&logo=linkedin&logoColor=white" height="22" target="_blank"></a>
      </td>
    </tr>
  </table>
</div>



<div align="center">
  <h2>
    <b>PaymentX</b>
  </h2>
  <h3>
    <i>Transforming Campus Payments</i> | Made with ❤️ at <a href="https://github.com/ISTE-VIT" target="_blank">ISTE-VIT</a>
  </h3>
   
  ---
</div>
