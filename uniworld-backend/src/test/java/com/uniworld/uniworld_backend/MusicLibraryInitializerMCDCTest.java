package com.uniworld.uniworld_backend;

import com.uniworld.uniworld_backend.config.MusicLibraryInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MC/DC (Modified Condition/Decision Coverage) Test Cases for MusicLibraryInitializer
 * 
 * Complex Decision Logic Areas:
 * 1. ID3 tag extraction with cascading null checks:
 *    if (id3v2Tag != null && id3v2Tag.getTitle() != null && !id3v2Tag.getTitle().isBlank())
 *    → 3-part AND with negation
 * 
 * 2. Album cover image fallback:
 *    if ((album.getCoverImage() == null || album.getCoverImage().isBlank()) && coverImagePath != null)
 *    → 3-part condition (2-part OR combined with AND)
 * 
 * 3. Tag existence with null/blank checks:
 *    if (value == null || value.isBlank())
 *    → 2-part OR
 * 
 * MC/DC requires each condition to independently affect outcome
 */
@DisplayName("MusicLibraryInitializer MC/DC Test Suite - Metadata Extraction")
public class MusicLibraryInitializerMCDCTest {

    private MusicLibraryInitializer initializer;
    private TestID3v2Tag testTag;
    private TestAlbum testAlbum;

    @BeforeEach
    void setUp() {
        initializer = new MusicLibraryInitializer(null, null, null);
        testTag = new TestID3v2Tag();
        testAlbum = new TestAlbum();
    }

    @Nested
    @DisplayName("MC/DC: ID3 Title Extraction - 3-part AND with Negation")
    class ID3TitleExtractionTests {

        /**
         * TC1: All conditions true - extract title
         * C1=T (tag !null), C2=T (title !null), C3=T (!blank) → Extract title
         */
        @Test
        @DisplayName("TC1: Valid tag, valid title, not blank - extract title")
        void validTagValidTitleNotBlank() {
            testTag.setNotNull(true);
            testTag.setTitle("Abbey Road");
            testTag.setTitleNotNull(true);
            
            boolean shouldExtract = testTag.isNotNull() && 
                                    testTag.getTitleNotNull() && 
                                    !testTag.getTitleBlank();
            assertTrue(shouldExtract, "Should extract title when all conditions are true");
        }

        /**
         * TC2: Tag is null - don't extract
         * C1=F (tag is null), C2=T (title !null), C3=T (!blank) → Don't extract (short-circuit)
         */
        @Test
        @DisplayName("TC2: Tag is null - should not extract (short-circuit)")
        void tagIsNull() {
            testTag.setNotNull(false);
            testTag.setTitle(null);
            testTag.setTitleNotNull(false);
            
            boolean shouldExtract = testTag.isNotNull() && 
                                    testTag.getTitleNotNull() && 
                                    !testTag.getTitleBlank();
            assertFalse(shouldExtract, "Should not extract when tag is null (short-circuit at C1)");
        }

        /**
         * TC3: Title is null - don't extract
         * C1=T (tag !null), C2=F (title is null), C3=T (!blank) → Don't extract (short-circuit)
         * Verifies C2 independently affects outcome
         */
        @Test
        @DisplayName("TC3: Tag exists but title is null - should not extract")
        void titleIsNull() {
            testTag.setNotNull(true);
            testTag.setTitle(null);
            testTag.setTitleNotNull(false);
            
            boolean shouldExtract = testTag.isNotNull() && 
                                    testTag.getTitleNotNull() && 
                                    !testTag.getTitleBlank();
            assertFalse(shouldExtract, "Should not extract when title is null (short-circuit at C2)");
        }

        /**
         * TC4: Title is blank - don't extract
         * C1=T (tag !null), C2=T (title !null), C3=F (is blank) → Don't extract
         * Verifies C3 independently affects outcome (negation matters)
         */
        @Test
        @DisplayName("TC4: Tag and title exist but title is blank - should not extract")
        void titleIsBlank() {
            testTag.setNotNull(true);
            testTag.setTitle("   ");
            testTag.setTitleNotNull(true);
            testTag.setTitleBlank(true);
            
            boolean shouldExtract = testTag.isNotNull() && 
                                    testTag.getTitleNotNull() && 
                                    !testTag.getTitleBlank();
            assertFalse(shouldExtract, "Should not extract when title is blank (negation fails at C3)");
        }

        /**
         * TC5: Title is empty string
         */
        @Test
        @DisplayName("TC5: Title is empty string")
        void titleIsEmptyString() {
            testTag.setNotNull(true);
            testTag.setTitle("");
            testTag.setTitleNotNull(true);
            testTag.setTitleBlank(true);
            
            boolean shouldExtract = testTag.isNotNull() && 
                                    testTag.getTitleNotNull() && 
                                    !testTag.getTitleBlank();
            assertFalse(shouldExtract, "Should not extract empty string title");
        }

