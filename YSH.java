
// JAVA CLASSES 
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;


import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

// CLASS 
public class YSH {

    // FILE SYSTEM 
    static class FileNode {

        String name;
        String content = "";

        FileNode(String name) {

            this.name = name;
        }
    }

    // FOLDER SYSTEM 
    static class Folder {

        String name;
        Folder parent;
        ArrayList<Folder> children = new ArrayList<>();
        ArrayList<FileNode> files = new ArrayList<>();

        Folder(String name, Folder parent) {

            this.name = name;
            this.parent = parent;
        }
    }

    // ========== VARIABLES ========== 

    // ROOT 
    static Path currentPath = Paths.get(System.getProperty("user.home"));
    static final Path homePath = Paths.get(System.getProperty("user.home"));
    static Path copiedPath = null;

    // CALC 
    static boolean waitingForCalc = false;

    // DEV ROOT INDI 
    static boolean developerMode = false;

    // NOTE 
    static boolean waitingForNoteAction = false;
    static boolean waitingForNoteTitle = false;
    static boolean waitingForNoteText = false;
    static boolean waitingForNoteView = false;

    static String noteAction = "";
    static String tempTitle = "";

    static ArrayList<String> noteTitles = new ArrayList<>();
    static ArrayList<String> noteTexts = new ArrayList<>();

    // GUI 
    static JTextArea terminal;

    // YSH v8 TERMINAL 
    static int promptPosition = 0;
    static String currentPrompt = "YSH /> ";
    static ArrayList<String> commandHistory = new ArrayList<>();
    static int historyIndex = -1;

    static boolean clearingTerminal = false;

    // KONAMI CODE 
    static int[] konami = {

        java.awt.event.KeyEvent.VK_UP,
        java.awt.event.KeyEvent.VK_UP,
        java.awt.event.KeyEvent.VK_DOWN,
        java.awt.event.KeyEvent.VK_DOWN,
        java.awt.event.KeyEvent.VK_LEFT,
        java.awt.event.KeyEvent.VK_RIGHT,
        java.awt.event.KeyEvent.VK_LEFT,
        java.awt.event.KeyEvent.VK_RIGHT,
        java.awt.event.KeyEvent.VK_B,
        java.awt.event.KeyEvent.VK_A

    };
    static int konamiIndex = 0;

    // THEME 
    static Color bgColor = Color.WHITE;
    static Color textColor = Color.BLACK;
    static Color inputColor = Color.BLACK;

    // CHAT SYSTEM 
    static ServerSocket serverSocket;
    static Socket socket;
    static PrintWriter out;
    static BufferedReader in;

    static boolean isChatRunning = false;
    static boolean isHost = false;

    // FILE SHARE ( LAN ) 
    static ServerSocket fileServerSocket;
    static boolean isFileServerRunning = false;

    // USERNAME 
    static String username = "Guest";

    // MULTI CLIENT SUPPORT 
    static ArrayList<ClientHandler> clients = new ArrayList<>();

    // NOTES SYSTEM 
    static ArrayList<String> notes = new ArrayList<>();

    // CLIENT HANDLER 
    static class ClientHandler extends Thread {

        Socket socket;
        BufferedReader in;
        PrintWriter out;
        String username = "Guest";

        ClientHandler(Socket socket) {

            this.socket = socket;

            try {
                in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );

                out = new PrintWriter(
                    socket.getOutputStream(),
                    true
                );

            } 
            
            catch (Exception e) {

                // BOARD @%25 NUMBER !$# 143 @ 143 

                terminal.append("client error\n");
            }
        }

        @Override
        public void run() {

            try {

                // first message = username
                username = in.readLine();

                terminal.append(username + " joined chat\n");

                broadcast("[SYSTEM] " + username + " joined");

                String msg;

                while ((msg = in.readLine())!= null) {

                    terminal.append(
                        "[" + username + "]: " + msg + "\n"
                    );

                    broadcast(
                        "[" + username + "]: " + msg
                    );
                }

            } 
            
            catch (Exception e) {

                terminal.append(username + " disconnected\n");

            } 
            
            finally {

                clients.remove(this);

                broadcast(
                    "[SYSTEM] " + username + " left"
                );

                try {

                    socket.close();
                } 
                
                catch (Exception ignored) {}
            }
        }

