# MC/DC Test Suite - UniWorld Music Streaming Application

## Overview

Modified Condition/Decision Coverage (MC/DC) is a rigorous code coverage metric that requires each condition in a decision statement to independently affect the decision's outcome. This ensures comprehensive testing of complex boolean logic.

## Test Files Generated

### Backend (Java/Spring Boot)

#### 1. **SearchControllerMCDCTest.java**
- **Location**: `src/test/java/com/uniworld/uniworld_backend/SearchControllerMCDCTest.java`
- **Complexity**: HIGH (5-part OR condition)
- **Decision Logic**: `matchesSong()` method with 5 independent conditions
- **Test Cases**: 17 test cases covering:
  - Each condition independently matching
  - No conditions matching
  - Multiple conditions matching simultaneously
  - Null/blank field handling
  - Case-insensitive matching
  - Special characters and unicode
  - Partial word matching
- **MC/DC Coverage**: Each of 5 conditions independently affects outcome

#### 2. **AuthServiceLoginMCDCTest.java**
- **Location**: `src/test/java/com/uniworld/uniworld_backend/AuthServiceLoginMCDCTest.java`
- **Complexity**: HIGH (4-part OR condition)
- **Decision Logic**: `login()` validation with 4 independent conditions
- **Test Cases**: 13 test cases covering:
  - Email/password null checks
  - Email/password blank checks
  - Edge cases (single space, tabs, very long strings)
  - Multiple invalid conditions simultaneously
  - Email format variations
  - Negation logic verification
- **MC/DC Coverage**: Each of 4 validation conditions independently affects outcome
- **Decision Table**: Comprehensive matrix showing all condition combinations

#### 3. **JwtServiceValidationMCDCTest.java**
- **Location**: `src/test/java/com/uniworld/uniworld_backend/JwtServiceValidationMCDCTest.java`
- **Complexity**: MEDIUM (2-part AND with negation)
- **Decision Logic**: `isTokenValid()` with username match and token expiry
- **Test Cases**: 14 test cases covering:
  - Username matches + token valid
  - Username matches + token expired
  - Username mismatch + token valid
  - Username mismatch + token expired
  - Negation operator behavior
  - Short-circuit evaluation in AND logic
  - Null/empty username handling
  - Case sensitivity verification
- **MC/DC Coverage**: Each condition independently affects result

#### 4. **PlaylistAuthorizationMCDCTest.java**
- **Location**: `src/test/java/com/uniworld/uniworld_backend/PlaylistAuthorizationMCDCTest.java`
- **Complexity**: MEDIUM (negated equals check)
- **Decision Logic**: `!existing.getUser().getUserID().equals(user.getUserID())`
- **Test Cases**: 13 test cases covering:
  - User owns playlist (IDs match)
  - User doesn't own playlist (IDs differ)
  - Negative/zero/max IDs
  - Off-by-one errors
  - Null reference handling
  - Negation logic verification
  - Repository integration scenarios
- **MC/DC Coverage**: Each operand value independently affects authorization

#### 5. **MusicLibraryInitializerMCDCTest.java**
- **Location**: `src/test/java/com/uniworld/uniworld_backend/MusicLibraryInitializerMCDCTest.java`
- **Complexity**: HIGHEST (multiple cascading conditions)
- **Decision Logic**: Three critical areas:
  1. ID3 tag extraction (3-part AND with negation)
  2. Album cover fallback (mixed AND/OR logic)
  3. Null/blank validation (2-part OR)
- **Test Cases**: 18 test cases covering:
  - Cascading null checks
  - Tag/title extraction with all combinations
  - Album cover presence/absence scenarios
  - MIME type detection with fallback
  - File extension validation
  - Complex metadata reading patterns
- **MC/DC Coverage**: Multiple independent decision paths verified

### Frontend (Angular/TypeScript)

#### 6. **player.mcdc.spec.ts**
- **Location**: `src/app/components/player/player.mcdc.spec.ts`
- **Complexity**: HIGHEST (3-part AND in onAudioEnded)
- **Decision Logic**: End-of-queue detection with shuffle/repeat/last-song
- **Test Cases**: 20 test cases covering:
  - All condition combinations (TTT, FTT, TFT, TTF)
  - Empty queue handling
  - Single song queue
  - Current index boundaries
  - Repeat mode variations ('one', 'all', 'off')
  - Shuffle enabled/disabled scenarios
  - Negation operator verification
  - String comparison (case-sensitive, whitespace)
  - Guard conditions (null audio, empty queue)
