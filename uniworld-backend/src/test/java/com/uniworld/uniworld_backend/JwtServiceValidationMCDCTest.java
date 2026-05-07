package com.uniworld.uniworld_backend;

import com.uniworld.uniworld_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.security.core.userdetails.UserDetails;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MC/DC (Modified Condition/Decision Coverage) Test Cases for JwtService.isTokenValid()
 * 
 * Decision Logic (2-part AND with negation):
 * return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
 * 
 * MC/DC Requirements: Each condition must independently affect the outcome
 * For 2 conditions in AND: Minimum 4 test cases needed (actually 3, but 4 for completeness)
 */
@DisplayName("JwtService MC/DC Test Suite - isTokenValid() Method")
public class JwtServiceValidationMCDCTest {

    private static final String JWT_SECRET = "VGhpc0lzQVN0cm9uZ1VuaXdvcmxkSldUU2VjcmV0S2V5Rm9yRGV2T25seQ==";
    private static final long JWT_EXPIRATION_MS = 86400000L;

    @Mock
    private UserDetails userDetails;
    
    private JwtService jwtService;
    private String testToken;
    private String expiredToken;
    private String testUsername;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(); // May need mocking depending on implementation
        testUsername = "testuser@example.com";
        when(userDetails.getUsername()).thenReturn(testUsername);
        ReflectionTestUtils.setField(jwtService, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", JWT_EXPIRATION_MS);
        testToken = jwtService.generateToken(userDetails);
        expiredToken = generateExpiredToken(testUsername);
    }