        void send(String msg) {

            out.println(msg);
        }
    }

    public static void main(String[] args) {

        // WINDOW FRAME 
        JFrame frame = new JFrame("YSH Terminal v8");
        // • removed 
        frame.setSize(760, 470);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // INPUT DIALOG 
        terminal = new JTextArea();
        terminal.setEditable(true);
        terminal.setLineWrap(false);
        terminal.setFont(new Font("Consolas", Font.PLAIN, 15));
        terminal.setBackground(Color.BLACK);
        terminal.setForeground(new Color(0, 255, 70));
        terminal.setCaretColor(new Color(0, 255, 70));
        terminal.setBorder(
            BorderFactory.createEmptyBorder(0, 2, 0, 2)
        );
        terminal.setMargin(new Insets(0, 0, 0, 0));
        terminal.setFocusable(true);

        ((javax.swing.text.AbstractDocument) terminal.getDocument())
        .setDocumentFilter(new TerminalFilter());
        
        // SCROLL BAR 
        JScrollPane scroll = new JScrollPane(terminal);
        JScrollBar bar = scroll.getVerticalScrollBar();

        bar.setPreferredSize(new Dimension(8, 0));

        bar.setBackground(bgColor);

        UIManager.put("ScrollBar.width", 8);

        scroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        DefaultCaret caret = (DefaultCaret)
        terminal.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // TERMINAL SHORTCUT SYSTEM
        terminal.addKeyListener(new java.awt.event.KeyAdapter() {
        
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
            
                // KONAMI CODE
                if (e.getKeyCode() == konami[konamiIndex]) {
                
                    konamiIndex++;
                
                    if (konamiIndex == konami.length) {
                    
                        terminal.append("""

                                ______________________________
                        
                                        ACHIEVEMENT
                                     Developer Instincts                        
                                ______________________________
                                """);
                            
                        developerMode = true;
                            
                        devmode();
                            
                        konamiIndex = 0;
                            
                        printPrompt();
                            
                        e.consume();
                        return;
                    }
                
                } 
                
                else {
                
                    konamiIndex = 0;
                }

                int key = e.getKeyCode();
            
                // CTRL + UP / DOWN = SCROLL
                if (e.isControlDown()) {
                
                    JScrollBar sb = scroll.getVerticalScrollBar();
                    int amount = 60;
                
                    if (key == java.awt.event.KeyEvent.VK_UP) {
                    
                        sb.setValue(sb.getValue() - amount);
                        e.consume();
                        return;
                    }
                
                    if (key == java.awt.event.KeyEvent.VK_DOWN) {
                    
                        sb.setValue(sb.getValue() + amount);
                        e.consume();
                        return;
                    }
                }
            
                // ENTER = EXECUTE
                if (key == java.awt.event.KeyEvent.VK_ENTER) {
                
                    e.consume();
                
                    executeCurrentCommand();
                    return;
                }
            
                // UP = PREVIOUS COMMAND
                if (key == java.awt.event.KeyEvent.VK_UP) {
                
                    historyUp();
                    e.consume();
                    return;
                }
            
                // DOWN = NEXT COMMAND
                if (key == java.awt.event.KeyEvent.VK_DOWN) {
                
                    historyDown();
                    e.consume();
                    return;
                }
            
                // TAB = COMMAND COMPLETION
                if (key == java.awt.event.KeyEvent.VK_TAB) {
                
                    e.consume();
                    completeCommand();
                    return;
                }
            
                // CTRL + L = CLEAR
                if (e.isControlDown() &&
                    key == java.awt.event.KeyEvent.VK_L) {
                    
                    e.consume();
                    
                    terminal.setText("");
                    printBanner();
                    printPrompt();
                    
                    return;
                }
            
                // CTRL + C = CANCEL CURRENT INPUT
                if (e.isControlDown() &&
                    key == java.awt.event.KeyEvent.VK_C) {
                    
                    e.consume();
                    
                    terminal.append("^C\n");
                    printPrompt();
                    
                    return;
                }
            
                // HOME = START OF COMMAND
                if (key == java.awt.event.KeyEvent.VK_HOME) {
                
                    terminal.setCaretPosition(promptPosition);
                    e.consume();
                    return;
                }
            
                // END = END OF COMMAND
                if (key == java.awt.event.KeyEvent.VK_END) {
                
                    terminal.setCaretPosition(
                        terminal.getDocument().getLength()
                    );
                
                    e.consume();
                }
            }
        });

        applyTheme();
        timeGreeting();
        printBanner();
        printPrompt();
                
        frame.add(scroll, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // EXECUTE CURRENT TERMINAL INPUT
    static void executeCurrentCommand() {

        try {

            int length =
                terminal.getDocument().getLength() - promptPosition;

            if (length < 0) {
                return;
            }

            String command =
                terminal.getText(promptPosition, length).trim();

            terminal.append("\n");

            if (!command.isEmpty()) {

                commandHistory.add(command);
                historyIndex = commandHistory.size();

                execute(command);
            }

            printPrompt();

        } catch (Exception e) {

            terminal.append("YSH: input error\n");
            printPrompt();
        }
    }

    // HISTORY UP
    static void historyUp() {
    
        if (commandHistory.isEmpty()) {
            return;
        }
    
        if (historyIndex > 0) {
            historyIndex--;
        }
    
        replaceCurrentInput(
            commandHistory.get(historyIndex)
        );
    }

    // HISTORY DOWN
    static void historyDown() {
    
        if (commandHistory.isEmpty()) {
            return;
        }
    
        if (historyIndex < commandHistory.size() - 1) {
        
            historyIndex++;
        
            replaceCurrentInput(
                commandHistory.get(historyIndex)
            );
        
        } else {
        
            historyIndex = commandHistory.size();
            replaceCurrentInput("");
        }
    }

    // REPLACE CURRENT COMMAND
    static void replaceCurrentInput(String text) {
    
        try {
        
            terminal.getDocument().remove(
                promptPosition,
                terminal.getDocument().getLength() - promptPosition
            );
        
            terminal.getDocument().insertString(
                promptPosition,
                text,
                null
            );
        
            terminal.setCaretPosition(
                terminal.getDocument().getLength()
            );
        
        } catch (Exception e) {
        
            terminal.append("YSH: input error\n");
        }
    }

    // COMMAND COMPLETION
    static void completeCommand() {
    
        String input;
    
        try {
        
            int length =
                terminal.getDocument().getLength() - promptPosition;
        
            input =
                terminal.getText(promptPosition, length).trim();
        
        } catch (Exception e) {
        
            return;
        }
    
        if (input.isEmpty()) {
            return;
        }
    
        String[] commands = {
        
            "help",
            "mkdir",
            "touch",
            "rm",
            "rename",
            "copy",
            "paste",
            "move",
            "ls",
            "search",
            "cat",
            "write",
            "cd",
            "pwd",
            "home",
            "calc",
            "echo",
            "clear",
            "whatsup",
            "sysinfo",
            "theme",
            "open",
            "run",
            "check",
            "developer",
            "note",
            "chat",
            "scan",
            "msg",
            "username",
            "listusers",
            "kick",
            "announce",
            "fileshare",
            "sudo",
            "whoami",
            "whocreatedyou",
            "easteregg",
            "exit"
        };
    
        ArrayList<String> matches = new ArrayList<>();
    
        for (String command : commands) {
        
            if (command.startsWith(input)) {
                matches.add(command);
            }
        }
    
        if (matches.size() == 1) {
        
            replaceCurrentInput(matches.get(0));
        
        } else if (matches.size() > 1) {
        
            terminal.append("\n");
        
            for (String match : matches) {
                terminal.append(match + "    ");
            }
        
            terminal.append("\n");
        
            printPrompt();
            replaceCurrentInput(input);
        }
    }

    // TERMINAL INPUT PROTECTION 
    static class TerminalFilter extends DocumentFilter {

        @Override
        public void insertString(
                FilterBypass fb,
                int offset,
                String string,
                AttributeSet attr
        ) throws BadLocationException {

            if (offset >= promptPosition) {
                fb.insertString(offset, string, attr);
            }
        }

        @Override
        public void remove(
                FilterBypass fb,
                int offset,
                int length
        ) throws BadLocationException {

            if (clearingTerminal || offset >= promptPosition) {

                int allowedLength =
                        Math.min(length, fb.getDocument().getLength() - promptPosition);

                if (allowedLength > 0) {
                    fb.remove(offset, allowedLength);
                }
            }
        }

        @Override
        public void replace(
                FilterBypass fb,
                int offset,
                int length,
                String text,
                AttributeSet attrs
        ) throws BadLocationException {

            if (clearingTerminal || offset >= promptPosition) {

                int allowedLength =
                        Math.min(length, fb.getDocument().getLength() - promptPosition);

                fb.replace(offset, allowedLength, text, attrs);
            }
        }
    }

    // EXECUTOR
    static void execute(String inputText) {

        if (inputText == null || inputText.trim().isEmpty()) return;

        String command;
        String args;

        inputText = inputText.trim();

        if (inputText.contains(" ")) {

            command = inputText.substring(0, inputText.indexOf(" "));
            args = inputText.substring(inputText.indexOf(" ") + 1);
        } 
        
        else {

            command = inputText;
            args = "";
        }

        // INLINE CALC + WAITING FOR EXPRESSION
        if (waitingForCalc) {
            
            try {

                String exp = inputText.trim();
                exp = exp.replace("×", "*").replace("÷", "/");
                double result = evalSimple(exp);
                terminal.append("= " + result + "\n");
            } 
            
            catch (Exception e) {

                terminal.append("Invalid expression\n");
            }

            waitingForCalc = false;
            return;
        }

        // NOTE SYSTEM
        if (waitingForNoteAction) {
            
            noteAction = inputText.toLowerCase();
            switch (noteAction) {
                
                case "add":
                    terminal.append("Title: ");
                    waitingForNoteTitle = true;
                    break;

                case "list":
                    for (int i = 0; i < noteTitles.size(); i++) {

                        terminal.append((i + 1) + ". " + noteTitles.get(i) + "\n");
                    }
                    break;

                case "view":
                    terminal.append("Note number: ");
                    waitingForNoteView = true;
                    break;

                default:
                    terminal.append("Unknown note command.\n");
            }

            waitingForNoteAction = false;
            return;
        }

        // NOTE TITLE
        if (waitingForNoteTitle) {
 
            tempTitle = inputText;
            terminal.append("Text: ");
            waitingForNoteTitle = false;
            waitingForNoteText = true;
            return;
        }

        // NOTE TEXT ( content ) 
        if (waitingForNoteText) {

            noteTitles.add(tempTitle);
            noteTexts.add(inputText);
            terminal.append("Saved.\n");
            waitingForNoteText = false;
            return;
        }

        // NOTE VIEW 
        if (waitingForNoteView) {

            try {

                int index = Integer.parseInt(inputText) - 1;

                if (index >= 0 && index < noteTitles.size()) {

                    terminal.append("[" + noteTitles.get(index) + "]\n");
                    terminal.append(noteTexts.get(index) + "\n");
                } 
                
                else {

                    terminal.append("Invalid note.\n");
                }
            } 
            
            catch (Exception e) {

                terminal.append("Invalid note number.\n");
            }

            waitingForNoteView = false;
            // 505 - Arctic Monkeys 

            return;
        }

        // *COMMAND INPUT SYSTEM*
        switch (command) {

            // HELP 
            case "help":
                help();
                break;

            // MAKE FOLDER 
            case "mkdir":
                mkdir(args);
                break;

            // CREATE FILE 
            case "touch":
                touch(args);
                break;
                
            // OMAR (E) 
            case "omarkhaa":
                omar();
                break;

            // DELETE 
            case "rm":
                delete(args);
                break;

            // RENAME 
            case "rename":
                rename(args);
                break;

            // COPY 
            case "copy":
                copy(args);
                break;

            // PASTE 
            case "paste":
                paste();
                break;

            // MOVE 
            case "move":
                move(args);
                break;

            // LIST 
            case "ls":
                ls();
                break;

            // SEARCH 
            case "search":
                search(args);
                break;

            // READ FILE 
            case "cat":
                cat(args);
                break;

            // WRITE FILE 
            case "write":
                write(args);
                break;

            // CHANGE DIRECTORY 
            case "cd":
                cd(args);
                break;

            // PATH 
            case "pwd":
                terminal.append(currentPath.toAbsolutePath() + "\n");
                break;

            // HOME 
            case "home":
                currentPath = homePath;
                terminal.append(currentPath.toAbsolutePath() + "\n");
                break;

            // 7743 (E) 
            case "7743":
                seven();
                break;

            // YSH (E) 
            case "YSH":
                ysh();
                break;

            // BRUH (E) 
            case"bruh":
                bruh();
                break;

            // APPOCALYPSE (E) 
            case "appocalypse":
                appocalypse();
                break;

            // THANKS (E) 
            case "thanks":
                thanks();
                break;

            // CALCULATOR - CLI 
            case "calc":
                terminal.append("Expression: ");
                waitingForCalc = true;
                break;

            // ECHO 
            case "echo":
                terminal.append(args + "\n");
                break;

            // CLEAR 
            case "clear":
            case "cls":
                clearTerminal();
                break;

            // DATE AND TIME 
            case "whatsup":
                terminal.append(new Date().toString()+ "\n");
                break;

            // SYSTEM INFORMATION 
            case "sysinfo":
                sysinfo();
                break;

            // THEME 
            case "theme":
                setTheme(args);
                break;

            // CHECK 
            case "check":
                check();
                break;

            // DEVELOPER MODE
            case "developer":
                developerMode = !developerMode;

                if(developerMode) {
                    devmode();
                }
                else {
                    terminal.append("Developer Mode disabled.\n");
                }
                break;

            // OPEN / RUN APP 
            case "open":
            case "run":
                openApp(args);
                break;

            // EXIT 
            case "exit":
                System.exit(0);
                break;

            // NOTE - CLI 
            case "note":
                terminal.append("Commands: add, list, view\n");
                waitingForNoteAction = true;
                break;

            // TANVEER (E) 
            case "tanveer":
            case "tanvir":
                tanveer();
                break;

            // IBRAHIM (E) 
            case "ibrahim":
                ibrahim();
                break;

            // MUQTADIR (E) 
            case "muqtadir":
                muqtadir();;
                break;

            // MANZIL (E) 
            case "manzil":
                manzil();
                break;

            // CHAT 
            case "chat":
                chatCommand(args);
                break;

                // SCAN CHATROOMS 
            case "scan":
               scanLAN();
               break;

            // MESSAGE 
            case "msg":
                sendMsg(args);
                break;

            // USERNAME 
            case "username":
                setUsername(args);
                break;

            // LIST USERS 
            case "listusers":
                listUsers();
                break;

            // KICK USER 
            case "kick":
                kickUser(args);
                break;

            // ANNOUNCEMENT 
            case "announce":
                announce(args);
                break;

            // FILE SHARE 
            case "fileshare":
                fileShareCommand(args);
                break;

            // SUDO (E) 
            case "sudo":
                sudo();
                break;

            // WHOAMI (E) 
            case "whoami":
                whoami();
                break;

            // CREATOR (E) 
            case "whocreatedyou":
                creator();
                break;

            // EASTER EGG AHH (E) 
            case "easteregg":
                easter();
                break;

            default:
                terminal.append( command + " is not recognized as an System or LAN command.\n"); 
        }
    }

    // DELETE FILE OR DIRECTORY FUNCTION 
    static void delete(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: rm name\n");
            return;
        }

        try {

            Path target = currentPath.resolve(name).normalize();

            if (!Files.exists(target)) {
                terminal.append("rm: not found\n");
                return;
            }

            Files.delete(target);

            terminal.append(
                Files.isDirectory(target)
                    ? "folder deleted\n"
                    : "file deleted\n"
            );

        } catch (DirectoryNotEmptyException e) {

            terminal.append("rm: directory not empty\n");

        } catch (IOException e) {

            terminal.append("rm: " + e.getMessage() + "\n");
        }
    }

    // MAKE DIRECTORY FUNCTION
    static void mkdir(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: mkdir name\n");
            return;
        }

        try {

            Path directory = currentPath.resolve(name).normalize();

            if (Files.exists(directory)) {
                terminal.append("mkdir: already exists\n");
                return;
            }

            Files.createDirectory(directory);

            terminal.append("folder created\n");

        } catch (IOException e) {

            terminal.append("mkdir: " + e.getMessage() + "\n");
        }
    }

    // SYSTEM INFORMATION FUNCTION 
    static void sysinfo() {

        terminal.append("   \n");
        terminal.append("OS: "+ System.getProperty("os.name")+ "\n");
        terminal.append("Version: "+ System.getProperty("os.version")+ "\n");
        terminal.append("User: "+ System.getProperty("user.name")+ "\n");
        terminal.append("CPU cores: "+ Runtime.getRuntime().availableProcessors()+ "\n");
        terminal.append("Total Memory: "+ Runtime.getRuntime().totalMemory()/1024/1024+ "Mb\n");
        terminal.append("Max Memory: "+ Runtime.getRuntime().maxMemory()/1024/1024+ "Mb\n");
        terminal.append("Free Memory: "+ Runtime.getRuntime().freeMemory()/1024/1024+ "Mb\n");
        terminal.append(" or better type "+" open System Information");
        terminal.append(" || better type "+" run System Information");
        terminal.append("   \n");

    }

    // HELP FUNCTION 
    static void help() {

        terminal.append("""
===================== YSH HELP =================================

FILE COMMANDS--
mkdir "name"                    -> create a folder
touch "name"                    -> create a file
rm "name"                       -> delete a file or folder
rename "oldname" "newname"      -> rename a file or folder
copy "name"                     -> copy a file or folder
paste                           -> paste copied item
move "folder/file"              -> move file/folder
ls                              -> list contents
search "name"                   -> search files/folders

MODE-- 
developer                       -> Developer mode 
up up down down < > < > B A     -> Konami Developer Code 
KDE Plasma (for Arch users)     -> ||~ (COMMING SOON) ~||

FILE CONTENT--  
cat "file"                      -> read file contents
write "file" "text"             -> write text to file

NAVIGATION--    
cd "folder"                     -> enter folder
cd ..                           -> go back
pwd                             -> show current path
home                            -> go to root

SYSTEM--    
calc                            -> to perform calculations
echo "text"                     -> print text
clear                           -> clear terminal
whatsup                         -> date and time
sysinfo                         -> system information
theme "name"                    -> change theme
open "app"                      -> launch app
run "app"                       -> launch app
check                           -> YSH check
help                            -> show commands
exit                            -> close YSH
easteregg                       -> helping msg 

NETWORK / CHAT--    
chat host                       -> start chat server
chat join "ip"                  -> join LAN chat
msg "text"                      -> send message

FILE SHARE--    
fileshare host                  -> start file server on port 6001
fileshare get "ip" "filename"   -> get text file from host

NOTES-- 
note add                        -> add notes
note list                       -> list notes
note view                       -> view the saved notes according
                                  to their serial in note list

THEMES--
matrix | blue | purple | red | dark | light

================================================================
""");

    }

    // 7743 FUNCTION 
    static void seven() {

        terminal.append("""
                    Found an Easter Egg there are more.
                    Learn Java.
                    github.com/YousufProjs-exe
                """);

    }

    // APPOCALYPSE FUNCTION 
    static void appocalypse() {

        terminal.append("""
                    No internet?
                    No problem
                    YSH LAN is built for this.
                """);

    }

    // THNAKS FUNCTION 
    static void thanks() {

        terminal.append("""
                    Thanks for using YSH.
                    Report bugs on github.
                    github.com/YousufProjs-exe
                    and its my pleasure.
                """);

    }

    // TANVEER FUNCTION 
    static void tanveer() {

        terminal.append("""
                    Tanveer D. Luffy
                    watch MHA & Your Name buddy.
                    Lol!! MHA won the anime of the year
                """);

    } 

    // IBRAHIM FUNCTION 
    static void ibrahim() {

        terminal.append("""
                    Ibrahim M. Zoro
                    watch A Silent Voice buddy.
                    ~ Dabi, Toya Todoroki
                """);

    }

    // MUQTADIR FUNCTION 
    static void muqtadir() {

        terminal.append("""
                    Muqtadir Jiraiya
                    watch MHA & Another World buddy.
                    rynosuke
                """);

    }

    // MANZIL FUNCTION 
    static void manzil() {

        terminal.append("""
                    Faxeel PVP Master
                    and.... idk
                """);

    }

    // OMAR FUNCTION 
    static void omar() {

        terminal.append("""
                    OMAR KHAAAA 
                    PRO MC and idk...
                """);

    }

    // SUDO FUNCTION 
    static void sudo() {

        terminal.append("""
                    Administrator privilages denied.
                    Nice try Linux user 
                    Reason: 
                    You are not him.
                """);

    }

    // WHOAMI FUNCTION 
    static void whoami() {

        terminal.append("""
                    Guest / Bot / maybe a NPC
                """);

    }

    // WHOCREATEDYOU FUNCTION 
    static void creator() {

        terminal.append("""
                    Built by:
                    Khaja Yousuf Uddin || Saad 
                    ~ yousuf.env 
                    ~ YousufProjs-exe 
                """);

    }

    // EASTER EGG FUNCTION 
    static void easter() {

        terminal.append("""
                    Life aint this easy bruh..
                    Hidden archive found.
                    "THE SHELL REMEMBERS".
                """);

    }

    // YSH FUNCTION
    static void ysh() {

        terminal.append("""
                huh try something better...
                something like: "7743" , "appocalypse" & "easteregg"
                """);
    }

    // BRUH FUNCTION 
    static void bruh() {

        terminal.append("""
                wdym??! 
                better try "help"
                """);
    }

    // CHECK FUNCTION 
    static void check() {

        try {

            terminal.append("Booting YSH");
            Thread.sleep(500);

            terminal.append(".");
            Thread.sleep(600);

            terminal.append("..");
            Thread.sleep(700);

            terminal.append("...\n");

            terminal.append("[OK] Filesystem\n");
            terminal.append("[OK] Calculator\n");
            terminal.append("[OK] Notes\n");
            terminal.append("[OK] LAN Chat\n");
            terminal.append("[OK] File Share\n\n");

        }

        catch (InterruptedException e) {

            terminal.append("[ERROR 7743] Contact Yousuf");
        }
    }

    // CLEAR TERMINAL
    static void clearTerminal() {

        javax.swing.text.AbstractDocument doc =
            (javax.swing.text.AbstractDocument) terminal.getDocument();

        javax.swing.text.DocumentFilter oldFilter =
            doc.getDocumentFilter();

        doc.setDocumentFilter(null);

        terminal.setText("");

        doc.setDocumentFilter(oldFilter);

        promptPosition = 0;

        printBanner();
    }

    // DEVMODE FUNCTION
    static void devmode() {

        terminal.append("""

 __     __  _______  __     __
  __   __   ||       ||     ||
   __ __    ||       ||     ||
    __      ||_____  ||_____||
    __           ||  ||     ||
    __           ||  ||     ||
    __      _____||  __     __


______________________________

      YSH TERMINAL v7 
      Developer Edition 
______________________________

Status : Ready
JDK    : 17
Mode   : GUI DEV EDITION

Type 'help' or continue anyways.
""");

}

    // CREATE FILE FUNCTION
    static void touch(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: touch name\n");
            return;
        }

        try {

            Path file = currentPath.resolve(name).normalize();

            if (Files.exists(file)) {
                terminal.append("touch: file already exists\n");
                return;
            }

            Files.createFile(file);

            terminal.append("file created\n");

        } catch (IOException e) {

            terminal.append("touch: " + e.getMessage() + "\n");
        }
    }

    // CD FUNCTION 
    static void cd(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: cd directory\n");
            return;
        }

        try {

            Path target;

            if (name.equals("..")) {

                target = currentPath.getParent();

                if (target == null) {
                    return;
                }

            } else if (name.equals("~") || name.equals("/")) {

                target = homePath;

            } else {

                target = currentPath.resolve(name);
            }

            target = target.normalize();

            if (!Files.exists(target)) {
                terminal.append("cd: no such file or directory\n");
                return;
            }

            if (!Files.isDirectory(target)) {
                terminal.append("cd: not a directory\n");
                return;
            }

            currentPath = target;

        } catch (Exception e) {

            terminal.append("cd: " + e.getMessage() + "\n");
        }
    }

    // LIST FILESYSTEM
    static void ls() {

        try {

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {

                for (Path path : stream) {

                    if (Files.isDirectory(path)) {
                        terminal.append("[DIR]  " + path.getFileName() + "\n");
                    } else {
                        terminal.append("[FILE] " + path.getFileName() + "\n");
                    }
                }
            }

        } catch (IOException e) {

            terminal.append("ls: " + e.getMessage() + "\n");
        }
    }

    // READ FUNCTION 
    static void cat(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: cat file\n");
            return;
        }

        try {

            Path file = currentPath.resolve(name).normalize();

            if (!Files.exists(file)) {
                terminal.append("cat: file not found\n");
                return;
            }

            if (!Files.isRegularFile(file)) {
                terminal.append("cat: not a file\n");
                return;
            }

            String content = Files.readString(file);

            terminal.append(content);

            if (!content.endsWith("\n")) {
                terminal.append("\n");
            }

        } catch (IOException e) {

            terminal.append("cat: " + e.getMessage() + "\n");
        }
    }

    // WRITE FILE FUNCTION 
    static void write(String args) {

        if (args.isEmpty()) {
            terminal.append("usage: write file text\n");
            return;
        }

        String[] p = args.split(" ", 2);

        if (p.length < 2) {
            terminal.append("usage: write file text\n");
            return;
        }

        try {

            Path file = currentPath.resolve(p[0]).normalize();

            if (!Files.exists(file)) {
                terminal.append("write: file not found\n");
                return;
            }

            if (!Files.isRegularFile(file)) {
                terminal.append("write: not a file\n");
                return;
            }

            Files.writeString(
                file,
                p[1],
                StandardOpenOption.TRUNCATE_EXISTING
            );

            terminal.append("written\n");

        } catch (IOException e) {

            terminal.append("write: " + e.getMessage() + "\n");
        }
    }

    // RENAME FILE OR DIRECTORY FUNCTION 
    static void rename(String args) {

        String[] p = args.split(" ", 2);

        if (p.length < 2) {
            terminal.append("usage: rename oldname newname\n");
            return;
        }

        try {

            Path oldPath = currentPath.resolve(p[0]).normalize();
            Path newPath = currentPath.resolve(p[1]).normalize();

            if (!Files.exists(oldPath)) {
                terminal.append("rename: not found\n");
                return;
            }

            if (Files.exists(newPath)) {
                terminal.append("rename: destination already exists\n");
                return;
            }

            Files.move(oldPath, newPath);

            terminal.append("renamed\n");

        } catch (IOException e) {

            terminal.append("rename: " + e.getMessage() + "\n");
        }
    }

    // COPY FILE OR DIRECTORY FUNCTION 
    static void copy(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: copy name\n");
            return;
        }

        Path source = currentPath.resolve(name).normalize();

        if (!Files.exists(source)) {
            terminal.append("copy: not found\n");
            return;
        }

        copiedPath = source;

        terminal.append(
            Files.isDirectory(source)
                ? "folder copied\n"
                : "file copied\n"
        );
    }

    // PASTE FILE OR DIRECTORY FUNCTION 
    static void paste() {

        if (copiedPath == null) {
            terminal.append("nothing copied\n");
            return;
        }

        try {

            if (!Files.exists(copiedPath)) {
                terminal.append("paste: source no longer exists\n");
                copiedPath = null;
                return;
            }

            Path destination =
                    currentPath.resolve(copiedPath.getFileName().toString() + "_copy");

            if (Files.exists(destination)) {
                terminal.append("paste: destination already exists\n");
                return;
            }

            if (Files.isDirectory(copiedPath)) {

                Files.createDirectory(destination);

            } else {

                Files.copy(copiedPath, destination);
            }

            terminal.append("pasted\n");

        } catch (IOException e) {

            terminal.append("paste: " + e.getMessage() + "\n");
        }
    }

    // MOVE FILE OR DIRECTORY FUNCTION 
    static void move(String args) {

        String[] p = args.split(" ", 2);

        if (p.length < 2) {
            terminal.append("usage: move item destination\n");
            return;
        }

        try {

            Path source = currentPath.resolve(p[0]).normalize();
            Path destination = currentPath.resolve(p[1]).normalize();

            if (!Files.exists(source)) {
                terminal.append("move: source not found\n");
                return;
            }

            if (!Files.exists(destination)) {
                terminal.append("move: destination not found\n");
                return;
            }

            if (!Files.isDirectory(destination)) {
                terminal.append("move: destination is not a directory\n");
                return;
            }

            Path target =
                    destination.resolve(source.getFileName().toString());

            if (Files.exists(target)) {
                terminal.append("move: destination already exists\n");
                return;
            }

            Files.move(source, target);

            terminal.append("moved\n");

        } catch (IOException e) {

            terminal.append("move: " + e.getMessage() + "\n");
        }
    }

    // SEARCH FILESYSTEM
    static void search(String keyword) {

        if (keyword.isEmpty()) {
            terminal.append("usage: search name\n");
            return;
        }

        final String target = keyword.toLowerCase();
        boolean found = false;

        try {

            try (var stream = Files.walk(currentPath)) {

                for (Path path : (Iterable<Path>) stream::iterator) {

                    if (path.equals(currentPath)) {
                        continue;
                    }

                    String name = path.getFileName().toString();

                    if (name.toLowerCase().contains(target)) {

                        if (Files.isDirectory(path)) {
                            terminal.append("[DIR]  " + path + "\n");
                        } else {
                            terminal.append("[FILE] " + path + "\n");
                        }

                        found = true;
                    }
                }
            }

        } catch (IOException e) {

            terminal.append("search: " + e.getMessage() + "\n");
            return;
        }

        if (!found) {
            terminal.append("nothing found\n");
        }
    }

    // LAUNCH APP SYSTEM 
    static void openApp(String app) {

        try {

            switch (app.toLowerCase()) {

                case "notepad":
                    Runtime.getRuntime().exec("cmd /c start notepad");
                    break;

                case "calc":
                    Runtime.getRuntime().exec("cmd /c start calc");
                    break;

                case "chrome":
                    Runtime.getRuntime().exec("cmd /c start chrome");
                    break;

                case "System Information":
                    Runtime.getRuntime().exec("cmd /c start System Information");
                    break;

                default:
                    Runtime.getRuntime().exec("cmd /c start " + app);
            }

            terminal.append("app launched\n");
        } 
        
        catch (Exception e) {

            terminal.append("failed to launch\n");
        }
    }

    // CHAT SYSTEM 
    static void chatCommand(String arg) {

        try {

            if (arg.equals("host")) {

                serverSocket = new ServerSocket(6000);
                isChatRunning = true;
                isHost = true;
                terminal.append("chat server started on port 6000\n");

                new Thread(() -> {

                    while (true) {

                        try {

                            Socket clientSocket = serverSocket.accept();
                            ClientHandler client = new ClientHandler(clientSocket);
                            clients.add(client);
                            client.start();
                        } 
                        
                        catch (Exception e) {

                            break;
                        }
                    }

                }).start();
            }
            
            else if (arg.startsWith("join")) {
                // BOARD @%25 NUMBER !$# 143 @ 1430 

                String[] p = arg.split(" ");

                if (p.length < 2) {

                    terminal.append("usage: chat join ip\n");
                    return;
                }

                String ip = p[1];

                socket = new Socket(ip, 6000);

                in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );

                out = new PrintWriter(
                    socket.getOutputStream(),
                    true
                );

                isChatRunning = true;

                out.println(username);

                new Thread(() -> {

                    try {
                        String msg;

                        while ((msg = in.readLine())!= null) {

                            terminal.append(msg + "\n");
                        }
                    } 
                    
                    catch (Exception e) {

                        terminal.append("Disconnected\n");
                    }

                }).start();

                terminal.append("connected to " + ip + "\n");
            }

        } 
        
        catch (Exception e) {

            terminal.append("Connection Failed\n");
        }
    }

    // MESSAGE SYSTEM 
    static void sendMsg(String msg) {

        if (!isChatRunning) {

            terminal.append("chat not running\n");
            return;
        }

        if (!isHost && out!= null) {

            out.println(msg);
        }

        else if (isHost) {

            terminal.append("[YOU]: " + msg + "\n");
            broadcast("[" + username + "]: " + msg);

            // 505 at 1505 

        }
    }

    // CLIENT SYSTEM 
    static void broadcast(String msg) {

        for (ClientHandler client : clients) {

            client.send(msg);
        }
    }

    // USERNAME SYSTEM 
    static void setUsername(String name) {

        if (name.isEmpty()) {

            terminal.append("usage: username name\n");
            return;
        }

        username = name;
        terminal.append("username set to " + username + "\n");

        if (isChatRunning && out!= null) {

            out.println(username);
        }
    }

    // FILE TRANSFER SERVER - HOST
    static void isFileServer() {

        if (isFileServerRunning)
            return;
        isFileServerRunning = true;

        new Thread(() -> {

            try {

                fileServerSocket = new ServerSocket(6001);
                terminal.append("File server started on port 6001\n");
                while(true) {

                    Socket fs = fileServerSocket.accept();
                    new Thread(() -> handleFileTransfer(fs)).start();

                }
            }

            catch (Exception e) {

                terminal.append("File server error\n");
            }

        }).start();
    }

    // FILE TRANSFER HANDLER
    static void handleFileTransfer(Socket fs) {

        try {

            BufferedReader fin = new BufferedReader(new InputStreamReader(fs.getInputStream()));
            PrintWriter fout = new PrintWriter(fs.getOutputStream(), true);

            String request = fin.readLine();

            if (request != null && request.startsWith("GET ")) {

                String filename = request.substring(4);
                Path file = currentPath.resolve(filename).normalize();

                if (Files.exists(file) && Files.isRegularFile(file)) {
            
                    fout.println("OK");
                    fout.println(Files.readString(file));
                    
                    terminal.append(
                        "Sent file: " + filename + "\n"
                    );
            
                } 
                    
                else {  

                    fout.println("NOTFOUND");
                }
    
                fs.close();
                return;
            }
        }

        catch (Exception e) {
            terminal.append("File transfer error\n");
        }
    }

    // FILE SHARE SYSTEM 
    static void fileShareCommand(String args) {

        String[] p = args.split(" ");

        if (p.length == 0) {

            terminal.append("usage: fileshare host | fileshare get ip filename\n");
            return;
        }

        if (p[0].equals("host")) {

            isFileServer();
        }

        else if (p[0].equals("get")) {

            if (p.length < 3) {

                terminal.append("usage: fileshare get ip filename\n");
                return;
            }

            String ip = p[1];
            String filename = p[2];

            new Thread(() -> {

                try {

                    Socket s = new Socket(ip, 6001);
                    PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream())); 
                    // longest line 
                    
                    pw.println("GET " + filename);
                    String status = br.readLine();

                    if ("OK".equals(status)) {

                        String content = br.readLine();

                        Path file = currentPath.resolve(filename).normalize();

                        Files.writeString(
                            file,
                            content,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                        );
                    
                        terminal.append(
                            "File received: " + filename + "\n"
                        );
                    }
                    
                    else {

                        terminal.append("File not found on host\n");
                    }

                    s.close();
                } 
                
                catch (Exception e) {

                    terminal.append("Failed to get file\n");
                }

            }).start();
        }
    }

    // LIST USERS FUNCTION 
    static void listUsers() {

        if (!isHost) {

            terminal.append("host only command\n");
            return;
        }

        terminal.append("Connected Users:\n");

        for (ClientHandler c : clients) {

            terminal.append(c.username + "\n");
        }
    }

    // KICK USER SYSTEM 
    static void kickUser(String name) {

        if (!isHost) {

            terminal.append("host only command\n");
            return;
        }

        for (ClientHandler c : clients) {

            if (c.username.equals(name)) {

                c.send("[SYSTEM] You were kicked");

                try {

                    c.socket.close();
                } 

                catch (Exception ignored) {}

                clients.remove(c);
                terminal.append(name + " kicked\n");
                return;
            }
        }

        terminal.append("user not found\n");
    }

    // ANNOUNCEMENT FUNCTION 
    static void announce(String msg) {

        if (!isHost) {

            terminal.append("host only command\n");
            return;
        }

        broadcast("[ANNOUNCEMENT]: " + msg);
        terminal.append("announcement sent\n");
    }

    // SCAN CHATROOM SYSTEM 
    static void scanLAN() {

        terminal.append("Scanning network...\n");

        new Thread(() -> {

            try {

                String localIP = InetAddress.getLocalHost().getHostAddress();
                String subnet = localIP.substring(0, localIP.lastIndexOf(".") + 1);

                for (int i = 1; i <= 254; i++) {

                    String host = subnet + i;

                    try {

                        InetAddress address = InetAddress.getByName(host);

                        if (address.isReachable(200)) {

                            SwingUtilities.invokeLater(() ->
                                terminal.append("[ONLINE] " + host + "\n")
                            );
                        }

                    } catch (Exception ignored) {}
                }

                SwingUtilities.invokeLater(() ->
                    terminal.append("Scan complete.\n")
                );

            } 
            catch (Exception e) {

                terminal.append("Scan failed.\n");
            }
            
        }).start();
    }
    
    // *BANNER*
    static void printBanner() {
        
        terminal.append("YSH - Yousuf Shell [Version v8.GUI]\n");
        terminal.append("(c) K.Yousuf 25061-CS-010\n");
        terminal.append("Type 'help' to see available commands\n\n");
        
    }

    // SHELL PROMPT
    static void printPrompt() {

        currentPrompt = developerMode
            ? ">_ "
            : "YSH /> ";

        terminal.append(currentPrompt);

        promptPosition =
            terminal.getDocument().getLength();
    }

    // TIME GREETING 
    static void timeGreeting() {

        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 5) {

            terminal.append("""
                ________________________________

                    Late Night Session on YSH    
                    Respect. 
                    also try "theme dark" 
                ________________________________
                \n """);

        }
    }

    // DEFAULT THEME SYSTEM 
    static void applyTheme() {

        terminal.setBackground(bgColor);
        terminal.setForeground(textColor);
        terminal.setCaretColor(textColor);

    }

    // CUSTOM THEME SYSTEM 
    static void setTheme(String t) {

        switch (t.toLowerCase()) {

            case "matrix":
                bgColor = Color.BLACK;
                textColor = Color.GREEN;
                inputColor = Color.GREEN;
                break;

            case "blue":
                bgColor = Color.DARK_GRAY;
                textColor = Color.CYAN;
                inputColor = Color.CYAN;
                break;

            case "purple":
                bgColor = new Color(30, 0, 50);
                textColor = new Color(200, 120, 255);
                inputColor = new Color(200, 120, 255);
                break;

            case "red":
                bgColor = new Color(50, 0, 0);
                textColor = Color.RED;
                inputColor = Color.RED;
                break;

            case "dark":
            case "black":
                bgColor = Color.BLACK;
                textColor = Color.LIGHT_GRAY;
                inputColor = Color.LIGHT_GRAY;
                break;

            case "light":
            case "default":   
                bgColor = Color.WHITE;
                textColor = Color.BLACK;
                inputColor = Color.BLACK;
                break;

            default:
                terminal.append("themes: matrix | blue | purple | red | dark | light \n");
                return;
        }

        applyTheme();
    }

    // CALC SYSTEM ( Fav Sys ) 
    static double evalSimple(String exp) {

        exp = exp.replaceAll("\\s+", "");

        if (exp.contains("+")) {

            String[] p = exp.split("\\+", 2);
            return Double.parseDouble(p[0]) + Double.parseDouble(p[1]);
        } 
        
        else if (exp.contains("-")) {

            String[] p = exp.split("-", 2);
            return Double.parseDouble(p[0]) - Double.parseDouble(p[1]);
        } 
        
        else if (exp.contains("*") || exp.contains("×")) {

            String[] p = exp.split("\\*|×", 2);
            return Double.parseDouble(p[0]) * Double.parseDouble(p[1]);
        } 
        
        else if (exp.contains("/") || exp.contains("÷")) {

            String[] p = exp.split("/|÷", 2);
            return Double.parseDouble(p[0]) / Double.parseDouble(p[1]);
        }
        
        return Double.parseDouble(exp);
    }
    
// " • "" in output becoming " â€¢ " 
// UTF-8 ( Encoding ) doesnt supports ASCII 
// JDK 17 
// YSH v1-v5 CLI 
// YSH v5 
// YSH v5.GUI 
// YSH v7.GUI 
// YSH v7 
// YSH v8 
// YSH v8 Linux 
// YSH Ecosystem Support and Help 
// Real File System 
// LAN Networking 
//             --  Direct and Group Chats 
//             --  File Share , secured with exact details 
// Secured 
// Actual Shell 
// 50+ commands 
// 2 Achievement 
// Konami Pattern 
// 2 Modes 
// 15+ Easter Eggs 
// 4 In Line Comment Messages 
// Time sensetive message ( 12am - 5am ) 
// Intel Pentinium friendly btw ~ 
// YSH.jar available for your Linux 
// YSH.exe available for your Windows 
// Open Source YSH.java and YSH/ available for you and your other machines 
// Can I get a coffee Please 🙂 

}
