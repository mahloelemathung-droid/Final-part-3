import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * QuickChat - Parts 1 to 3.
 *
 * The first six ArrayLists below are PARALLEL: index i in each list describes
 * the same message. sentMessages, disregardedMessages and storedMessages hold
 * the message text for each flag.
 */
public class QuickChat {

    public static final String FLAG_SENT = "Sent";
    public static final String FLAG_STORED = "Stored";
    public static final String FLAG_DISREGARD = "Disregard";

    private static final Path JSON_PATH = Paths.get("storedMessages.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- Arrays required by Part 3 ----
    public static final ArrayList<String> sentMessages = new ArrayList<>();
    public static final ArrayList<String> disregardedMessages = new ArrayList<>();
    public static final ArrayList<String> storedMessages = new ArrayList<>();
    public static final ArrayList<String> messageHashes = new ArrayList<>();
    public static final ArrayList<String> messageIDs = new ArrayList<>();

    // ---- Parallel arrays that hold the remaining details of every message ----
    public static final ArrayList<String> messageSenders = new ArrayList<>();
    public static final ArrayList<String> messageRecipients = new ArrayList<>();
    public static final ArrayList<String> messageTexts = new ArrayList<>();
    public static final ArrayList<String> messageFlags = new ArrayList<>();

    private static int totalMessages;
    private static int processedCount = 0;

    // =====================================================================
    // Array handling
    // =====================================================================

    /** Empties every array (used at start-up of the tests). */
    public static void clearAllArrays() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        messageSenders.clear();
        messageRecipients.clear();
        messageTexts.clear();
        messageFlags.clear();
    }

    /** Adds a message and builds its hash. Returns the new hash. */
    public static String addMessage(String id, String sender, String recipient,
                                    String text, String flag) {
        String hash = createMessageHash(id, messageIDs.size() + 1, text);
        addMessage(id, hash, sender, recipient, text, flag);
        return hash;
    }

    /** Adds a message that already has a hash (e.g. one read from JSON). */
    public static void addMessage(String id, String hash, String sender,
                                  String recipient, String text, String flag) {
        messageIDs.add(id);
        messageHashes.add(hash);
        messageSenders.add(sender);
        messageRecipients.add(recipient);
        messageTexts.add(text);
        messageFlags.add(flag);

        if (FLAG_SENT.equals(flag)) {
            sentMessages.add(text);
        } else if (FLAG_STORED.equals(flag)) {
            storedMessages.add(text);
        } else {
            disregardedMessages.add(text);
        }
    }

    // =====================================================================
    // Validation, ID and hash (from Part 2)
    // =====================================================================

    public static boolean CheckRecipientNumber(String number) {
        return number != null && number.matches("\\+27[0-9]{9}");
    }

    public static String generateMessageID() {
        long id = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return String.valueOf(id);
    }

    public static String createMessageHash(String messageID, int messageNumber, String message) {
        String[] words = message.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord = words[words.length - 1];

        return messageID.substring(0, 2)
                + ":" + messageNumber
                + ":" + firstWord
                + ":" + lastWord;
    }

    // =====================================================================
    // Stored Messages features (Part 3, task 2)
    // =====================================================================

