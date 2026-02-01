# Fixing Java 25 Compatibility Issue

## ❌ Problem
You're running **Java 25**, but Mockito doesn't support it yet (only supports up to Java 24). This causes 9 test failures.

## ✅ Solution: Switch to Java 21 (Recommended)

### In IntelliJ IDEA:

#### Step 1: Check Your Installed JDKs
1. Go to **File** → **Project Structure** (or press `Ctrl+Alt+Shift+S`)
2. Click **SDKs** (under Platform Settings)
3. Check if you have **Java 21** installed
4. If not, click `+` → **Download JDK** → Select **Java 21** (Temurin or Oracle)

#### Step 2: Set Project SDK
1. In **Project Structure** → **Project**
2. Set **SDK** to **Java 21**
3. Set **Language level** to **21**
4. Click **Apply**

#### Step 3: Set Module SDK
1. In **Project Structure** → **Modules**
2. Select your `newtask` module
3. Set **Module SDK** to **Java 21**
4. Click **OK**

#### Step 4: Update Maven JDK
1. Go to **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven** → **Runner**
2. Set **JRE** to **Java 21**
3. Click **OK**

#### Step 5: Reload Maven & Rebuild
1. Right-click `pom.xml` → **Maven** → **Reload Project**
2. **Build** → **Rebuild Project**

#### Step 6: Run Tests Again
1. Right-click `src/test/java` → **Run 'All Tests'**
2. **All 40 tests should pass!** ✅

---

## Alternative: Add VM Argument (Quick Fix)

If you want to keep Java 25 for now:

### In IntelliJ:
1. Go to **Run** → **Edit Configurations**
2. Select **Templates** → **JUnit**
3. Add to **VM options**:
   ```
   -Dnet.bytebuddy.experimental=true
   ```
4. Click **Apply**
5. Delete existing run configurations (so they use the new template)
6. Run tests again

---

## Why This Happened

- **Your Project**: Configured for Java 21 (in `pom.xml`)
- **Your System**: Running Java 25
- **Mockito/ByteBuddy**: Doesn't support Java 25 yet
- **Result**: 9 unit tests fail (can't mock JwtTokenProvider or Authentication)

---

## What I Did

I added the latest ByteBuddy version (1.15.11) to your `pom.xml`, which might help, but **switching to Java 21 is the best solution**.

---

## After Switching to Java 21

You should see:
- ✅ **40/40 tests passing**
- ✅ **90%+ coverage**
- ✅ All integration tests working

Let me know once you've switched and we can verify everything works!
