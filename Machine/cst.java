import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Thread;

// CPU of the SAP 2 _ cst
public class cst {
    public static String[] mnemonics = {"ADD B","MOV B,A","ADD C","MOV B,C","ANA B","MOV C,A","ANA C","MOV C,B","ANI byte","MVI A,byte","CALL address","MVI B,byte","CMA","MVI C,byte","DCR A","NOP","DCR B","ORA B","DCR C","ORA C","HLT","ORI byte","IN byte","OUT byte","INR A","RAL","INR B","RAR","INR C","RET","JM address","STA address","JMP address","SUB B","JNZ address","SUB C","JZ address","XRA B","LDA address","XRA C","MOV A,B","XRI byte","MOV A,C"};
    public static String[] codes = {"80","47","81","41","A0","4F","A1","48","E6","3E","CD","06","2F","0E","3D","00","05","B0","0D","BI","76","F6","DB","D3","3C","17","04","1F","0C","C9","FA","32","C3","90","C2","91","CA","A8","3A","A9","78","EE","79"};
    public static String[] mnemonicsReserved = mnemonics.clone();
    public static ArrayList<String> Memory = new ArrayList<String>();

    public static String A = "0", B = "0", C = "0";
    public static int PC = 0;
    public static int flagSignal;
    public static int flagZero;
    public static long time=0;
    public static String[] outs = {"0","0","0"};

    public static boolean stepMnemonic = true,clearTerminal_when_stepInEachMnemonic = false;

    // errors 8

    public static void errors(int error,String saida){
        System.out.printf("\nCST ERROR %03d : %s\n",error,saida);
        System.exit(-1);
    }

    public static String hex(int number){
        String ret = Integer.toString(number,16).toUpperCase();
        if (ret.length()==1) return "0"+ret;
        return ret;
    }
    public static String hex(long number){
        String ret = Integer.toString((int) number,16).toUpperCase();
        if (ret.length()==1) return "0"+ret;
        return ret;
    }
    public static String hex(String number){
        return "";
    }

    public static int integer(String number){
        return Integer.decode("0x"+number);
    }

    public static void main(String[] args){
        System.out.println("\n__CST__");
        time = System.currentTimeMillis();
        setConfigurations();
        // Preset
        for (int i=0;i<mnemonics.length;i++){
            mnemonics[i] = mnemonics[i].replace(" ","");
        }

        try {
            if (args[0].substring(args[0].length()-4).equals(".txt")){
                File from  = new File(args[0]);
                Scanner read = null;

                int init=0;

                try {
                    read = new Scanner(from);
                } catch (Exception e){
                    errors(3,"Can't open "+args[0]+"\nCheck if this file exists and has the extension '.txt'");
                }

                ArrayList<String> codingLines = new ArrayList<String>();

                System.out.println("Take from : "+args[0]);

                while (read.hasNextLine()){
                    codingLines.add(read.nextLine());
                }
                for (int i=0;i<integer("FFFF");i++){
                    Memory.add("00");
                }

                // Read the Memory
                for (int i=0;i<codingLines.size();i++){
                    String[] entry = codingLines.get(i).toUpperCase().split(" ");
                    entry[0] = entry[0].replace("H", "");
                    entry[1] = entry[1].replace("H", "");
                    if (i==0) init = integer(entry[0]);
                    Memory.set(init+i,entry[1]);
                }
                PC = init;
                // Execute
                while (PC<Memory.size()){
                    int j = 0;
                    for (String code : codes){
                        if (code.equals(Memory.get(PC))) break;
                        j++;
                    }
                    

                    if (PC+2<Memory.size()) PC = PC + execute(Memory.get(PC),Memory.get(PC+1),Memory.get(PC+2));
                    else if (PC+1<Memory.size()) PC = PC + execute(Memory.get(PC),Memory.get(PC+1),"_NONE_");
                    else PC = PC + execute(Memory.get(PC),"_NONE_","_NONE_");
                    
                    if (stepMnemonic){
                        if (clearTerminal_when_stepInEachMnemonic) clearTerminal();
                        System.out.printf("%s\n%sH %sH %sH \n",mnemonicsReserved[j],hex(A),hex(B),hex(C));
                        sleep(0.005f);
                    }

                    PC++;

                }

                read.close();
            }else{
                errors(2,"This format of input is not available");
            }
        } catch (Exception e){
            if ((e+"").equals("java.lang.NumberFormatException: Sign character in wrong position")){
                errors(8,"Intern Stack Overflow");
            }else{
                System.out.println("Something went wrong, call the developer\n__ERROR not defined__\n\n"+e);
                return;
            }
        }
        System.out.printf("Read all of the memory\nA : %sH (%d)\nB : %sH (%d)\nC : %sH (%d)\nOUT 03H : %sH (%d)\nOUT 04H : %sH (%d)\n",hex(A),A,hex(B),B,hex(C),C,hex(outs[0]),outs[0],hex(outs[1]),outs[1]);
        System.out.println("Executed in " + (float) (System.currentTimeMillis()-time)/1000 + " seconds");
    }
    // Ececution

