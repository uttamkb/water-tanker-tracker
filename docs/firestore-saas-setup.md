# Firestore SaaS Setup

This project now assumes a multi-tenant Firestore layout.

## Collections

### Top-level user profiles

`users/{uid}`

Fields:

- `name`
- `email`
- `role`
- `apartmentId`
- `apartmentName`

### Apartment metadata

`apartments/{apartmentId}`

Fields:

- `apartmentId`
- `name`
- `createdByUserId`

### Vendors

`apartments/{apartmentId}/vendors/{vendorId}`

### Operator invites

`apartments/{apartmentId}/operator_invites/{inviteId}`

### Supply entries

`apartments/{apartmentId}/supply_entries/{entryId}`

## Bootstrap model

For the current app version, the first signed-in user becomes the admin of their own apartment tenant automatically:

- `apartmentId = Firebase UID`
- `role = ADMIN`

That keeps tenant setup isolated without requiring a backend admin panel yet.

## Rules intent

The Firestore rules in [firestore.rules](/Users/uttamkumar_barik/Documents/codex/firestore.rules) enforce:

- users can read their own profile
- apartment admins can read user profiles within their own apartment
- first-time self-bootstrap can create only their own admin profile
- apartment members can read only their own apartment data
- only apartment admins can manage vendors
- only apartment admins can manage operator invites
- apartment members can create supply entries only inside their own apartment
- only apartment admins can update or delete supply entries

## Deploying rules

If you use Firebase CLI later, the project root already includes:

- [firebase.json](/Users/uttamkumar_barik/Documents/codex/firebase.json)
- [firestore.rules](/Users/uttamkumar_barik/Documents/codex/firestore.rules)
- [firestore.indexes.json](/Users/uttamkumar_barik/Documents/codex/firestore.indexes.json)

Typical deploy command:

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## Next SaaS tasks

- replace stored operator invites with a true email acceptance flow
- let invited operators claim invites and create their own user profile safely
- add admin screens for apartment settings
- add billing/subscription logic outside Firestore security rules