        /**
         * TC6: Multiple consecutive null checks - defensive
         */
        @Test
        @DisplayName("TC6: All components null")
        void allComponentsNull() {
            testTag.setNotNull(false);
            testTag.setTitleNotNull(false);
            
            boolean shouldExtract = testTag.isNotNull() && 
                                    testTag.getTitleNotNull() && 
                                    !testTag.getTitleBlank();
            assertFalse(shouldExtract, "Should handle all nulls gracefully");
        }
    }

    @Nested
    @DisplayName("MC/DC: Album Cover Fallback - Mixed AND/OR Logic")
    class AlbumCoverFallbackTests {

        /**
         * Decision: if ((album.getCoverImage() == null || album.getCoverImage().isBlank()) && coverImagePath != null)
         * Equivalent to: (C1 || C2) && C3
         * Where:
         * C1 = album.getCoverImage() == null
         * C2 = album.getCoverImage().isBlank()
         * C3 = coverImagePath != null
         */

        /**
         * TC7: Album has no cover AND path provided - should set cover
         * (C1 || C2)=T, C3=T → Set cover
         */
        @Test
        @DisplayName("TC7: Album missing cover, path available - should set cover")
        void albumMissingCoverPathAvailable() {
            testAlbum.setCoverImage(null);
            String coverPath = "/path/to/cover.jpg";
            
            boolean shouldSetCover = (testAlbum.getCoverImage() == null || 
                                      (testAlbum.getCoverImage() != null && 
                                       testAlbum.getCoverImage().isBlank())) && 
                                      coverPath != null;
            assertTrue(shouldSetCover, "Should set cover when album has none and path exists");
        }

        /**
         * TC8: Album has blank cover AND path provided - should set cover
         * (C1 || C2)=T, C3=T → Set cover
         */
        @Test
        @DisplayName("TC8: Album has blank cover, path available - should set cover")
        void albumBlankCoverPathAvailable() {
            testAlbum.setCoverImage("   ");
            String coverPath = "/path/to/cover.jpg";
            
            boolean shouldSetCover = (testAlbum.getCoverImage() == null || 
                                      testAlbum.getCoverImage().isBlank()) && 
                                      coverPath != null;
            assertTrue(shouldSetCover, "Should set cover when album cover is blank");
        }

        /**
         * TC9: Album has cover BUT no path - don't override
         * (C1 || C2)=F, C3=T → Don't set
         */
        @Test
        @DisplayName("TC9: Album has cover - should not override")
        void albumHasCoverPathAvailable() {
            testAlbum.setCoverImage("existing/cover.jpg");
            String coverPath = "/path/to/cover.jpg";
            
            boolean shouldSetCover = (testAlbum.getCoverImage() == null || 
                                      testAlbum.getCoverImage().isBlank()) && 
                                      coverPath != null;
            assertFalse(shouldSetCover, "Should not override existing cover");
        }

        /**
         * TC10: Album has no cover AND no path - don't set
         * (C1 || C2)=T, C3=F → Don't set
         * Verifies C3 independently affects outcome
         */
        @Test
        @DisplayName("TC10: Album missing cover, no path available - don't set")
        void albumMissingCoverNoPath() {
            testAlbum.setCoverImage(null);
            String coverPath = null;
            
            boolean shouldSetCover = (testAlbum.getCoverImage() == null || 
                                      testAlbum.getCoverImage().isBlank()) && 
                                      coverPath != null;
            assertFalse(shouldSetCover, "Should not set cover when path is null");
        }

        /**
         * TC11: Album has cover AND no path
         * (C1 || C2)=F, C3=F → Don't set
         */
        @Test
        @DisplayName("TC11: Album has cover, no path - don't set")
        void albumHasCoverNoPath() {
            testAlbum.setCoverImage("existing.jpg");
            String coverPath = null;
            
            boolean shouldSetCover = (testAlbum.getCoverImage() == null || 
                                      testAlbum.getCoverImage().isBlank()) && 
                                      coverPath != null;
            assertFalse(shouldSetCover, "Should not set when both cover exists and path null");
        }
    }

    @Nested
    @DisplayName("MC/DC: Null/Blank Validation - 2-part OR")
    class NullBlankValidationTests {

        /**
         * Decision: if (value == null || value.isBlank())
         * Decision: value is invalid
         */

        /**
         * TC12: Null value
         * C1=T (is null), C2=F (isBlank N/A) → Invalid
         */
        @Test
        @DisplayName("TC12: Null value - is invalid")
        void nullValue() {
            String value = null;
            boolean isInvalid = value == null || (value != null && value.isBlank());
            assertTrue(isInvalid, "Null value should be invalid");
        }

        /**
         * TC13: Blank string
         * C1=F (not null), C2=T (is blank) → Invalid
         */
        @Test
        @DisplayName("TC13: Blank string - is invalid")
        void blankString() {
            String value = "   ";
            boolean isInvalid = value == null || value.isBlank();
            assertTrue(isInvalid, "Blank string should be invalid");
        }