- **MC/DC Coverage**: Each of 3 conditions independently affects playback end

#### 7. **app.mcdc.spec.ts**
- **Location**: `src/app/app.mcdc.spec.ts`
- **Complexity**: MEDIUM-HIGH (mixed guard conditions and route visibility)
- **Decision Logic**: Three critical areas:
  1. Playlist guard conditions (2-part)
  2. Playlist ID validation (2-part AND)
  3. Route visibility with OR negation (De Morgan's Law)
- **Test Cases**: 25 test cases covering:
  - Guard conditions for adding songs to playlist
  - Concurrent operation prevention
  - Playlist ID validation (integer, positive)
  - Route visibility for signin/signup/home/profile
  - Case sensitivity in route matching
  - URL parameter handling
  - De Morgan's Law verification
- **MC/DC Coverage**: All conditions independently verified

## Running the Tests

### Backend Tests

#### Prerequisites
```bash
cd uniworld-backend
mvn clean install
```

#### Run All MC/DC Tests
```bash
# All MC/DC tests
mvn test -Dtest=*MCDCTest

# Specific test class
mvn test -Dtest=SearchControllerMCDCTest
mvn test -Dtest=AuthServiceLoginMCDCTest
mvn test -Dtest=JwtServiceValidationMCDCTest
mvn test -Dtest=PlaylistAuthorizationMCDCTest
mvn test -Dtest=MusicLibraryInitializerMCDCTest
```

#### Generate Coverage Report
```bash
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

#### Run Specific Test Nested Class
```bash
mvn test -Dtest=SearchControllerMCDCTest#IndependentConditionTests
```

### Frontend Tests

#### Prerequisites
```bash
cd uniworld-frontend
npm install
```

#### Run All MC/DC Tests
```bash
# All MC/DC tests
npm test -- **/*.mcdc.spec.ts

# Specific test file
npm test -- player.mcdc.spec.ts
npm test -- app.mcdc.spec.ts
```

#### With Coverage
```bash
npm test -- --coverage **/*.mcdc.spec.ts
```

#### Watch Mode (for development)
```bash
npm test -- --watch **/*.mcdc.spec.ts
```

## MC/DC Coverage Summary

| Component | Decision Type | Conditions | Test Cases | Status |
|-----------|---------------|-----------|-----------|--------|
| SearchController.matchesSong | 5-part OR | 5 | 17 | ✓ Complete |
| AuthService.login | 4-part OR | 4 | 13 | ✓ Complete |
| JwtService.isTokenValid | 2-part AND | 2 | 14 | ✓ Complete |
| PlaylistAuthorizationMCDCTest | Negated Equals | 1 | 13 | ✓ Complete |
| MusicLibraryInitializer | Cascading (3+2+3) | 8 | 18 | ✓ Complete |
| Player.onAudioEnded | 3-part AND | 3 | 20 | ✓ Complete |
| SearchResult Guard | 2-part | 2 | 4 | ✓ Complete |
| Route Visibility | 2-part OR NOT | 2 | 10 | ✓ Complete |
| Playlist ID Validation | 2-part AND | 2 | 6 | ✓ Complete |
| **TOTAL** | | | **115** | ✓ |

## Key MC/DC Principles Applied

### 1. **Independent Condition Coverage**
Each test verifies that changing one condition from true to false (or vice versa) changes the outcome of the decision, while all other conditions remain constant.

**Example (SearchController):**
- TC1: Only Title matches → TRUE
- TC2: Only Genre matches → TRUE (verifies Genre independently affects outcome)
- TC6: Nothing matches → FALSE

### 2. **Negation Handling**
Tests specifically verify negation operators (!) work correctly:
- When negation converts true to false
- When negation converts false to true
- De Morgan's Laws for complex negations

**Example (Player):**
- TC1: `!isShuffleEnabled` (shuffle OFF) → condition TRUE
- TC2: `!isShuffleEnabled` (shuffle ON) → condition FALSE

### 3. **Short-Circuit Logic**
Tests verify AND/OR operators short-circuit correctly:
- First false in AND prevents evaluation of later conditions
- First true in OR prevents evaluation of later conditions

**Example (AuthService):**
- `if (email==null || email.isBlank() || password==null || password.isBlank())`
- TC1: Only email null → Exception (short-circuits at first condition)
- TC2: Email valid, password null → Exception (evaluates to third condition)

### 4. **Boundary Conditions**
Tests include edge cases:
- Null values
- Empty strings vs blank strings (whitespace)
- Zero, negative, and maximum values
- Case sensitivity in string comparisons
- Off-by-one errors in index comparisons

### 5. **Guard Conditions**
Tests for defensive programming:
- Null pointer prevention
- Collection emptiness checks
- Index boundary validation
- Reference existence verification

## Test Organization Pattern

Each test file follows this structure:

```
├── @Nested IndependentConditionTests (or Base MC/DC Cases)
│   ├── TC1: First condition independent
│   ├── TC2: Second condition independent
│   └── ...
├── @Nested EdgeCaseTests
│   ├── TC_N: Boundary conditions
│   └── ...
├── @Nested NullHandlingTests
│   ├── Null references
│   └── Default values
├── @Nested CoverageMatrixTests
│   └── Truth table verification
└── Decision Table/Matrix Documentation
```

## Decision Coverage Verification

For each test file, a decision table documents:
- All condition combinations (truth tables)
- MC/DC coverage markers (T/F for each condition)
- Result for each combination
- Which conditions are independently verified

**Example Decision Table:**

| Test | C1 | C2 | C3 | Result | Notes |
|------|----|----|----|----|---------|
| TC1  | T | T | T | T | All conditions true |
| TC2  | F | T | T | F | C1 independent |
| TC3  | T | F | T | F | C2 independent |
| TC4  | T | T | F | F | C3 independent |

## Coverage Metrics

### Recommended Coverage Levels
- **Line Coverage**: 100% (all executable code)
- **Branch Coverage**: 100% (all if/else paths)
- **MC/DC Coverage**: ≥80% (all conditions independently verified)

### Checking Coverage

**Backend (with Jacoco):**
```
- Line Coverage: Look for green highlighting in src/ files
- Branch Coverage: Verify all if/else branches executed
- Check target/site/jacoco/index.html
```

**Frontend (with Istanbul/Nyc):**
```
npm test -- --coverage
# Check coverage/index.html
```

## Common MC/DC Patterns in UniWorld

### Pattern 1: Cascading Null Checks
```java
if (obj != null && obj.field != null && !obj.field.isBlank())
```
**Requires**: Test all 8 combinations of null/not-null

### Pattern 2: OR Validation
```java
if (email == null || email.isBlank() || password == null || password.isBlank())
```
**Requires**: Each condition must independently trigger the decision

### Pattern 3: Negated Boolean
```java
if (!isShuffleEnabled && repeatMode == 'off' && isLastSong)
```
**Requires**: Verify negation operator is correctly handled

### Pattern 4: De Morgan's Law
```javascript
showSidebar = !(url.startsWith('/signin') || url.startsWith('/signup'))
```
**Requires**: Test all combinations of both OR conditions and negation

## Maintenance Guidelines

1. **Update Tests When Logic Changes**
   - Any modification to decision conditions requires test review
   - Update decision table if conditions change
   - Add new test cases for new conditions

2. **Verify Coverage After Changes**
   - Run full test suite after modifications
   - Regenerate coverage reports
   - Ensure MC/DC coverage remains ≥80%

3. **Document Complex Decisions**
   - Add decision table comments in complex test classes
   - Explain unusual condition combinations
   - Reference business logic requirements

## References

- **MC/DC Definition**: DO-178B/C Software Verification Guidance
- **Coverage Levels**: RTCA/DO-178B Compliance Levels
- **Best Practices**: NIST SP 800-53 Software Testing Recommendations

## Next Steps

1. **Run Tests**: Execute test suite to verify all MC/DC cases pass
2. **Generate Coverage Reports**: Document baseline coverage metrics
3. **Integrate with CI/CD**: Add MC/DC tests to automated build pipeline
4. **Monitor Coverage**: Track coverage trends over project lifetime
5. **Refine Tests**: Add additional cases for edge scenarios discovered in production
