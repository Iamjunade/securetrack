# Testing Checklist - Navigation Debugging

## Issue Description
User reports "Contact" button in navbar is not working.

## Potential Causes
1. **Z-Index/Elevation**: The "Center FAB" or another element might be overlapping the contact button's touch area.
2. **Clickable Attribute**: The LinearLayout `navContacts` might need `android:clickable="true"` and `android:focusable="true"` explicitly set, although `setOnClickListener` usually handles this.
3. **Parent Interception**: The parent LinearLayout or FrameLayout might be intercepting touches.

## Verification Steps
- [ ] Check `activity_main.xml` for overlapping views (FAB margin issues).
- [ ] Ensure `navContacts` has `android:clickable="true"` if needed (though programmatic listener usually suffices, for ripple effect it's good).
- [ ] Check if `android:background="?attr/selectableItemBackground"` is missing for visual feedback (though user said "not working", implying no action).

## Action Plan
1. Add `android:clickable="true"` and `android:focusable="true"` to nav items.
2. Add `android:background="?attr/selectableItemBackground"` for ripple feedback.
3. Check margins of `fabAdd` to ensure it doesn't overlap `navContacts`.
