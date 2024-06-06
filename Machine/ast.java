import java.io.FileWriter;
import java.io.File;
import java.lang.Exception;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

// Assembler of the SAP 2 _ ast
public class ast{
    public static String[] mnemonics = {"ADD B","MOV B,A","ADD C","MOV B,C","ANA B","MOV C,A","ANA C","MOV C,B","ANI byte","MVI A,byte","CALL address","MVI B,byte","CMA","MVI C,byte","DCR A","NOP","DCR B","ORA B","DCR C","ORA C","HLT","ORI byte","IN byte","OUT byte","INR A","RAL","INR B","RAR","INR C","RET","JM address","STA address","JMP address","SUB B","JNZ address","SUB C","JZ address","XRA B","LDA address","XRA C","MOV A,B","XRI byte","MOV A,C"};
    public static String[] codes = {"80","47","81","41","A0","4F","A1","48","E6","3E","CD","06","2F","0E","3D","00","05","B0","0D","BI","76","F6","DB","D3","3C","17","04","1F","0C","C9","FA","32","C3","90","C2","91","CA","A8","3A","A9","78","EE","79"};
    public static ArrayList<String> mnemonicsLines = new ArrayList<String>();
    public static ArrayList<String> codingLines = new ArrayList<String>();
    public static ArrayList<Pointer> pointers = new ArrayList<Pointer>();
    public static String[] mnemonicsReserved = mnemonics.clone();
    public static int differencePositionMemory = 0;
    public static boolean blockComment = false;
    public static long time=0;
    public static String version = "beta";

    // Errors 13

    public static class Pointer{
        int address;
        String name;
        Pointer(String name, int address){
            this.name = name;
            this.address = address;
        }
    }

    public static class Config{
        public static boolean showMnemonics;
        public static boolean upperAllLetters_when_showMnemonics;
        public static boolean showPointers;
    
        public static void addCoding_byConfig(int positionCode,int positionLine){
            String mnemonicLine = mnemonicsLines.get(positionLine);
            if (!(Config.showPointers)){
                // Remove the attribuitions to the pointers
                String cache = "";
                String sub_cache = "";
                for (char c : mnemonicLine.toCharArray()){
                    if (c == '>') break;
                    if (c == ' '){
                        sub_cache = sub_cache + c;
                        continue;
                    }
                    cache = cache + sub_cache + c;
                    sub_cache = "";
                }
                mnemonicLine = cache;
                // Replace the pointers name to the addresses
                cache = "";
                String remove = "";
                boolean readToAddress = false;
                for (char c : mnemonicLine.toCharArray()){
                    if (readToAddress){
                        cache = cache + c;
                        remove = remove + c;
                    }
                    if (c == '<'){
                        readToAddress = true;
                        remove = remove + c;
                        continue;
                    }
                }
                mnemonicLine = mnemonicLine.replace(remove,"");
                cache = cache.replace(" ","").toUpperCase();
                if (readToAddress){
                    for (Pointer pointer: pointers){
                        if (pointer.name.equals(cache)){
                            cache = ""+hex(pointer.address)+"H";
                        }
                    }
                }
                mnemonicLine += cache;

            }

            if (Config.showMnemonics){
                if (Config.upperAllLetters_when_showMnemonics) codingLines.add(codes[positionCode]+"H "+mnemonicLine.toUpperCase());
                else codingLines.add(codes[positionCode]+"H "+mnemonicLine);
            }
            else codingLines.add(codes[positionCode]+"H");
        }
    }

    public static String hex(int number){
        return Integer.toString(number,16).toUpperCase();
    }

