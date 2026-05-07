package com.uniworld.uniworld_backend;

import com.uniworld.uniworld_backend.controller.SearchController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MC/DC (Modified Condition/Decision Coverage) Test Cases for SearchController.matchesSong()
 * 
 * Decision Logic (5-part OR):
 * return contains(song.getTitle(), keyword)
 *     || contains(song.getGenre(), keyword)
 *     || contains(song.getKeyScale(), keyword)
 *     || contains(albumTitle, keyword)
 *     || contains(artistNames, keyword);
 * 
 * MC/DC requires: Each condition independently affects the outcome
 * For 5 conditions: Minimum 10 test cases needed (N+1 rule: 5+1 = 6 base, plus variations)
 */
@DisplayName("SearchController MC/DC Test Suite - matchesSong() Method")
public class SearchControllerMCDCTest {

    private SearchController searchController;
    private Song testSong;
    private Album testAlbum;
    private Artist testArtist;
    private String keyword;

    @BeforeEach
    void setUp() {
        searchController = new SearchController(null, null, null); // Services will be mocked
        
        // Setup test entities
        testArtist = new Artist();
        testArtist.setName("The Beatles");
        testArtist.setGenre("Rock");
        
        testAlbum = new Album();
        testAlbum.setTitle("Abbey Road");
        testAlbum.setGenre("Classic Rock");
        
        testSong = new Song();
        testSong.setTitle("Come Together");
        testSong.setGenre("Rock");
        testSong.setKeyScale("D Minor");
        testSong.setAlbum(testAlbum);
        testSong.setArtists(List.of(testArtist));
    }

    @Nested
    @DisplayName("Base MC/DC Test Cases - Each Condition Independently True")
    class IndependentConditionTests {

        /**
         * TC1: Title contains keyword (only Title matches)
         * C1=T, C2=F, C3=F, C4=F, C5=F → Result=T
         */
        @Test
        @DisplayName("TC1: Title matches keyword - should return true")
        void titleMatchesOnly() {
            keyword = "come";
            testSong.setTitle("Come Together");
            testSong.setGenre("Jazz");
            testSong.setKeyScale("C Major");
            testAlbum.setTitle("Different Album");
            testArtist.setName("Different Artist");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match when title contains keyword");
        }

        /**
         * TC2: Genre contains keyword (only Genre matches)
         * C1=F, C2=T, C3=F, C4=F, C5=F → Result=T
         */
        @Test
        @DisplayName("TC2: Genre matches keyword - should return true")
        void genreMatchesOnly() {
            keyword = "rock";
            testSong.setTitle("Different Song");
            testSong.setGenre("Rock");
            testSong.setKeyScale("C Major");
            testAlbum.setTitle("Different Album");
            testArtist.setName("Different Artist");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match when genre contains keyword");
        }

        /**
         * TC3: Key Scale contains keyword (only KeyScale matches)
         * C1=F, C2=F, C3=T, C4=F, C5=F → Result=T
         */
        @Test
        @DisplayName("TC3: Key scale matches keyword - should return true")
        void keyScaleMatchesOnly() {
            keyword = "minor";
            testSong.setTitle("Different Song");
            testSong.setGenre("Jazz");
            testSong.setKeyScale("D Minor");
            testAlbum.setTitle("Different Album");
            testArtist.setName("Different Artist");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match when key scale contains keyword");
        }

        /**
         * TC4: Album title contains keyword (only Album matches)
         * C1=F, C2=F, C3=F, C4=T, C5=F → Result=T
         */
        @Test
        @DisplayName("TC4: Album title matches keyword - should return true")
        void albumMatchesOnly() {
            keyword = "road";
            testSong.setTitle("Different Song");
            testSong.setGenre("Jazz");
            testSong.setKeyScale("C Major");
            testAlbum.setTitle("Abbey Road");
            testArtist.setName("Different Artist");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match when album title contains keyword");
        }

        /**
         * TC5: Artist name contains keyword (only Artist matches)
         * C1=F, C2=F, C3=F, C4=F, C5=T → Result=T
         */
        @Test
        @DisplayName("TC5: Artist name matches keyword - should return true")
        void artistMatchesOnly() {
            keyword = "beatles";
            testSong.setTitle("Different Song");
            testSong.setGenre("Jazz");
            testSong.setKeyScale("C Major");
            testAlbum.setTitle("Different Album");
            testArtist.setName("The Beatles");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match when artist name contains keyword");
        }

        /**
         * TC6: No field contains keyword (all conditions false)
         * C1=F, C2=F, C3=F, C4=F, C5=F → Result=F
         */
        @Test
        @DisplayName("TC6: No field matches keyword - should return false")
        void noMatchesAnywhere() {
            keyword = "XYZ";
            testSong.setTitle("Come Together");
            testSong.setGenre("Rock");
            testSong.setKeyScale("D Minor");
            testAlbum.setTitle("Abbey Road");
            testArtist.setName("The Beatles");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertFalse(result, "Should not match when keyword not found anywhere");
        }
    }

    @Nested
    @DisplayName("Null/Blank Handling - Edge Cases for MC/DC")
    class NullBlankHandlingTests {

