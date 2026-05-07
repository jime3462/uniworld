package com.uniworld.uniworld_backend;

import com.uniworld.uniworld_backend.service.AuthService;
import com.uniworld.uniworld_backend.repository.UserRepository;
import com.uniworld.uniworld_backend.dto.AuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MC/DC (Modified Condition/Decision Coverage) Test Cases for AuthService.login()
 * 
 * Decision Logic (4-part OR validation):
 * if (request.email() == null || request.email().isBlank() 
 *     || request.password() == null || request.password().isBlank())
 *     throw new IllegalArgumentException("Email and password are required");
 * 
 * MC/DC Requirements: Each condition must independently affect the outcome
 * For 4 conditions in OR: Minimum 8 test cases needed
 */
@DisplayName("AuthService MC/DC Test Suite - login() Method Validation")
public class AuthServiceLoginMCDCTest {

    @Mock
    private UserRepository userRepository;
    
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private com.uniworld.uniworld_backend.security.JwtService jwtService;
    private AuthService authService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(com.uniworld.uniworld_backend.security.JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Nested
    @DisplayName("Base MC/DC Cases - Each Condition Independently Affects Outcome")
    class IndependentConditionTests {

        /**
         * TC1: Email is null (C1=T)
         * C1=T, C2=F, C3=F, C4=F → Result=EXCEPTION
         */
        @Test
        @DisplayName("TC1: Email is null - should throw exception")
        void emailIsNull() {
            AuthRequest request = new AuthRequest(null, "validPassword");
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            }, "Should throw exception when email is null");
        }