    public static void main(String[] args){
        System.out.println("\n__AST__"+version);
        time = System.currentTimeMillis();
        // Preset
        for (int i=0;i<mnemonics.length;i++){
            mnemonics[i] = mnemonics[i].replace(" ","");
        }

        try {
            if (args[0].substring(args[0].length()-4).equals(".txt")){
                int init=0,positionMemory,position;
                File from = new File(args[0]);
                boolean getNewPointer = true;
                FileWriter to;
                char[] chars;

                if (args.length == 3){ 
                    init = Integer.decode("0x"+args[2]);
                }else if (args.length == 2){
                    init = Integer.decode("0x"+args[1]);
                }else{
                    errors(6, "Provide a correct position for the beggin of the memory ("+args.length+")");
                }
                positionMemory = init;
                try {
                    setConfigurations();
                } catch (Exception e){
                    errors(9,"Occured a problem when reading 'Config.txt'");
                }

                Scanner read = null;
                try {
                    read = new Scanner(from);
                } catch (Exception e){
                    errors(8,"Can't open "+args[0]+"(Addressing)\nCheck if this file exists and has the extension '.txt'");
                }

                System.out.println("Take from : "+args[0]);

                // Addressing
                while (read.hasNextLine()){
                    mnemonicsLines.add(read.nextLine());
                }
                read.close();

                for (int i=0;i<mnemonicsLines.size();i++){
                    chars = mnemonicsLines.get(i).toUpperCase().toCharArray();
                    chars = removeCommentary(chars);
                    if (chars.length > 0)
                        if (chars[0]=='#'){
                            blockComment = !(blockComment);
                            continue;
                        }
                    if (blockComment) continue;
                    if (isNotCommentary(chars)){
                        position = getPositionMnemonic(chars);
                        if (hasAddressSetting(chars)){
                            getNewPointer = true;
                            for (Pointer pointer : pointers) if (pointer.name.equals(getNameToAddress(chars))){
                                pointer.address = positionMemory+differencePositionMemory;
                                getNewPointer = false;
                                break;
                            }
                            if (getNewPointer) pointers.add(new Pointer(getNameToAddress(chars),positionMemory+differencePositionMemory));
                        }
                        Config.addCoding_byConfig(position,i);

                        if (hasAddress(position)){
                            String _address_ = getAddress(chars);
                            if (_address_ == "") _address_ = "NULL";
                            codingLines.add(_address_.substring(2)+"H");positionMemory++;
                            codingLines.add(_address_.substring(0,2)+"H");positionMemory++;
                        }else if (hasByte(position)){
                            String _byte_ = getByte(chars,hasTwoArguments(position));
                            if (_byte_ == "") errors(3,"Byte not catched : "+mnemonicsReserved[position]+" (is not syntactically equals to) "+mnemonicsLines.get(i).toUpperCase());
                            codingLines.add(_byte_+"H");
                            positionMemory++;
                        }
                        positionMemory++;
                    }
                }
                positionMemory = init;
                chars = null;
                codingLines = new ArrayList<String>();

                // Reading
                blockComment = false;
                for (int i=0;i<mnemonicsLines.size();i++){
                    chars = mnemonicsLines.get(i).toCharArray();
                    if (chars.length > 0)
                        if (chars[0]=='#'){
                            blockComment = !(blockComment);
                            continue;
                        }
                    if (blockComment) continue;
                    
                    mnemonicsLines.set(i,toString(removeCommentary(chars)));
                    chars = mnemonicsLines.get(i).toUpperCase().toCharArray();
                    chars = removeCommentary(chars);
                    
                    if (isNotCommentary(chars)){
                        position = getPositionMnemonic(chars);
                        Config.addCoding_byConfig(position,i);

                        if (hasAddress(position)){
                            String _address_ = getAddress(chars);
                            if (_address_ == "") errors(2,"Address not catched : "+mnemonicsReserved[position]+" (is not syntactically equals to) "+mnemonicsLines.get(i).toUpperCase());
                            codingLines.add(_address_.substring(2)+"H");positionMemory++;
                            codingLines.add(_address_.substring(0,2)+"H");positionMemory++;
                        }else if (hasByte(position)){
                            String _byte_ = getByte(chars,hasTwoArguments(position));
                            if (_byte_ == "") errors(3,"Byte not catched : "+mnemonicsReserved[position]+" (is not syntactically equals to) "+mnemonicsLines.get(i).toUpperCase());
                            codingLines.add(_byte_+"H");
                            positionMemory++;
                        }
                        positionMemory++;
                    }
                }

                if (args.length == 3){ // format : orign destiny position
                    to = new FileWriter(args[1]);
                    init = Integer.decode("0x"+args[2]);
                    int i = 0;
                    for (String line : codingLines){
                        to.write(hex(init+i)+"H "+line+"\n");
                        i++;
                    }
                    System.out.println("Put in : "+args[1]);
                    to.close();
                }else if (args.length == 2){ // format : orgign/destiny position
                    init = Integer.decode("0x"+args[1]);
                    to = new FileWriter(args[0]);
                    int i = 0;
                    for (String line : codingLines){
                        to.write(hex(init+i)+"H "+line+"\n");
                        i++;
                    }
                    System.out.println("Put in : "+args[0]);
                    to.close();
                }

            }else{
                errors(4,"This format of input is not available");
            }
        } catch (IOException e){
            System.out.println("Something went wrong, call the developer\n__ERROR not defined__\n\n"+e);
            return;
        }
        
        System.out.println("Compiled in " +  (float) (System.currentTimeMillis()-time)/1000 + " seconds");
    }

