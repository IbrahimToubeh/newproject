# Debug the Failing Admin Test

## The Issue
`getUserById_WithAdminRole_ShouldSucceed` is getting 403 instead of 200.

## Quick Fix Steps:

### 1. Rebuild Project
**Build → Rebuild Project** (very important!)

### 2. Clear IntelliJ Caches
**File → Invalidate Caches → Invalidate and Restart**

### 3. Re-run the Single Failing Test
- Right-click `getUserById_WithAdminRole_ShouldSucceed` method
- Select "Run 'getUserById_WithAdminRole_ShouldSucceed()'"

---

## If Still Failing - Try This:

Add this test method to see what authorities are being loaded:

```java
@Test
void debugAdminAuthorities() throws Exception {
    System.out.println("Admin User: " + adminUser.getUsername());
    System.out.println("Admin Role: " + adminUser.getRole());
    
    UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
    System.out.println("Loaded authorities: " + userDetails.getAuthorities());
    
    assertTrue(userDetails.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
}
```

Add this to `UserControllerIntegrationTest.java` and run it.

This will show us if:
- The admin user is in the database ✅
- The UserDetailsService is loading it ✅
- The authorities include "ROLE_ADMIN" ✅

---

## Expected Output:
```
Admin User: admin
Admin Role: ADMIN
Loaded authorities: [ROLE_ADMIN]
```

If you don't see `ROLE_ADMIN`, then we know where the problem is!