    public static void clearTerminal(){
        for (int i=0;i<15;i++){
            System.out.println("\n");
        }
        sleep(0.1666f);
    }

    public static void sleep(float timeSleep){
        try { Thread.sleep((int) (timeSleep*1000)); } catch (Exception e) {}
    }

    public static int execute(String entry, String first, String second){
        
        switch (entry){
            case "80": // ADD B
                A = sum(A,B);
                setFlags("A");
                break;
            case "47": // MOV B,A
                B = A;
                break;
            case "81": // ADD C
                A = sum(A,C);
                setFlags("A");
                break;
            case "41": // MOV B,C
                B = C;
                break;
            // case "A0": // ANA B
            //     break;
            case "4F": // MOV C,A
                C = A;
                break;
            // case "A1": // ANA C
            //     setFlags("A");
            //     break;
            case "48": // MOV C,B
                C = B;
                break;
            // case "E6": // ANI byte
            //     break;
            case "3E": // MVI A,byte
                A = h2b(first);
                return 1;
            case "CD": // CALL address
                String address = hex(PC);
                Memory.set(integer("FFFE")-1,address.substring(2));
                Memory.set(integer("FFFF")-1,address.substring(0,2));
                return -1+integer(second+first)-PC;
            case "06": // MVI B,byte
                B = h2b(first);return 1;
            // case "2F": // CMA
            //     break;
            case "0E": // MVI C,byte
                C = h2b(first);return 1;
            case "3D": // DCR A
                A = sum(A,);
                setFlags("A");
                break;
            case "00": // NOP
                break;
            case "05": // DCR B
                B = sum(B,);
                setFlags("B");
                break;
            // case "BO": // ORA B
            //     break;
            case "0D": // DCR C
                C = sum(C,);
                setFlags("C");
                break;
            // case "BI": // ORA C
            //     break;
            case "76": // HLT
                finalizeIt();
                break;
            // case "F6": // ORI byte
            //     break;
            // case "DB": // IN byte
            //     break;
            case "D3": // OUT byte
                if (integer(first) == 3) outs[0] = A;
                else if (integer(first) == 4) outs[1] = A;
                return 1;
            case "3C": // INR A
                A++;
                setFlags("A");
                break;
            // case "17": // RAL
            //     break;
            case "04": // INR B
                B++;
                setFlags("B");
                break;
            // case "1F": // RAR
            //     break;
            case "OC": // INR C
                C++;
                setFlags("C");
                break;
            case "C9": // RET
                int positionSubroutineNow = integer(""+Memory.get(integer("FFFF")-1)+Memory.get(integer("FFFE")-1));
                return positionSubroutineNow-PC+2;
            case "FA": // JM address   
                if (flagSignal == 1) return -1+(integer(second+first)-PC);
                return 2;
            case "32": // STA address
                Memory.set(integer(second+first),hex(A)); return 2;
            case "C3": // JMP address
                return -1+(integer(second+first)-PC);
            case "90": // SUB B
                A -= B;
                setFlags("A");
                break;
            case "C2": // JNZ address
                if (flagZero == 0) return -1+integer(second+first)-PC;
                return 2;
            case "91": // SUB C
                A -= C;
                setFlags("A");
                break;
            case "CA": // JZ address
                if (flagZero == 1) return -1+(integer(second+first)-PC);
                return 2;
            // case "A8": // XRA B
            //     break;
            case "3A": // LDA address
                A = h2b(Memory.get(integer(second+first)));return 2;
            // case "A9": // XRA C
            //     break;
            case "78": // MOV A,B
                A = B;
                break;
            // case "EE": // XRI byte
            //     break;
            case "79": // MOV A,C
                A = C;
                break;
            default:
                errors(1,"Code not identified or implemented : '"+entry+"' line "+hex(PC)+"H");
        }
        return 0;
    }

    public static void finalizeIt(){
        System.out.printf("System halted\nA : %sH (%d)\nB : %sH (%d)\nC : %sH (%d)\nOUT 03H : %sH (%d)\nOUT 04H : %sH (%d)\n",hex(A),A,hex(B),B,hex(C),C,hex(outs[0]),outs[0],hex(outs[1]),outs[1]);
        System.out.println("Executed in " + (float) (System.currentTimeMillis()-time)/1000 + " seconds");
        System.exit(0);
    }

    public static void setFlags(String acumulator){
        switch (acumulator){
            case "A":
                flagSignal = 1;
                flagZero = 1;
                if (b2d(A).toCharArray()[0] != '-') flagSignal = 0;
                if (b2d(A) != "0") flagZero = 0;
                break;
            case "B":
                flagSignal = 1;
                flagZero = 1;
                if (b2d(B).toCharArray()[0] != '-') flagSignal = 0;
                if (b2d(B) != "0") flagZero = 0;
                break;
            case "C":
                flagSignal = 1;
                flagZero = 1;
                if (b2d(C).toCharArray()[0] != '-') flagSignal = 0;
                if (b2d(C) != "0") flagZero = 0;
                break;
        }
    }