    // Machine 2

    public static boolean hasAddressSetting(char[] chars) {
        for (int i=0;i<chars.length;i++){
            if (chars[i] == '>') return true;
        }
        return false;
    }

    public static String getNameToAddress(char[] chars) {
        boolean read=false,breakWhenEncounterSpace=false,canReadNumber=true;
        differencePositionMemory = 0;
        String cache = "";
        for (int i=0;i<chars.length;i++){
            if (isNumber(chars[i]) && read && canReadNumber){
                differencePositionMemory = Integer.parseInt(""+chars[i]);
            }else if (chars[i] != ' ' && read){
                canReadNumber = false;
                breakWhenEncounterSpace = true;
                cache = cache + chars[i];
            }else if (breakWhenEncounterSpace) break;
            if (chars[i] == '>') read = true;
        }
        return cache;
        
    }

    public static String toString(char[] chars){
        String word = "";
        for ( char c : chars){
            word = word + c;
        }
        return word;
    }

    public static boolean isNotCommentary(char[] chars) {
        
        if (chars.length >= 2){
            if (chars[0]==chars[1] && chars[0]=='-'){
                return false;
            }
            return true;
        }else if (toString(chars).equals("")) return false;
        
        errors(5, "Line not identified : '"+toString(chars)+"'");
        return true;
    }

    public static char[] removeCommentary(char[] chars){
        String res = "";
        for (int i =0;i<chars.length;i++){
            if (chars[i] == ';') break;
            res = res + chars[i];
        }
        return res.toCharArray();
    }

    public static boolean hasTwoArguments(int position){
        for (int i=0;i<mnemonics[position].length();i++){
            if (mnemonics[position].toCharArray()[i] == ',') return true;
        }
        return false;
    }

    public static void errors(int error,String saida){
        System.out.printf("\nAST ERROR %03d : %s\n",error,saida);
        System.exit(-1);
    }

    public static boolean hasAddress(int position){
        char[] address = "address".toCharArray();
        int i=0;
        for (char c : mnemonics[position].toCharArray()){
            if (c == address[i]){
                i++;
                if (i == address.length) return true;
            }else i=0;
        }
        return false;
    }

    public static String getAddress(char[] charsStrange){
        String word = "";
        for (char c : charsStrange){
            word = word + c;
        }
        char[] chars = word.toUpperCase().toCharArray();
        boolean toCount = false,breakWhenEncounterSpace = false,read = false,canReadNumber = true;
        differencePositionMemory = 0;
        String cache = "";
        for (int i=0;i<chars.length;i++){
            if (toCount)
                if (isNumber(chars[i]) && canReadNumber){
                    differencePositionMemory = Integer.parseInt(""+chars[i]);
                }else if (chars[i] != ' '){
                    canReadNumber = false;
                    breakWhenEncounterSpace = true;
                    if (chars[i] == '\n') break;
                    cache = cache + chars[i];
                }else if (breakWhenEncounterSpace) break;
                
            if (chars[i] == '<') toCount = true;
        }

        for (Pointer pointer : pointers){
            if (pointer.name.equals(cache)){
                word = "";
                breakWhenEncounterSpace = false;
                for (int i=0;i<chars.length;i++){
                    if (chars[i] != ' ' && read && !(breakWhenEncounterSpace)){
                        breakWhenEncounterSpace = true;
                        word = word + hex(pointer.address+differencePositionMemory)+"H";
                    } else if (breakWhenEncounterSpace) break;
                    if (chars[i] == '<'){
                        read = true;
                        continue;
                    }
                    if (!(breakWhenEncounterSpace)) word = word + chars[i];
                }
                chars = word.toCharArray();
            }
        }
        breakWhenEncounterSpace = false;
        toCount = false;
        cache = "";
        for (int i=0;i<chars.length;i++){
            if (toCount)
                if (chars[i] != ' '){
                    breakWhenEncounterSpace = true;
                    if (chars[i] == 'H') break;
                    cache = cache + chars[i];
                }else if (breakWhenEncounterSpace){
                    cache = "";
                    break;
                }
            if (chars[i] == ' ') toCount = true;
        }
        return cache;
    }