    /** (a) Sender and recipient of every stored message. */
    public static String listStoredSendersAndRecipients() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageIDs.size(); i++) {
            if (FLAG_STORED.equals(messageFlags.get(i))) {
                sb.append("Sender: ").append(messageSenders.get(i))
                  .append(" | Recipient: ").append(messageRecipients.get(i)).append("\n");
            }
        }
        return sb.length() == 0 ? "No stored messages." : sb.toString().trim();
    }

    /** (b) Longest stored message. */
    public static String findLongestStoredMessage() {
        String longest = "";
        for (String text : storedMessages) {
            if (text.length() > longest.length()) {
                longest = text;
            }
        }
        return longest.isEmpty() ? "No stored messages." : longest;
    }

    /** Position of a message ID in the parallel arrays, or -1. */
    public static int findIndexByID(String id) {
        return messageIDs.indexOf(id);
    }

    /** (c) Message text for a message ID. */
    public static String searchByMessageID(String id) {
        int index = findIndexByID(id);
        return index == -1 ? "Message ID not found." : messageTexts.get(index);
    }

    /** (d) All sent or stored messages for one recipient. */
    public static ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < messageRecipients.size(); i++) {
            String flag = messageFlags.get(i);
            boolean sentOrStored = FLAG_SENT.equals(flag) || FLAG_STORED.equals(flag);
            if (sentOrStored && messageRecipients.get(i).equals(recipient)) {
                found.add(messageTexts.get(i));
            }
        }
        return found;
    }

    /** (e) Delete a message (from every array) using its hash. */
    public static String deleteByHash(String hash) {
        int index = messageHashes.indexOf(hash);
        if (index == -1) {
            return "Message hash not found.";
        }

        String text = messageTexts.get(index);
        String flag = messageFlags.get(index);

        if (FLAG_SENT.equals(flag)) {
            sentMessages.remove(text);
        } else if (FLAG_STORED.equals(flag)) {
            storedMessages.remove(text);
        } else {
            disregardedMessages.remove(text);
        }

        messageIDs.remove(index);
        messageHashes.remove(index);
        messageSenders.remove(index);
        messageRecipients.remove(index);
        messageTexts.remove(index);
        messageFlags.remove(index);

        return "Message: \"" + text + "\" successfully deleted.";
    }

    /** (f) Report with the full details of all sent and stored messages. */
    public static String buildReport() {
        StringBuilder sb = new StringBuilder("===== Message Report =====\n");
        int count = 0;
        for (int i = 0; i < messageIDs.size(); i++) {
            String flag = messageFlags.get(i);
            if (FLAG_DISREGARD.equals(flag)) {
                continue;
            }
            count++;
            sb.append("\nMessage Hash: ").append(messageHashes.get(i))
              .append("\nMessage ID: ").append(messageIDs.get(i))
              .append("\nSender: ").append(messageSenders.get(i))
              .append("\nRecipient: ").append(messageRecipients.get(i))
              .append("\nMessage: ").append(messageTexts.get(i))
              .append("\nStatus: ").append(flag).append("\n");
        }
        if (count == 0) {
            sb.append("\nNo messages to report.\n");
        }
        return sb.toString();
    }

    // =====================================================================
    // JSON storage
    // Code adapted from the Gson User Guide:
    // https://github.com/google/gson/blob/main/UserGuide.md
    // (replace/add the exact source(s) you used)
    // =====================================================================

    /** Writes every stored message to the JSON file. */
    public static void saveStoredMessagesToJson(Path file) throws IOException {
        List<Message> records = new ArrayList<>();
        for (int i = 0; i < messageIDs.size(); i++) {
            if (FLAG_STORED.equals(messageFlags.get(i))) {
                records.add(new Message(messageIDs.get(i), messageHashes.get(i),
                        messageSenders.get(i), messageRecipients.get(i),
                        messageTexts.get(i), messageFlags.get(i)));
            }
        }
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(records, writer);
        }
    }

    /** Reads the JSON file into the arrays (as Stored messages). */
    public static void loadStoredMessagesFromJson(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        Type listType = new TypeToken<ArrayList<Message>>() { }.getType();
        try (Reader reader = Files.newBufferedReader(file)) {
            List<Message> records = GSON.fromJson(reader, listType);
            if (records == null) {
                return;
            }
            for (Message m : records) {
                addMessage(m.getMessageID(), m.getMessageHash(), m.getSender(),
                        m.getRecipient(), m.getMessageText(), FLAG_STORED);
            }
        }
    }

    private static void saveJsonQuietly() {
        try {
            saveStoredMessagesToJson(JSON_PATH);
        } catch (IOException e) {
            System.out.println("Could not save stored messages: " + e.getMessage());
        }
    }

    // =====================================================================
    // Console menus
    // =====================================================================

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        try {
            loadStoredMessagesFromJson(JSON_PATH);
        } catch (IOException e) {
            System.out.println("Could not read stored messages: " + e.getMessage());
        }

        System.out.println("Welcome to QuickChat.");
        System.out.print("Enter your own number (+27XXXXXXXXX): ");
        String sender = readValidNumber(input);

        System.out.print("How many messages would you like to send? ");
        totalMessages = readNonNegativeInt(input);

        while (true) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("1. Send Messages");
            System.out.println("2. Show Recently Sent Messages");
            System.out.println("3. Quit");
            System.out.println("4. Stored Messages");
            System.out.print("Choose an option: ");

            switch (input.nextLine().trim()) {
                case "1":
                    sendMessages(input, sender);
                    break;
                case "2":
                    showSentMessages();
                    break;
                case "3":
                    System.out.println("Thank you for using QuickChat.");
                    input.close();
                    return;
                case "4":
                    storedMessagesMenu(input);
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    private static String readValidNumber(Scanner input) {
        String number = input.nextLine().trim();
        while (!CheckRecipientNumber(number)) {
            System.out.print("Invalid number. Use +27 followed by 9 digits: ");
            number = input.nextLine().trim();
        }
        return number;
    }

    private static int readNonNegativeInt(Scanner input) {
        while (true) {
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                if (value >= 0) {
                    return value;
                }
                System.out.print("Enter zero or a positive number: ");
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }

    private static void sendMessages(Scanner input, String sender) {
        while (processedCount < totalMessages) {
            System.out.println("\nMessage " + (processedCount + 1) + " of " + totalMessages);

            System.out.print("Enter recipient number: ");
            String recipient = input.nextLine().trim();
            if (!CheckRecipientNumber(recipient)) {
                System.out.println("Invalid recipient number.");
                continue;
            }

            System.out.print("Enter your message: ");
            String message = input.nextLine();
            if (message.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
                continue;
            }
            if (message.length() > 250) {
                System.out.println("Please enter a message of 250 characters or less.");
                continue;
            }

            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");
            String flag = flagFromChoice(input.nextLine().trim());
            if (flag == null) {
                System.out.println("Invalid option, please start this message again.");
                continue;
            }

            String id = generateMessageID();
            String hash = addMessage(id, sender, recipient, message, flag);
            processedCount++;

            if (FLAG_STORED.equals(flag)) {
                saveJsonQuietly();
            }

            System.out.println("\nMessage " + flag.toLowerCase() + ".");
            System.out.println("Message ID: " + id);
            System.out.println("Message Hash: " + hash);
            System.out.println("Messages remaining: " + (totalMessages - processedCount));
        }
        System.out.println("\nAll requested messages have been entered.");
    }

    private static String flagFromChoice(String choice) {
        switch (choice) {
            case "1": return FLAG_SENT;
            case "2": return FLAG_DISREGARD;
            case "3": return FLAG_STORED;
            default: return null;
        }
    }

    private static void showSentMessages() {
        if (sentMessages.isEmpty()) {
            System.out.println("No messages have been sent yet.");
            return;
        }
        for (String text : sentMessages) {
            System.out.println("- " + text);
        }
    }

    private static void storedMessagesMenu(Scanner input) {
        while (true) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("a. Display sender and recipient of all stored messages");
            System.out.println("b. Display the longest stored message");
            System.out.println("c. Search for a message ID");
            System.out.println("d. Search for messages to a recipient");
            System.out.println("e. Delete a message using its hash");
            System.out.println("f. Display report");
            System.out.println("g. Back to main menu");
            System.out.print("Choose an option: ");

            switch (input.nextLine().trim().toLowerCase()) {
                case "a":
                    System.out.println(listStoredSendersAndRecipients());
                    break;
                case "b":
                    System.out.println(findLongestStoredMessage());
                    break;
                case "c":
                    System.out.print("Enter message ID: ");
                    int index = findIndexByID(input.nextLine().trim());
                    if (index == -1) {
                        System.out.println("Message ID not found.");
                    } else {
                        System.out.println("Recipient: " + messageRecipients.get(index));
                        System.out.println("Message: " + messageTexts.get(index));
                    }
                    break;
                case "d":
                    System.out.print("Enter recipient number: ");
                    ArrayList<String> found = searchByRecipient(input.nextLine().trim());
                    if (found.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    }
                    for (String text : found) {
                        System.out.println("- " + text);
                    }
                    
                case "e":
                    System.out.print("Enter message hash: ");
                    System.out.println(deleteByHash(input.nextLine().trim()));
                    saveJsonQuietly();
                    break;
                case "f":
                   System.out.println(buildReport());
                    break;
                case "g":
                    return;
                default:
                    System.out.println("Invalid option. Please choose a to g."); 
            }
        }
    }
}
