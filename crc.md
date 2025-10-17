# CRC Cards - Pixels

## Class: Entrant

**Responsibilities:**
- Join waiting list for events
- Leave waiting list for events
- View available events
- Accept or decline event invitations
- Receive notifications
- Manage notification preferences
- View event registration history

**Collaborators:**
- Event
- WaitingList
- Profile
- Notification
- QRCodeScanner

---

## Class: Organizer

**Responsibilities:**
- Create new events
- Update event details
- Set registration periods
- View list of entrants
- Manage waiting list capacity
- Initiate lottery drawing
- Send notifications to entrants
- Cancel entrants
- Export final entrant lists
- Enable/disable geolocation requirements

**Collaborators:**
- Event
- WaitingList
- QRCode
- Notification
- LotterySystem
- Image
- LocationService

---

## Class: Administrator

**Responsibilities:**
- Remove events
- Remove profiles
- Remove images
- Browse all events
- Browse all profiles
- Browse all images
- Remove organizers
- Review notification logs

**Collaborators:**
- Event
- Profile
- Image
- Organizer
- NotificationLog

---

## Class: Event

**Responsibilities:**
- Store event details (name, description, time, place)
- Track registration period (start/end dates)
- Maintain waiting list reference
- Track selected entrants
- Track enrolled entrants
- Track cancelled entrants
- Store event capacity
- Store pricing information
- Link to QR code
- Link to event poster

**Collaborators:**
- Organizer
- WaitingList
- QRCode
- Image
- Entrant

---

## Class: WaitingList

**Responsibilities:**
- Add entrants to waiting list
- Remove entrants from waiting list
- Provide total entrant count
- Track geolocation of entrants (optional)
- Enforce capacity limits (optional)
- Provide entrants for lottery selection

**Collaborators:**
- Entrant
- Event
- LotterySystem
- LocationService

---

## Class: Profile

**Responsibilities:**
- Store user information (name, email, phone)
- Update user information
- Track event registration history
- Track selection status for events
- Delete profile data
- Generate device-based identification

**Collaborators:**
- Entrant
- Event
- DeviceIdentifier

---

## Class: Notification

**Responsibilities:**
- Send lottery win notifications
- Send lottery loss notifications
- Send invitation notifications
- Send general event notifications
- Respect user notification preferences
- Track notification delivery status

**Collaborators:**
- Entrant
- Organizer
- NotificationLog

---

## Class: QRCode

**Responsibilities:**
- Generate unique QR codes for events
- Link QR code to event details
- Provide event information when scanned

**Collaborators:**
- Event
- QRCodeScanner

---

## Class: QRCodeScanner

**Responsibilities:**
- Scan promotional QR codes
- Retrieve event details from QR code
- Navigate to event details page

**Collaborators:**
- QRCode
- Event
- Entrant

---

## Class: LotterySystem

**Responsibilities:**
- Randomly select specified number of entrants
- Draw replacement entrants when needed
- Track selection rounds
- Notify selected entrants
- Notify unselected entrants
- Handle declined invitations

**Collaborators:**
- WaitingList
- Event
- Entrant
- Notification

---

## Class: Image

**Responsibilities:**
- Store event poster images
- Upload images
- Update images
- Delete images
- Validate image format and size

**Collaborators:**
- Event
- Organizer
- Administrator

---

## Class: LocationService

**Responsibilities:**
- Capture device geolocation
- Store entrant join locations
- Provide map visualization of entrant locations
- Verify geolocation requirements

**Collaborators:**
- Entrant
- WaitingList
- Organizer

---

## Class: NotificationLog

**Responsibilities:**
- Record all sent notifications
- Track notification recipients
- Track notification timestamps
- Provide notification history for review

**Collaborators:**
- Notification
- Administrator
- Organizer

---

## Class: DeviceIdentifier

**Responsibilities:**
- Generate unique device-based identification
- Authenticate users without username/password
- Link device to user profile

**Collaborators:**
- Profile
- Entrant

---

## Class: InvitationManager

**Responsibilities:**
- Track invitation status (pending, accepted, declined)
- Handle invitation acceptances
- Handle invitation declines
- Trigger replacement draws on decline
- Manage invitation expiration

**Collaborators:**
- Entrant
- Event
- LotterySystem
- Notification

---

## Class: FirebaseManager

**Responsibilities:**
- Establish Firebase connection
- Manage Firestore database instance
- Handle authentication with Firebase
- Manage real-time listeners
- Handle connection errors and retries

**Collaborators:**
- EventRepository
- ProfileRepository
- ImageRepository
- NotificationRepository

---

## Class: EventRepository

**Responsibilities:**
- Store and retrieve event data from Firestore
- Update event details in database
- Delete events from database
- Query events based on filters
- Sync real-time event updates
- Handle event data validation

**Collaborators:**
- Event
- FirebaseManager
- Organizer
- Administrator

---

## Class: ProfileRepository

**Responsibilities:**
- Store and retrieve profile data from Firestore
- Update profile information in database
- Delete profiles from database
- Query profiles
- Sync profile data across devices
- Handle profile data validation

**Collaborators:**
- Profile
- FirebaseManager
- Entrant
- Administrator

---

## Class: WaitingListRepository

**Responsibilities:**
- Store and retrieve waiting list data from Firestore
- Add/remove entrants from waiting lists
- Update waiting list status in real-time
- Query waiting list information
- Handle concurrent access to waiting lists

**Collaborators:**
- WaitingList
- FirebaseManager
- Event
- Entrant

---

## Class: ImageRepository

**Responsibilities:**
- Upload images to Firebase Storage
- Retrieve image URLs from Firebase Storage
- Delete images from Firebase Storage
- Handle image compression
- Manage image metadata in Firestore

**Collaborators:**
- Image
- FirebaseManager
- Event
- Administrator

---

## Class: NotificationRepository

**Responsibilities:**
- Store notification logs in Firestore
- Retrieve notification history
- Handle Firebase Cloud Messaging integration
- Track notification delivery status
- Query notification records

**Collaborators:**
- Notification
- FirebaseManager
- NotificationLog
- Administrator