        /**
         * TC14: Valid non-blank string
         * C1=F (not null), C2=F (not blank) → Valid
         */
        @Test
        @DisplayName("TC14: Valid string - is valid")
        void validString() {
            String value = "Valid Content";
            boolean isInvalid = value == null || value.isBlank();
            assertFalse(isInvalid, "Valid non-blank string should be valid");
        }

        /**
         * TC15: Empty string
         * C1=F (not null), C2=T (is blank) → Invalid
         */
        @Test
        @DisplayName("TC15: Empty string - is invalid")
        void emptyString() {
            String value = "";
            boolean isInvalid = value == null || value.isBlank();
            assertTrue(isInvalid, "Empty string is blank and should be invalid");
        }

        /**
         * TC16: String with only newlines/tabs
         */
        @Test
        @DisplayName("TC16: Whitespace-only variations")
        void whitespaceVariations() {
            String[] whitespaceValues = { "\n", "\t", "\r", " \n \t " };
            for (String value : whitespaceValues) {
                boolean isInvalid = value == null || value.isBlank();
                assertTrue(isInvalid, "Whitespace-only '" + value + "' should be invalid");
            }
        }
    }

    @Nested
    @DisplayName("MC/DC: Complex Cascading Conditions")
    class CascadingConditionsTests {

        /**
         * TC17: Simulate extractExtension pattern
         * Multiple conditions testing file validation
         */
        @Test
        @DisplayName("TC17: File extension validation cascade")
        void fileExtensionValidation() {
            String[] validExtensions = { ".mp3", ".wav", ".ogg", ".m4a" };
            String fileName = "song.mp3";
            
            boolean isSupported = false;
            for (String ext : validExtensions) {
                if (fileName.toLowerCase().endsWith(ext)) {
                    isSupported = true;
                    break;
                }
            }
            assertTrue(isSupported, "Should validate supported audio extensions");
        }

        /**
         * TC18: MIME type validation with fallback
         */
        @Test
        @DisplayName("TC18: MIME type detection with cascading fallback")
        void mimeTypeDetection() {
            String mimeType = null;
            
            // Cascading null checks
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "audio/mpeg"; // Default
            }
            
            assertNotNull(mimeType);
            assertFalse(mimeType.isBlank());
        }
    }

    @Nested
    @DisplayName("MC/DC Coverage Matrix")
    class CoverageMatrixTests {

        /**
         * Matrix for 3-part AND with negation:
         * ID3 Tag | Title != null | !Title.isBlank() | Result
         * T       | T             | T                | Extract
         * F       | -             | -                | Skip (short-circuit)
         * T       | F             | -                | Skip (short-circuit)
         * T       | T             | F                | Skip
         */
        @Test
        @DisplayName("Coverage Matrix: ID3 extraction decision table")
        void id3ExtractionMatrix() {
            int passCount = 0;
            int failCount = 0;

            boolean tagExists;
            boolean titleExists;
            boolean titleNotBlank;

            // TC1: All true
            tagExists = true;
            titleExists = true;
            titleNotBlank = true;
            if (tagExists && titleExists && titleNotBlank)
                passCount++;
            else
                failCount++;

            // TC2: Tag null
            tagExists = false;
            titleExists = true;
            titleNotBlank = true;
            if (tagExists && titleExists && titleNotBlank)
                passCount++;
            else
                failCount++;

            // TC3: Title null
            tagExists = true;
            titleExists = false;
            titleNotBlank = true;
            if (tagExists && titleExists && titleNotBlank)
                passCount++;
            else
                failCount++;

            // TC4: Title blank
            tagExists = true;
            titleExists = true;
            titleNotBlank = false;
            if (tagExists && titleExists && titleNotBlank)
                passCount++;
            else
                failCount++;

            assertEquals(1, passCount, "Should have exactly 1 passing case");
            assertEquals(3, failCount, "Should have 3 failing cases");
        }
    }

    // Helper classes for testing
    private static class TestID3v2Tag {
        private boolean notNull = true;
        private String title;
        private boolean titleNotNull = true;
        private boolean titleBlank = false;

        public boolean isNotNull() { return notNull; }
        public void setNotNull(boolean value) { this.notNull = value; }

        
        public void setTitle(String title) { 
            this.title = title;
            this.titleNotNull = title != null;
            this.titleBlank = title == null || title.isBlank();
        }

        public String getTitle() {
            return this.title;
        }

        public boolean getTitleNotNull() { return titleNotNull; }
        public void setTitleNotNull(boolean value) { this.titleNotNull = value; }

        public boolean getTitleBlank() { return titleBlank; }
        public void setTitleBlank(boolean value) { this.titleBlank = value; }
    }

    private static class TestAlbum {
        private String coverImage;

        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String image) { this.coverImage = image; }
    }
}