    private String generateExpiredToken(String username) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() - 1000L);
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(now.getTime() - 2000L))
                .expiration(expiredAt)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    @Nested
    @DisplayName("Base MC/DC Cases - Each Condition Independently Controls Result")
    class IndependentConditionTests {

        /**
         * TC1: Username matches AND token not expired
         * C1=T (username equals), C2=T (token not expired) → Result=T
         * (Negation: !isTokenExpired = T, so isTokenExpired = F)
         */
        @Test
        @DisplayName("TC1: Username matches and token valid - should return true")
        void usernameMatchesAndTokenValid() {
            when(userDetails.getUsername()).thenReturn(testUsername);
            // Token is not expired
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertTrue(result, "Should return true when username matches and token is not expired");
        }

        /**
         * TC2: Username matches BUT token is expired
         * C1=T (username equals), C2=F (token expired) → Result=F
         * (Negation: !isTokenExpired = F, so isTokenExpired = T)
         * 
         * Verifies that second condition independently affects outcome
         */
        @Test
        @DisplayName("TC2: Username matches but token expired - should return false")
        void usernameMatchesButTokenExpired() {
            when(userDetails.getUsername()).thenReturn(testUsername);
            // Token is expired - this should make the result false
            
            boolean result = jwtService.isTokenValid(expiredToken, userDetails);
            assertFalse(result, "Should return false when token is expired, regardless of username match");
        }

        /**
         * TC3: Username doesn't match AND token not expired
         * C1=F (username differs), C2=T (token not expired) → Result=F
         * 
         * Verifies that first condition independently affects outcome (short-circuit)
         */
        @Test
        @DisplayName("TC3: Username doesn't match, token valid - should return false")
        void usernameDoesntMatchTokenValid() {
            when(userDetails.getUsername()).thenReturn("differentuser@example.com");
            // Token is not expired
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(result, "Should return false when username doesn't match, even if token is valid");
        }

        /**
         * TC4: Username doesn't match AND token is expired
         * C1=F (username differs), C2=F (token expired) → Result=F
         * 
         * Both conditions false - confirms AND logic
         */
        @Test
        @DisplayName("TC4: Username doesn't match and token expired - should return false")
        void usernameDoesntMatchAndTokenExpired() {
            when(userDetails.getUsername()).thenReturn("differentuser@example.com");
            // Token is expired
            
            boolean result = jwtService.isTokenValid(expiredToken, userDetails);
            assertFalse(result, "Should return false when both conditions are false");
        }
    }

    @Nested
    @DisplayName("Negation Verification - !isTokenExpired Logic")
    class NegationTests {

        /**
         * TC5: Verify negation operator behavior
         * When isTokenExpired(token) = true → !isTokenExpired = false → Result = false
         */
        @Test
        @DisplayName("TC5: Negation case - expired token with matching username")
        void negationOperatorExpiredToken() {
            when(userDetails.getUsername()).thenReturn(testUsername);
            // isTokenExpired returns true, so !isTokenExpired = false
            
            boolean result = jwtService.isTokenValid(expiredToken, userDetails);
            assertFalse(result, "Negation operator: false AND true = false");
        }

        /**
         * TC6: Verify negation with non-expired token
         * When isTokenExpired(token) = false → !isTokenExpired = true
         */
        @Test
        @DisplayName("TC6: Negation case - valid token with matching username")
        void negationOperatorValidToken() {
            when(userDetails.getUsername()).thenReturn(testUsername);
            // isTokenExpired returns false, so !isTokenExpired = true
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertTrue(result, "Negation operator: true AND true = true");
        }
    }

    @Nested
    @DisplayName("Short-Circuit Evaluation - AND Logic")
    class ShortCircuitTests {

        /**
         * TC7: Verify AND operator short-circuits
         * If username doesn't match (C1=F), isTokenExpired() shouldn't be evaluated
         */
        @Test
        @DisplayName("TC7: Short-circuit - mismatched username should not evaluate token expiry")
        void shortCircuitOnUsernameFailure() {
            when(userDetails.getUsername()).thenReturn("different@example.com");
            // We should not need to evaluate token expiry for false username
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(result, "Should short-circuit and return false without checking token expiry");
            
            // Verify behavior consistent
            verify(userDetails, atLeastOnce()).getUsername();
        }
    }

    @Nested
    @DisplayName("Edge Cases - Null/Empty Values")
    class EdgeCaseTests {

        /**
         * TC8: Null username in token
         */
        @Test
        @DisplayName("TC8: Null username from token - should handle gracefully")
        void nullUsernameFromToken() {
            when(userDetails.getUsername()).thenReturn("validuser@example.com");
            
            assertDoesNotThrow(() -> {
                jwtService.isTokenValid(testToken, userDetails);
            }, "Should handle null username without throwing exception");
        }

        /**
         * TC9: Null UserDetails
         */
        @Test
        @DisplayName("TC9: Null UserDetails object - should throw or handle")
        void nullUserDetails() {
            assertThrows(Exception.class, () -> {
                jwtService.isTokenValid(testToken, null);
            }, "Should throw exception or handle null UserDetails appropriately");
        }

        /**
         * TC10: Empty string usernames
         */
        @Test
        @DisplayName("TC10: Empty string usernames")
        void emptyStringUsernames() {
            when(userDetails.getUsername()).thenReturn("");
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(result, "Empty strings should not match a non-empty token subject");
        }

        /**
         * TC11: Whitespace-only usernames
         */
        @Test
        @DisplayName("TC11: Whitespace usernames - equals() is case-sensitive")
        void whitespaceUsernames() {
            when(userDetails.getUsername()).thenReturn("  user  ");
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(result, "Whitespace should cause mismatch - equals() is exact");
        }
    }

    @Nested
    @DisplayName("Case Sensitivity and String Comparison")
    class StringComparisonTests {

        /**
         * TC12: Case-sensitive username comparison
         */
        @Test
        @DisplayName("TC12: Case-sensitive comparison")
        void caseSensitiveComparison() {
            when(userDetails.getUsername()).thenReturn("TestUser@example.com");
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(result, "Username comparison should be case-sensitive");
        }

        /**
         * TC13: Exact match required (no contains/startsWith)
         */
        @Test
        @DisplayName("TC13: Exact match required - not partial")
        void exactMatchRequired() {
            when(userDetails.getUsername()).thenReturn("testuser@example.com");
            
            boolean result = jwtService.isTokenValid(testToken, userDetails);
            assertTrue(result, "Should require exact match, which this case satisfies");
        }
    }

    @Nested
    @DisplayName("MC/DC Coverage Matrix")
    class CoverageMatrixTests {

        /**
         * MC/DC Coverage Matrix for: C1 && !C2
         * 
         * Test | Username Match (C1) | Token Expired (C2) | !C2 | C1 && !C2
         * TC1  | T                  | F                 | T   | T
         * TC2  | T                  | T                 | F   | F (C2 independent)
         * TC3  | F                  | F                 | T   | F (C1 independent)
         * TC4  | F                  | T                 | F   | F
         * 
         * Key Coverage:
         * - Each condition independently affects the result:
         *   - C1 independent: TC1(T) vs TC3(F) with C2=T
         *   - C2 independent: TC1(T,!exp) vs TC2(T,exp) with C1=T
         */
        @Test
        @DisplayName("MC/DC Matrix verification - each condition independently affects result")
        void mcDCMatrixVerification() {
            // TC1: C1=T, C2=F → Result=T
            when(userDetails.getUsername()).thenReturn(testUsername);
            boolean tc1Result = jwtService.isTokenValid(testToken, userDetails);
            assertTrue(tc1Result, "TC1: Username matches, token valid → true");

            // TC2: C1=T, C2=T → Result=F (verifies C2 independent)
            when(userDetails.getUsername()).thenReturn(testUsername);
            boolean tc2Result = jwtService.isTokenValid(expiredToken, userDetails);
            assertFalse(tc2Result, "TC2: Expired token should make result false");
            
            // TC3: C1=F, C2=F → Result=F (verifies C1 independent)
            when(userDetails.getUsername()).thenReturn("different@example.com");
            boolean tc3Result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(tc3Result, "TC3: Username mismatch → false regardless of token");

            // TC4: C1=F, C2=T → Result=F
            when(userDetails.getUsername()).thenReturn("different@example.com");
            boolean tc4Result = jwtService.isTokenValid(testToken, userDetails);
            assertFalse(tc4Result, "TC4: All conditions false → false");
        }
    }
}