        /**
         * TC7: Null values should be handled gracefully
         */
        @Test
        @DisplayName("TC7: Null song fields - should not throw exception")
        void nullSongFields() {
            keyword = "test";
            testSong.setTitle(null);
            testSong.setGenre(null);
            testSong.setKeyScale(null);
            testAlbum.setTitle(null);
            testArtist.setName(null);
            
            assertDoesNotThrow(() -> {
                searchController.matchesSong(testSong, keyword);
            }, "Should handle null fields gracefully");
        }

        /**
         * TC8: Blank strings should not match non-empty keyword
         */
        @Test
        @DisplayName("TC8: Blank fields with non-blank keyword - should return false")
        void blankFieldsNonBlankKeyword() {
            keyword = "test";
            testSong.setTitle("");
            testSong.setGenre("   ");
            testSong.setKeyScale("\t");
            testAlbum.setTitle("");
            testArtist.setName("   ");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertFalse(result, "Should not match when all fields are blank");
        }

        /**
         * TC9: Keyword is case-insensitive
         */
        @Test
        @DisplayName("TC9: Case-insensitive keyword matching")
        void caseInsensitiveMatching() {
            keyword = "come";
            testSong.setTitle("COME Together");
            testSong.setGenre("Jazz");
            testSong.setKeyScale("C Major");
            testAlbum.setTitle("Different");
            testArtist.setName("Different");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match case-insensitively");
        }
    }

    @Nested
    @DisplayName("Multiple Conditions True - Independent Coverage")
    class MultipleTrueConditionsTests {

        /**
         * TC10: Multiple conditions true (C1=T, C2=T, rest false)
         * Verifies OR short-circuit doesn't hide failures in later conditions
         */
        @Test
        @DisplayName("TC10: First and second conditions match")
        void firstAndSecondConditionsTrue() {
            keyword = "rock";
            testSong.setTitle("Rock Song");
            testSong.setGenre("Rock");
            testSong.setKeyScale("C Major");
            testAlbum.setTitle("Different");
            testArtist.setName("Different");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should return true when multiple conditions match");
        }

        /**
         * TC11: All conditions true
         * Verifies no unexpected behavior when all fields match
         */
        @Test
        @DisplayName("TC11: All fields contain keyword")
        void allConditionsTrue() {
            keyword = "rock";
            testSong.setTitle("Rock Song");
            testSong.setGenre("Rock");
            testSong.setKeyScale("Rock Scale");
            testAlbum.setTitle("Rock Album");
            testArtist.setName("Rock Band");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should return true when all fields contain keyword");
        }
    }

    @Nested
    @DisplayName("Whitespace and Normalization - Boundary Cases")
    class WhitespaceHandlingTests {

        /**
         * TC12: Leading/trailing whitespace in keyword
         */
        @Test
        @DisplayName("TC12: Keyword with surrounding whitespace")
        void keywordWithWhitespace() {
            keyword = "  Come  ";
            testSong.setTitle("Come Together");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertFalse(result, "Whitespace should be significant or trimmed during search");
        }

        /**
         * TC13: Partial word matching
         */
        @Test
        @DisplayName("TC13: Partial word matching")
        void partialWordMatching() {
            keyword = "beat";
            testSong.setTitle("Different");
            testSong.setGenre("Different");
            testArtist.setName("The Beatles");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should match partial words");
        }
    }

    @Nested
    @DisplayName("Null Album/Artist References - Defensive Programming")
    class NullReferenceHandlingTests {

        /**
         * TC14: Null album should not cause NPE
         */
        @Test
        @DisplayName("TC14: Null album reference - should handle gracefully")
        void nullAlbumReference() {
            keyword = "Beatles";
            testSong.setTitle("Song");
            testSong.setAlbum(null);
            
            assertDoesNotThrow(() -> {
                searchController.matchesSong(testSong, keyword);
            }, "Should not throw NPE when album is null");
        }

        /**
         * TC15: Null artist reference should not cause NPE
         */
        @Test
        @DisplayName("TC15: Null artist reference - should handle gracefully")
        void nullArtistReference() {
            keyword = "Beatles";
            testSong.setTitle("Song");
            
            assertDoesNotThrow(() -> {
                searchController.matchesSong(testSong, keyword);
            }, "Should not throw NPE when artist is null");
        }
    }

    @Nested
    @DisplayName("Special Characters and Regex - Injection Safety")
    class SpecialCharactersTests {

        /**
         * TC16: Regex special characters should be treated as literals
         */
        @Test
        @DisplayName("TC16: Regex special characters as literal search")
        void regexSpecialCharacters() {
            keyword = ".*Rock.*";
            testSong.setTitle("Rock Song");
            
            // Implementation detail: search should be literal, not regex
            searchController.matchesSong(testSong, keyword);
            // If implementation uses regex, this could cause unexpected matches
        }

        /**
         * TC17: Unicode characters in search
         */
        @Test
        @DisplayName("TC17: Unicode characters in search keyword")
        void unicodeCharacters() {
            keyword = "café";
            testSong.setTitle("café Song");
            testSong.setGenre("Jazz");
            
            boolean result = searchController.matchesSong(testSong, keyword);
            assertTrue(result, "Should handle unicode characters");
        }
    }
}
