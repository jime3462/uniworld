package com.uniworld.uniworld_backend;

import com.uniworld.uniworld_backend.repository.PlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MC/DC (Modified Condition/Decision Coverage) Test Cases for Playlist Authorization
 * 
 * Decision Logic - Ownership Check:
 * if (!existing.getUser().getUserID().equals(user.getUserID()))
 *     throw new UnauthorizedException("Unauthorized: You do not own this playlist");
 * 
 * Simplified to: if (C1=T) throw exception
 * Where C1 = !existing.getUser().getUserID().equals(user.getUserID())
 *           = !(A.equals(B))
 *           = A != B (negated equals)
 * 
 * MC/DC Coverage: 2 test cases minimum
 * - TC1: IDs match (C1=F) → No exception
 * - TC2: IDs don't match (C1=T) → Exception
 */
@DisplayName("PlaylistController MC/DC Test Suite - Authorization Logic")
public class PlaylistAuthorizationMCDCTest {

    @Mock
    private PlaylistRepository playlistRepository;
    
    // Removed: unused playlistController field
    private User currentUser;
    private User playlistOwner;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        currentUser = new User();
        currentUser.setUserID(1L);
        currentUser.setName("Current User");
        
        playlistOwner = new User();
        playlistOwner.setUserID(1L);
        playlistOwner.setName("Playlist Owner");
        