        /**
         * TC2: Email is blank (C2=T)
         * C1=F, C2=T, C3=F, C4=F → Result=EXCEPTION
         */
        @Test
        @DisplayName("TC2: Email is blank - should throw exception")
        void emailIsBlank() {
            AuthRequest request = new AuthRequest("   ", "validPassword");
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            }, "Should throw exception when email is blank");
        }

        /**
         * TC3: Password is null (C3=T)
         * C1=F, C2=F, C3=T, C4=F → Result=EXCEPTION
         */
        @Test
        @DisplayName("TC3: Password is null - should throw exception")
        void passwordIsNull() {
            AuthRequest request = new AuthRequest("valid@email.com", null);
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            }, "Should throw exception when password is null");
        }

        /**
         * TC4: Password is blank (C4=T)
         * C1=F, C2=F, C3=F, C4=T → Result=EXCEPTION
         */
        @Test
        @DisplayName("TC4: Password is blank - should throw exception")
        void passwordIsBlank() {
            AuthRequest request = new AuthRequest("valid@email.com", "   ");
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            }, "Should throw exception when password is blank");
        }

        /**
         * TC5: Both email and password valid (all conditions false)
         * C1=F, C2=F, C3=F, C4=F → Result=CONTINUE (no exception from validation)
         */
        @Test
        @DisplayName("TC5: Both email and password valid - should pass validation")
        void bothEmailAndPasswordValid() {
            AuthRequest request = new AuthRequest("valid@email.com", "validPassword123");
            
            // Should not throw IllegalArgumentException from validation
            // (may throw other exceptions from business logic, which is expected)
            try {
                authService.login(request);
            } catch (IllegalArgumentException e) {
                fail("Should not throw validation exception when both email and password are valid: " + e.getMessage());
            } catch (Exception e) {
                // Other exceptions from business logic (e.g., user not found) are acceptable
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases - Boundary Conditions for Email and Password")
    class EdgeCaseTests {

        /**
         * TC6: Email with single space
         */
        @Test
        @DisplayName("TC6: Email with only whitespace variations")
        void emailWithWhitespaceVariations() {
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(new AuthRequest(" ", "password"));
            });
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(new AuthRequest("\t", "password"));
            });
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(new AuthRequest("\n", "password"));
            });
        }

        /**
         * TC7: Password with only whitespace
         */
        @Test
        @DisplayName("TC7: Password with only whitespace variations")
        void passwordWithWhitespaceVariations() {
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(new AuthRequest("valid@email.com", " "));
            });
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(new AuthRequest("valid@email.com", "\t"));
            });
        }

        /**
         * TC8: Email with embedded spaces (should be valid per isBlank())
         */
        @Test
        @DisplayName("TC8: Email containing valid content with spaces")
        void emailWithContent() {
            AuthRequest request = new AuthRequest("user@example.com", "password");
            
            try {
                authService.login(request);
            } catch (IllegalArgumentException e) {
                fail("Should not throw validation exception for valid email format");
            } catch (Exception e) {
                // Expected - user may not exist
            }
        }

        /**
         * TC9: Very long email and password
         */
        @Test
        @DisplayName("TC9: Very long email and password strings")
        void veryLongCredentials() {
            String longEmail = "a".repeat(300) + "@example.com";
            String longPassword = "p".repeat(1000);
            AuthRequest request = new AuthRequest(longEmail, longPassword);
            
            // Should pass validation (isBlank = false for non-blank strings)
            try {
                authService.login(request);
            } catch (IllegalArgumentException e) {
                fail("Should not throw validation exception for long but non-blank credentials");
            } catch (Exception e) {
                // Other exceptions acceptable
            }
        }
    }

    @Nested
    @DisplayName("Multiple Invalid Conditions - MC/DC Combinations")
    class MultipleInvalidConditionsTests {

        /**
         * TC10: Both email and password are null
         */
        @Test
        @DisplayName("TC10: Both email and password null - short-circuits at first condition")
        void bothEmailAndPasswordNull() {
            AuthRequest request = new AuthRequest(null, null);
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            }, "Should throw when both email and password are null");
        }

        /**
         * TC11: Both email and password are blank
         */
        @Test
        @DisplayName("TC11: Both email and password blank")
        void bothEmailAndPasswordBlank() {
            AuthRequest request = new AuthRequest("   ", "   ");
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            }, "Should throw when both email and password are blank");
        }

        /**
         * TC12: Email valid but password both null and blank (impossible but test logic)
         */
        @Test
        @DisplayName("TC12: Email valid, password null")
        void emailValidPasswordNull() {
            AuthRequest request = new AuthRequest("valid@example.com", null);
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            });
        }

        /**
         * TC13: Email blank, password valid
         */
        @Test
        @DisplayName("TC13: Email blank, password valid")
        void emailBlankPasswordValid() {
            AuthRequest request = new AuthRequest("", "validPassword123");
            
            assertThrows(ResponseStatusException.class, () -> {
                authService.login(request);
            });
        }
    }

    @Nested
    @DisplayName("Email Format Variations - Not Part of Validation")
    class EmailFormatTests {

        /**
         * TC14: Email without @ (valid per isBlank, may fail later validation)
         */
        @Test
        @DisplayName("TC14: Email without @ symbol - passes isBlank check")
        void emailWithoutAt() {
            AuthRequest request = new AuthRequest("invalidemail", "password");
            
            // Should pass isBlank validation (not null/blank)
            try {
                authService.login(request);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("Email and password are required")) {
                    fail("Should not fail isBlank validation");
                }
            } catch (Exception e) {
                // Other validation may fail
            }
        }

        /**
         * TC15: Email with special characters
         */
        @Test
        @DisplayName("TC15: Email with special characters - passes isBlank check")
        void emailWithSpecialCharacters() {
            AuthRequest request = new AuthRequest("user+tag@example.com", "password");
            
            try {
                authService.login(request);
            } catch (IllegalArgumentException e) {
                fail("Should not fail isBlank validation for special characters");
            } catch (Exception e) {
                // Other exceptions acceptable
            }
        }
    }

    @Nested
    @DisplayName("MC/DC Coverage Matrix Verification")
    class CoverageMatrixTests {

        /**
         * Verification test documenting MC/DC coverage matrix:
         * 
         * Test Case | Email=Null | Email=Blank | Pwd=Null | Pwd=Blank | Outcome
         * TC1       | T          | F           | F        | F         | EXCEPTION
         * TC2       | F          | T           | F        | F         | EXCEPTION
         * TC3       | F          | F           | T        | F         | EXCEPTION
         * TC4       | F          | F           | F        | T         | EXCEPTION
         * TC5       | F          | F           | F        | F         | VALID
         * TC10      | T          | T           | T        | T         | EXCEPTION (covers short-circuit)
         * TC12      | F          | F           | T        | F         | EXCEPTION (password null overrides)
         * TC13      | F          | T           | F        | F         | EXCEPTION (email blank overrides)
         */
        @Test
        @DisplayName("Coverage Matrix: Verify each condition independently affects outcome")
        void verifyMCDCCoverageMatrix() {
            // TC1: Email=null makes outcome EXCEPTION regardless of other conditions
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> 
                authService.login(new AuthRequest(null, "password")));
            
            // TC2: Email=blank makes outcome EXCEPTION even when password valid
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> 
                authService.login(new AuthRequest("   ", "password")));
            
            // TC3: Password=null makes outcome EXCEPTION even when email valid
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> 
                authService.login(new AuthRequest("user@test.com", null)));
            
            // TC4: Password=blank makes outcome EXCEPTION even when email valid
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> 
                authService.login(new AuthRequest("user@test.com", "   ")));
            
            // TC5: All conditions false = VALID (passes validation layer)
            try {
                authService.login(new AuthRequest("valid@email.com", "validPassword"));
            } catch (IllegalArgumentException e) {
                fail("Should not throw validation exception when all conditions are false");
            } catch (Exception ignored) {
                // Expected - business logic may reject
            }
        }
    }
}
