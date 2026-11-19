import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the QuickChat Part 3 features, using the supplied test data. */
class QuickChatTest {

    private static final String SENDER = "Developer";
    private static final String MSG1 = "Did you get the cake?";
    private static final String MSG2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String MSG3 = "Yohoooo, I am at your gate.";
    private static final String MSG4 = "It is dinner time!";
    private static final String MSG5 = "Ok, I am leaving without you.";

    /** Loads test data messages 1-5 before every test. */
    @BeforeEach
    void setUp() {
        QuickChat.clearAllArrays();
        QuickChat.addMessage("1000000001", SENDER, "+27834557896", MSG1, QuickChat.FLAG_SENT);
        QuickChat.addMessage("1000000002", SENDER, "+27838884567", MSG2, QuickChat.FLAG_STORED);
        QuickChat.addMessage("1000000003", SENDER, "+27834484567", MSG3, QuickChat.FLAG_DISREGARD);
        // Test message 4 gives no recipient; its "0838884567" value is used as the message ID.
        QuickChat.addMessage("0838884567", SENDER, "+27831112222", MSG4, QuickChat.FLAG_SENT);
        QuickChat.addMessage("1000000005", SENDER, "+27838884567", MSG5, QuickChat.FLAG_STORED);
    }

    @Test
    void sentMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(MSG1, MSG4), QuickChat.sentMessages);
    }

    @Test
    void disregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(MSG3), QuickChat.disregardedMessages);
    }

    @Test
    void storedMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(MSG2, MSG5), QuickChat.storedMessages);
    }

    @Test
    void displayLongestMessage() {
        assertEquals(MSG2, QuickChat.findLongestStoredMessage());
    }

    @Test
    void searchForMessageID() {
        assertEquals("It is dinner time!", QuickChat.searchByMessageID("0838884567"));
    }

    @Test
    void searchForUnknownMessageID() {
        assertEquals("Message ID not found.", QuickChat.searchByMessageID("0000000000"));
    }

    @Test
    void searchAllMessagesForRecipient() {
        assertEquals(List.of(MSG2, MSG5), QuickChat.searchByRecipient("+27838884567"));
    }

    @Test
    void deleteMessageUsingHash() {
        String hash = QuickChat.messageHashes.get(1); // test message 2
        String expected = "Message: \"" + MSG2 + "\" successfully deleted.";

        assertEquals(expected, QuickChat.deleteByHash(hash));
        assertEquals(List.of(MSG5), QuickChat.storedMessages);
        assertEquals(4, QuickChat.messageHashes.size());
    }

    @Test
    void deleteUnknownHash() {
        assertEquals("Message hash not found.", QuickChat.deleteByHash("00:0:No:Such"));
    }

    @Test
    void displayReportShowsHashRecipientAndMessage() {
        String report = QuickChat.buildReport();

        assertTrue(report.contains("Message Hash: " + QuickChat.messageHashes.get(0)));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: " + MSG1));
        assertFalse(report.contains(MSG3)); // disregarded messages are not reported
    }

    @Test
    void messageHashFormat() {
        assertEquals("10:2:Where:time.", QuickChat.createMessageHash("1000000002", 2, MSG2));
    }

    @Test
    void recipientNumberValidation() {
        assertTrue(QuickChat.CheckRecipientNumber("+27834557896"));
        assertFalse(QuickChat.CheckRecipientNumber("0838884567"));
    }

    @Test
    void storedMessagesSurviveJsonRoundTrip(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("stored.json");
        List<String> hashesBefore = new ArrayList<>(QuickChat.messageHashes);

        QuickChat.saveStoredMessagesToJson(file);
        QuickChat.clearAllArrays();
        QuickChat.loadStoredMessagesFromJson(file);

        assertEquals(List.of(MSG2, MSG5), QuickChat.storedMessages);
        assertEquals(List.of(hashesBefore.get(1), hashesBefore.get(4)), QuickChat.messageHashes);
    }
}
