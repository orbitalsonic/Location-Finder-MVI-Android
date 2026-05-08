# 📍 Location Finder MVI Android

A modern, production-ready Android Location Finder built with **MVI Architecture**, **StateFlow**, **DataStore**, and **Fused Location Provider**.

This project demonstrates how to build a scalable and clean Android location module with:

* Latest location fetching
* GPS handling
* Permission flow
* Address conversion
* DataStore caching
* MVI architecture
* Repository pattern
* Kotlin Coroutines
* Single state observer
* Clean production structure

---

# ✨ Features

✅ MVI Architecture

✅ StateFlow + Channel Effects

✅ DataStore Local Cache

✅ Latest Location Fetching

✅ GPS Enable Handling

✅ Permission + Rational Dialogs

✅ Permanent Denied Settings Redirect

✅ Geocoder Address Conversion

✅ Full Address / Short Address / City

✅ Distance Change Check (300 meters)

✅ Cached Fallback Support

✅ Production Ready Structure

✅ XML + ViewBinding

✅ Modern Kotlin Practices

---

# 🏗️ Architecture

```text
UI Layer
 └── MainActivity
      └── Collects State + Effect

Presentation Layer
 └── LocationViewModel
      └── Handles Intents

Domain Layer
 ├── Intent
 ├── State
 ├── Effect
 └── Models

Data Layer
 ├── Repository
 ├── RemoteDataSource
 └── DataStore Cache
```

---

# 📂 Project Structure

```text
location/
├── data/
│   ├── datastore/
│   ├── remote/
│   └── repository/
│
├── domain/
│   ├── model/
│   ├── intent/
│   ├── state/
│   └── effect/
│
├── presentation/
│
└── utils/
```

---

# 📸 Supported Features

## 📍 Location

* Latitude
* Longitude
* Latest Location
* Current Location

## 🏠 Address

* Full Address
* Short Address
* City Name
* Country Name

## 💾 Cache

* Instant cached location
* Background fresh update
* Offline fallback
* Distance-based saving

---

# 🔐 Permission Flow

```text
Request Permission
    ↓
Granted → Fetch Location
Denied → Show Rational Dialog
Permanent Denied → Open App Settings
```

---

# 📡 GPS Flow

```text
GPS Disabled
    ↓
Show GPS Dialog
    ↓
Open Location Settings
    ↓
Enable GPS
    ↓
Fetch Location Again
```

---

# 🔄 Location Flow

```text
App Open
   ↓
Load Cached Location
   ↓
Show Cached UI Instantly
   ↓
Fetch Fresh Location
   ↓
Compare Distance (300m)
   ↓
Save If Changed
   ↓
Update UI
```

---


## Contributing
Contributions are welcome! Fork the repository, make changes, and submit a pull request.

---

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Copyright OrbitalSonic

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