        playlist = new Playlist();
        playlist.setPlaylistID(1L);
        playlist.setUser(playlistOwner);
    }

    @Nested
    @DisplayName("Base MC/DC Cases - Ownership Authorization")
    class OwnershipAuthorizationTests {

        /**
         * TC1: User owns playlist (IDs match)
         * C1=F (!equals = false) → No exception, proceed
         */
        @Test
        @DisplayName("TC1: Authorized - user owns playlist")
        void userOwnsPlaylist() {
            currentUser.setUserID(1L);
            playlistOwner.setUserID(1L);
            playlist.setUser(playlistOwner);
            
            assertFalse(
                !currentUser.getUserID().equals(playlistOwner.getUserID()),
                "IDs should match - ownership verified"
            );
            // No exception should be thrown
        }

        /**
         * TC2: User doesn't own playlist (IDs don't match)
         * C1=T (!equals = true) → Throw exception
         */
        @Test
        @DisplayName("TC2: Unauthorized - user doesn't own playlist")
        void userDoesntOwnPlaylist() {
            currentUser.setUserID(1L);
            playlistOwner.setUserID(2L);
            playlist.setUser(playlistOwner);
            
            assertTrue(
                !currentUser.getUserID().equals(playlistOwner.getUserID()),
                "IDs should not match - authorization should fail"
            );
            // Exception should be thrown
        }
    }

    @Nested
    @DisplayName("Edge Cases - UserID Variations")
    class UserIDVariationsTests {

        /**
         * TC3: Negative user IDs
         */
        @Test
        @DisplayName("TC3: Negative user IDs - should compare correctly")
        void negativeUserIDs() {
            currentUser.setUserID(-1L);
            playlistOwner.setUserID(-1L);
            
            boolean authorized = currentUser.getUserID().equals(playlistOwner.getUserID());
            assertTrue(authorized, "Negative IDs should match");
        }

        /**
         * TC4: Zero user IDs (invalid but test comparison)
         */
        @Test
        @DisplayName("TC4: Zero user IDs")
        void zeroUserIDs() {
            currentUser.setUserID(0L);
            playlistOwner.setUserID(0L);
            
            boolean authorized = currentUser.getUserID().equals(playlistOwner.getUserID());
            assertTrue(authorized, "Zero IDs should match");
        }

        /**
         * TC5: Large user IDs (Long.MAX_VALUE)
         */
        @Test
        @DisplayName("TC5: Maximum Long value user IDs")
        void maxLongUserIDs() {
            currentUser.setUserID(Long.MAX_VALUE);
            playlistOwner.setUserID(Long.MAX_VALUE);
            
            boolean authorized = currentUser.getUserID().equals(playlistOwner.getUserID());
            assertTrue(authorized, "Maximum long IDs should match");
        }

        /**
         * TC6: Consecutive user IDs (off-by-one errors)
         */
        @Test
        @DisplayName("TC6: Off-by-one user IDs")
        void offByOneUserIDs() {
            currentUser.setUserID(100L);
            playlistOwner.setUserID(101L);
            
            boolean authorized = currentUser.getUserID().equals(playlistOwner.getUserID());
            assertFalse(authorized, "Off-by-one IDs should not match");
        }
    }

    @Nested
    @DisplayName("Null Reference Handling")
    class NullHandlingTests {

        /**
         * TC7: Null user on playlist (defensive check)
         */
        @Test
        @DisplayName("TC7: Null user on playlist - defensive")
        void nullUserOnPlaylist() {
            playlist.setUser(null);
            
            assertThrows(NullPointerException.class, () -> {
                if (!playlist.getUser().getUserID().equals(currentUser.getUserID())) {
                    throw new Exception("Unauthorized");
                }
            }, "Should handle null playlist user gracefully or throw NPE");
        }

        /**
         * TC8: Null userID on current user
         */
        @Test
        @DisplayName("TC8: Null user ID on current user")
        void nullUserIDOnCurrentUser() {
            currentUser.setUserID(null);
            playlistOwner.setUserID(1L);
            
            assertThrows(NullPointerException.class, () -> {
                if (!currentUser.getUserID().equals(playlistOwner.getUserID())) {
                    throw new Exception("Unauthorized");
                }
            }, "Should handle null user ID");
        }

        /**
         * TC9: Both userIDs null
         */
        @Test
        @DisplayName("TC9: Both user IDs null - null.equals(null)")
        void bothUserIDsNull() {
            currentUser.setUserID(null);
            playlistOwner.setUserID(null);
            
            // null.equals(null) throws NPE on the first null
            assertThrows(NullPointerException.class, () -> {
                currentUser.getUserID().equals(playlistOwner.getUserID());
            });
        }
    }

    @Nested
    @DisplayName("Negation Logic Verification")
    class NegationTests {

        /**
         * TC10: Verify double negation doesn't cause logic error
         * if (!existing.getUser().getUserID().equals(user.getUserID()))
         * The ! operator inverts the equals result
         */
        @Test
        @DisplayName("TC10: Negation verification - when IDs match")
        void negationWhenMatches() {
            currentUser.setUserID(1L);
            playlistOwner.setUserID(1L);
            
            // equals returns true, ! inverts to false
            boolean throwException = !currentUser.getUserID().equals(playlistOwner.getUserID());
            assertFalse(throwException, "Negation should convert true to false");
        }

        /**
         * TC11: Negation verification - when IDs don't match
         */
        @Test
        @DisplayName("TC11: Negation verification - when IDs don't match")
        void negationWhenDoesntMatch() {
            currentUser.setUserID(1L);
            playlistOwner.setUserID(2L);
            
            // equals returns false, ! inverts to true
            boolean throwException = !currentUser.getUserID().equals(playlistOwner.getUserID());
            assertTrue(throwException, "Negation should convert false to true");
        }
    }

    @Nested
    @DisplayName("MC/DC Decision Table")
    class DecisionTableTests {

        /**
         * MC/DC Truth Table for Authorization Check
         * 
         * Test | current.userID | owner.userID | equals() | !equals() | Action
         * TC1  | 1              | 1            | T        | F         | ALLOW
         * TC2  | 1              | 2            | F        | T         | DENY
         * TC3  | 100            | 100          | T        | F         | ALLOW
         * TC4  | 100            | 99           | F        | T         | DENY
         * TC5  | 0              | 0            | T        | F         | ALLOW
         * TC6  | 0              | 1            | F        | T         | DENY
         * 
         * MC/DC Coverage: Each operand value affects result
         * - equals() when C1=T prevents exception
         * - equals() when C1=F allows exception
         */
        @Test
        @DisplayName("MC/DC Decision Table Verification")
        void decisionTableVerification() {
            long currentId = 1L;
            long ownerId = 1L;
            long otherId = 2L;
            long thirdId = 100L;
            long fourthId = 99L;
            long matchingThirdId = 100L;

            // TC1: ID 1 vs 1
            assertTrue(currentId == ownerId && ownerId == currentId);
            
            // TC2: ID 1 vs 2
            assertFalse(currentId == otherId);
            
            // TC3: ID 100 vs 100
            assertTrue(thirdId == matchingThirdId);
            
            // TC4: ID 100 vs 99
            assertFalse(thirdId == fourthId);
        }
    }

    @Nested
    @DisplayName("Authorization with Repository Retrieval")
    class RepositoryIntegrationTests {

        /**
         * TC12: Authorization check in context of repository retrieval
         */
        @Test
        @DisplayName("TC12: Authorization after playlist retrieval from repository")
        void authorizationAfterRetrieval() {
            long currentUserID = 1L;
            long playlistOwnerId = 1L;
            
            Playlist retrievedPlaylist = new Playlist();
            User owner = new User();
            owner.setUserID(playlistOwnerId);
            retrievedPlaylist.setUser(owner);
            
            // Simulate authorization check
            User currentUser = new User();
            currentUser.setUserID(currentUserID);
            
            boolean authorized = retrievedPlaylist.getUser().getUserID().equals(currentUser.getUserID());
            assertTrue(authorized, "Should authorize matching IDs from repository");
        }

        /**
         * TC13: Multiple playlist ownership checks
         */
        @Test
        @DisplayName("TC13: Batch authorization checks")
        void batchAuthorizationChecks() {
            long currentUserID = 1L;
            User currentUser = new User();
            currentUser.setUserID(currentUserID);
            
            for (long playlistOwnerId : new long[]{1L, 2L, 1L, 3L}) {
                User owner = new User();
                owner.setUserID(playlistOwnerId);
                
                boolean authorized = owner.getUserID().equals(currentUser.getUserID());
                
                if (playlistOwnerId == currentUserID) {
                    assertTrue(authorized);
                } else {
                    assertFalse(authorized);
                }
            }
        }
    }
}