    public static void setConfigurations(){
        File config = new File("config.txt");
        Scanner read = null;
        try {
            read = new Scanner(config);
        } catch (Exception e){
            errors(4, "Can't open 'config.txt'\nCheck if this file exists and has the extension '.txt'");
        }
        ArrayList<String> lines = new ArrayList<String>();
        while (read.hasNextLine()){
            lines.add(read.nextLine());
        }
        
        for (String line : lines){
            if (!(line.split(" ")[0].equals("CST"))) continue;
            String configurationStr = line.split(" ")[1];
            String valueStr = "";
            try{
                valueStr = line.split(" ")[2];
            } catch (Exception e){
                errors(5,"Occured a problem when reading a configuration (value): '"+line+"'");
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
                    errors(6,"Occured a problem identifying a configuration (value): '"+line+"'");
            }
            switch (configurationStr){
                case "stepInEachMnemonic":
                    stepMnemonic = value;
                    break;
                case "clearTerminal_when_stepInEachMnemonic":
                    clearTerminal_when_stepInEachMnemonic = value;
                    break;
                default:
                    errors(7,"Occured a problem when reading a configuration (configuration): '"+line+"'");

            }
        }

    }
    // Numberis
    public static String b2h(String a){
        char[] parts = "....".toCharArray();
        String[] hexs = "0123456789ABCDEF".split("");
        int j=0;
        String result = "",got="";
        for (int i=a.length()-1;i>=0;i--){
            parts[3-j] = a.toCharArray()[i];
            if (j==3){
                int n = Integer.parseInt(b2d("0"+toString(parts)));
                //System.out.println(n);
                result = hexs[n]+result; 
                j=0;
            }else j++;
        }
        if (j != 0){
            got = a.substring(1,j+1);
            int n = Integer.parseInt(b2d("0"+toString(parts)));
            result = hexs[n]+result; 
        }
        return result;
    }

    public static String b2d(String a){
        String result = "0",step1;
        int exp = 0,expNow;
        for (int i=a.length()-1;i>0;i--){
            expNow = exp;
            step1 = ""+a.toCharArray()[i];
            while (expNow > 0){
                step1 = mult(step1,"2");
                expNow--;
            }
            result = sum(result,step1);
            exp++;
        }
        if (a.toCharArray()[0] == '1') result = '-'+result;
        return result;
    }

    public static String h2b(String a){
        String b = "";
        String[] hexs = "0123456789ABCDEF".split("");
        int j=0;
        String result = "",got="";
        for (int i=a.length()-1;i>=0;i--){
            int p=0;
            for (String str : hexs){
                if (str.equals(a.toCharArray()[i]+"")) break;
                p++;
            }
            got = Integer.toString(p,2);
            while (got.length() < 4) got = "0"+got;
            b = got + b;// 
        }
        result = b;
        return result;
    }

    public static String mult(String a,String b){
        String result = "",step2= "";
        String cacheZeros = "", cacheZerosA = "";
        int cache = 0,step1=0;
        //if (min(a.length(),b.length()) == a.length() && a.length() != b.length()) return mult(b,a);
        for (int i=1;i<=b.length();i++){
            step2 = "";
            cacheZerosA = "";
            for (int j=1;j<=a.length();j++){
                step1 = Integer.parseInt(""+b.toCharArray()[b.length()-i])*Integer.parseInt(""+a.toCharArray()[a.length()-j])+cache;
                cache = 0;
                if (step1 >= 10){
                    cache = (step1-step1%10)/10;
                }
                step2 = ""+sum(""+step1%10+cacheZerosA+cacheZeros,step2);

                
                cacheZerosA += "0";
            }
            //System.out.println(">"+step2);
            
            result = sum(step2,result);
           // System.out.println(":"+result);
            if (cache != 0) result = sum(cache+cacheZeros+cacheZerosA,result);
            cacheZeros += "0";
        }
        return result;
    }
    
    public static String sum(String a,String b){
        String result = "";
        int cache = 0,step1=0;
        if (min(a.length(),b.length()) == b.length()){
            while (a.length() != b.length()&& a.length() != b.length()){
                b = "0"+b;
            }
        }else return sum(b,a);
        for (int i=1;i<=min(a.length(),b.length());i++){
            step1 = Integer.parseInt(""+a.toCharArray()[a.length()-i])+Integer.parseInt(""+b.toCharArray()[b.length()-i])+cache;
            cache = 0;
            if (step1 >= 10){
                cache = (step1-step1%10)/10;
            }
            result = ""+step1%10 +result;
        }
        if (cache != 0) result = cache+result;
        return result;
    }
    public static int min(int a,int b){
        if (a > b) return b;
        return a;
    }
    public static String toString(char[] chars){
        String word = "";
        for ( char c : chars){
            word = word + c;
        }
        return word;
    }

}
// By SGL
// 24/05/2024