    public static boolean hasByte(int position){
        char[] _byte_ = "byte".toCharArray();
        int i=0;
        for (char c : mnemonics[position].toCharArray()){
            if (c == _byte_[i]){
                i++;
                if (i == _byte_.length) return true;
            }else i=0;
        }
        return false;
    }

    public static String getByte(char[] charsStrange,boolean hasTwoArguments){
        String word = "";
        for (char c : charsStrange){
            word = word + c;
        }
        char[] chars = word.toUpperCase().toCharArray();
        boolean toCount = false,breakWhenEncounterSpace = false;
        String cache = "";
        for (int i=0;i<chars.length;i++){
            //System.out.println(chars[i]);
            if (toCount)
                if (chars[i] != ' '){
                    breakWhenEncounterSpace = true;
                    if (chars[i] == 'H') break;
                    cache = cache + chars[i];
                }else if (breakWhenEncounterSpace){
                    cache = "";
                    break;
                }
            
            if (chars[i] == ',' && hasTwoArguments || chars[i] == ' ' && !(hasTwoArguments)) toCount = true;
        }
        return cache;
    }

    public static String getUntil(String word, int position){
        String cache = "";
        for (int i=0;i<position && i<word.length();i++){
            String confirmWord = ""+word.toCharArray()[i];
            if (confirmWord.toUpperCase().toCharArray()[0] != word.toCharArray()[i] || isNumberOrPointer(word.toCharArray()[i])) break;
            cache = cache + word.toCharArray()[i];
        }
        return cache;
    }

    public static int getPositionMnemonic(char[] chars){
        String cache = "";
        int qt=0,i,position=-1,len=0;
        for (char c : chars){
            if (c == ' ' || c == '\n') continue;
            cache = cache+c;
            len++;qt=0;i=0;
            for (String mnemonic : mnemonics){
                if (getUntil(mnemonic,len).equals(getUntil(cache,len))){
                    position = i;
                    qt++;
                }
                i++;
            }
            //System.out.println(cache+qt);
            if (qt==1) break;
        }
        if (position == -1) errors(1,"Mnemonic not identified\n"+cache+" ???");
        else if (qt != 1) errors(7,"Mnemonic not definied\n"+cache+" is ambiguos");
        return position;
    }

    public static boolean isNumberOrPointer(char c){
        char[] letters = "1234567890<>".toCharArray();
        for (char letter : letters){
            if (c == letter) return true;
        }
        return false;
    }
    
    public static boolean isNumber(char c){
        char[] letters = "1234567890".toCharArray();
        for (char letter : letters){
            if (c == letter) return true;
        }
        return false;
    }

    public static void pseudo_finalizeIt(){
        System.out.println("Encountered a HALT");
    }

    public static void setConfigurations(){
        File config = new File("Config.txt");
        Scanner read = null;
        try {
            read = new Scanner(config);
        } catch (Exception e){
            errors(12, "Can't open 'Config.txt'\nCheck if this file exists and has the extension '.txt'");
        }
        ArrayList<String> lines = new ArrayList<String>();
        while (read.hasNextLine()){
            lines.add(read.nextLine());
        }
        read.close();
        
        for (String line : lines){
            if (!(line.split(" ")[0].equals("AST"))) continue;
            String configurationStr = line.split(" ")[1];
            String valueStr = "";
            try{
                valueStr = line.split(" ")[2];
            } catch (Exception e){
                errors(13,"Occured a problem when reading a configuration (value): '"+line+"'");
            }
            boolean value=false;
            switch (valueStr){
                case "true":
                    value = true;
                    break;
                case "false":
                    value = false;
                    break;
                default:
                    errors(10,"Occured a problem identifying a configuration (value): '"+line+"'");
            }
            switch (configurationStr){
                case "showMnemonics":
                    Config.showMnemonics = value;
                    break;
                case "upperAllLetters_when_showMnemonics":
                    Config.upperAllLetters_when_showMnemonics = value;
                    break;
                case "showPointers":
                    Config.showPointers = value;
                    break;
                default:
                    errors(11,"Occured a problem when reading a configuration (configuration): '"+line+"'");
            }
        }

    }
}   

// Create by SGL
// 23/05/2024