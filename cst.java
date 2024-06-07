import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Thread;
import java.io.FileWriter;

// CPU of the SAP 2 _ cst
public class cst {
    public static String[] mnemonics = {"ADD B","MOV B,A","ADD C","MOV B,C","ANA B","MOV C,A","ANA C","MOV C,B","ANI byte","MVI A,byte","CALL address","MVI B,byte","CMA","MVI C,byte","DCR A","NOP","DCR B","ORA B","DCR C","ORA C","HLT","ORI byte","IN byte","OUT byte","INR A","RAL","INR B","RAR","INR C","RET","JM address","STA address","JMP address","SUB B","JNZ address","SUB C","JZ address","XRA B","LDA address","XRA C","MOV A,B","XRI byte","MOV A,C"};
    public static String[] codes = {"80","47","81","41","A0","4F","A1","48","E6","3E","CD","06","2F","0E","3D","00","05","B0","0D","BI","76","F6","DB","D3","3C","17","04","1F","0C","C9","FA","32","C3","90","C2","91","CA","A8","3A","A9","78","EE","79"};
    public static boolean stepMnemonic=false,clearTerminal_when_stepInEachMnemonic=false,delay_when_showAnd_useOutput=false,show_when_useOutput=false,showAllOutputs_when_useOutput=false,noCut_when_stackOverflowHardware=false;
    public static String[] outs = {"00","00"},mnemonicsReserved = mnemonics.clone();
    public static ArrayList<String> Memory = new ArrayList<String>();
    public static String A = "00", B = "00", C = "00";
    public static int PC = 0,flagSignal,flagZero;
    public static long time=0;
    public static String version = "beta";

    // Errors 15

    public static void errors(int error,String saida){
        System.out.printf("\nCST ERROR %03d : %s\n",error,saida);
        System.exit(-1);
    }

    public static String hex(int number){
        String ret = Integer.toString(number,16).toUpperCase();
        if (ret.length()==1) return "0"+ret;
        return ret;
    }
  
    public static int integer(String number){
        return Integer.decode("0x"+number);
    }

    public static void main(String[] args){
        System.out.println("\n__CST__"+version);
        time = System.currentTimeMillis();
        setConfigurations();
        // Preset
        for (int i=0;i<mnemonics.length;i++){
            mnemonics[i] = mnemonics[i].replace(" ","");
        }
        
        try {
            if (args[0].substring(args[0].length()-4).equals(".txt")){
                ArrayList<String> codingLines = new ArrayList<String>();
                File from  = new File(args[0]);
                Scanner read = null;
                String[] entry;
                int init = 0,j;

                try {
                    read = new Scanner(from);
                } catch (Exception e){
                    errors(3,"Can't open "+args[0]+"\nCheck if this file exists and has the extension '.txt'");
                }
                System.out.println("Take from : "+args[0]);

                while (read.hasNextLine()){
                    codingLines.add(read.nextLine());
                }
                read.close();
                for (int i=0;i<integer("FFFF");i++){
                    Memory.add("00");
                }

                // Read the Memory
                for (int i=0;i<codingLines.size();i++){
                    entry = codingLines.get(i).toUpperCase().split(" ");
                    entry[0] = entry[0].replace("H", "");
                    entry[1] = entry[1].replace("H", "");
                    if (i==0) init = integer(entry[0]);
                    Memory.set(init+i,entry[1]);
                }
                PC = init;
                // Execute
                while (PC<Memory.size()){
                    j = 0;
                    for (String code : codes){
                        if (code.equals(Memory.get(PC))) break;
                        j++;
                    }

                    overflowHardware();

                    if (PC+2<Memory.size()) PC = PC + execute(Memory.get(PC),Memory.get(PC+1),Memory.get(PC+2));
                    else if (PC+1<Memory.size()) PC = PC + execute(Memory.get(PC),Memory.get(PC+1),"_NONE_");
                    else PC = PC + execute(Memory.get(PC),"_NONE_","_NONE_");

                    if (stepMnemonic){
                        if (clearTerminal_when_stepInEachMnemonic) clearTerminal();
                        System.out.printf("%s\n%s %s %s \n",mnemonicsReserved[j],b2d(A),b2d(B),b2d(C));
                        sleep(0.1f);
                    }
                    if (show_when_useOutput && mnemonicsReserved[j].equals("OUT byte")){
                        if (showAllOutputs_when_useOutput){
                            System.out.println("OUT 3H : "+b2h(outs[0])+"H ("+b2d(outs[0])+")");
                            System.out.println("OUT 4H : "+b2h(outs[1])+"H ("+b2d(outs[1])+")");
                        }else System.out.println("OUT "+Memory.get(PC)+"H : "+b2h(A)+"H ("+b2d(A)+")");
                        if (delay_when_showAnd_useOutput) sleep(1);
                    }
                    PC++;
                }
            }else{
                errors(2,"This format of input is not available");
            }
        } catch (Exception e){
            System.out.println(h2b(Memory.get(PC)));
            if ((e+"").equals("java.lang.NumberFormatException: Sign character in wrong position")){
                errors(8,"Intern Stack Overflow");
            }else{
                System.out.println("Something went wrong, call the developer\n__ERROR not defined__\n\n"+e);
                return;
            }
        }
        System.out.println("----------------------");
        System.out.printf("Read all of the memory\nA : %sH (%s)\nB : %sH (%s)\nC : %sH (%s)\nOUT 03H : %sH (%s)\n",b2h(A),b2d(A),b2h(B),b2d(B),b2h(C),b2d(C),b2h(outs[0]),b2d(outs[0]));
        System.out.println("Executed in " + (float) (System.currentTimeMillis()-time)/1000 + " seconds");
    }

    // Execution

    public static void clearTerminal(){
        for (int i=0;i<15;i++){
            System.out.println("\n");
        }
        sleep(0.01666f);
    }

    public static void sleep(float timeSleep){
        try { 
            Thread.sleep((int) (timeSleep*1000));
        } catch (Exception e) {
            errors(13,"Occured a problem when using sleep, call the developer");
        }
    }

    public static int execute(String entry, String first, String second){
        String a,b,c,r;

        switch (entry){
            case "80": // ADD B
                A = sumB(A,B);
                setFlags("A");
                break;
            case "47": // MOV B,A
                B = A;
                break;
            case "81": // ADD C
                A = sumB(A,C);
                setFlags("A");
                break;
            case "41": // MOV B,C
                B = C;
                break;
            case "A0": // ANA B
                b=B;a=A;r="";
                while (a.length()<b.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>b.length())b=b.toCharArray()[0]+"0"+b.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]=='1'&&b.toCharArray()[i]=='1')?"1":"0";
                A = r;
                setFlags("A");
                break;
            case "4F": // MOV C,A
                C = A;
                break;
            case "A1": // ANA C
                c=C;a=A;r="";
                while (a.length()<c.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>c.length())c=c.toCharArray()[0]+"0"+c.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]=='1'&&c.toCharArray()[i]=='1')?"1":"0";
                A = r;
                setFlags("A");
                break;
            case "48": // MOV C,B
                C = B;
                break;
            case "E6": // ANI byte
                c=h2b(first);a=A;r="";
                while (a.length()<c.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>c.length())c=c.toCharArray()[0]+"0"+c.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]=='1'&&c.toCharArray()[i]=='1')?"1":"0";
                A = r;
                setFlags("A");
                return 1;
            case "3E": // MVI A,byte
                A = h2b(first);
                return 1;
            case "CD": // CALL address
                String address = hex(PC);
                Memory.set(integer("FFFE")-1,address.substring(2));
                Memory.set(integer("FFFF")-1,address.substring(0,2));
                return -1+integer(second+first)-PC;
            case "06": // MVI B,byte
                B = h2b(first);
                return 1;
            case "2F": // CMA
                a="";
                for (char k : A.toCharArray()){
                    if (k=='1')k='0';
                    else k='1';
                    a+=""+k;
                }
                A=a;
                break;
            case "0E": // MVI C,byte
                C = h2b(first);
                return 1;
            case "3D": // DCR A
                A = subB(A,"01");
                setFlags("A");
                break;
            case "00": // NOP
                break;
            case "05": // DCR B
                B = subB(B,"01");
                setFlags("B");
                break;
            case "B0": // ORA B
                a=A;b=B;r="";
                while (a.length()<b.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>b.length())b=b.toCharArray()[0]+"0"+b.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]=='1'||b.toCharArray()[i]=='1')?"1":"0";
                A = r;
                setFlags("A");
                break;
            case "0D": // DCR C
                C = subB(C,"01");
                setFlags("C");
                break;
            case "BI": // ORA C
                c=C;a=A;r="";
                while (a.length()<c.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>c.length())c=c.toCharArray()[0]+"0"+c.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]=='1'||c.toCharArray()[i]=='1')?"1":"0";
                A = r;
                setFlags("A");
                break;
            case "76": // HLT
                finalizeIt();
                break;
            case "F6": // ORI byte
                c=h2b(first);a=A;r="";
                while (a.length()<c.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>c.length())c=c.toCharArray()[0]+"0"+c.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]=='1'||c.toCharArray()[i]=='1')?"1":"0";
                A = r;
                setFlags("A");
                return 1;
            case "DB": // IN byte
                if (h2b(first).equals("00000001")){
                    Scanner readKeyboard = null;
                    String input = "",number;
                    boolean is_hex = false;
                    FileWriter file;
                    File stream1;
                    System.out.print(":> ");

                    while (true){
                        try {
                            stream1 = new File("Input.txt");
                            readKeyboard = new Scanner(stream1);
                        } catch (Exception e){
                            errors(10,"Can't open Input.txt\nCheck if this file exists and has the extension '.txt'");
                        }
                        if (!(readKeyboard.hasNext())) continue;
                        input = readKeyboard.next();
                        input = input.toUpperCase();
                        for (int i=0;i<input.length();i++) if (input.toCharArray()[i]=='H') is_hex=true;
                        if (is_hex){
                            number = "";
                            for (int i=0;i<input.length();i++){
                                if (input.toCharArray()[i]=='H')break;
                                number = number + input.toCharArray()[i];
                            }
                            A = h2b(number);
                            System.out.println(number+"H");
                        }else{
                            A = d2b(input);
                            System.out.println(input);
                        }
                        break;
                    }
                    readKeyboard.close();
                    try { // Força a limpar Input.txt
                        file = new FileWriter("Input.txt");
                        file.write("");
                        file.flush();
                        file.write("");
                        file.close();
                    }catch (Exception e){
                        errors(11,"Can't open Input.txt\nCheck if this file exists and has the extension '.txt'");
                    }
                }else if (h2b(first).equals("00000010")){
                    System.out.println("Port In 02H not yet implemented");
                }else{
                    errors(14,"Can't use the port in "+first+"H");
                }
                return 1;
            case "D3": // OUT byte
                if (integer(first) == 3) outs[0] = A;
                else if (integer(first) == 4) System.out.println("Port Out 04H not yet implemented");
                else errors(9,"Can't use the port out "+first+"H");
                return 1;
            case "3C": // INR A
                A = sumB(A,"01");
                setFlags("A");
                break;
            case "17": // RAL
                a = A;
                a = a.substring(1)+a.toCharArray()[0];
                A = a;
                break;
            case "04": // INR B
                B = sumB(B,"01");
                setFlags("B");
                break;
            case "1F": // RAR
                a = A;
                a = a.toCharArray()[a.length()-1]+a.substring(0,a.length()-1);
                A = a;
                break;
            case "0C": // INR C
                C = sumB(C,"01");
                setFlags("C");
                break;
            case "C9": // RET
                int positionSubroutineNow = integer(""+Memory.get(integer("FFFF")-1)+Memory.get(integer("FFFE")-1));
                return positionSubroutineNow-PC+2;
            case "FA": // JM address   
                if (flagSignal == 1) return -1+(integer(second+first)-PC);
                return 2;
            case "32": // STA address
                Memory.set(integer(second+first),b2h(A));
                return 2;
            case "C3": // JMP address
                return -1+(integer(second+first)-PC);
            case "90": // SUB B
                A = subB(A,B);
                setFlags("A");
                break;
            case "C2": // JNZ address
                if (flagZero == 0) return -1+integer(second+first)-PC;
                return 2;
            case "91": // SUB C
                A = subB(A,C);
                setFlags("A");
                break;
            case "CA": // JZ address
                if (flagZero == 1) return -1+integer(second+first)-PC;
                return 2;
            case "A8": // XRA B
                b=h2b(first);a=A;r="";
                while (a.length()<b.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>b.length())b=b.toCharArray()[0]+"0"+b.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]!=b.toCharArray()[i])?"1":"0";
                A = r;
                setFlags("A");
                break;
            case "3A": // LDA address
                A = h2b(Memory.get(integer(second+first)));
                return 2;
            case "A9": // XRA C
                c=h2b(first);a=A;r="";
                while (a.length()<c.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>c.length())c=c.toCharArray()[0]+"0"+c.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]!=c.toCharArray()[i])?"1":"0";
                A = r;
                setFlags("A");
                break;
            case "78": // MOV A,B
                A = B;
                break;
            case "EE": // XRI byte
                c=h2b(first);a=A;r="";
                while (a.length()<c.length())a=a.toCharArray()[0]+"0"+a.substring(1);
                while (a.length()>c.length())c=c.toCharArray()[0]+"0"+c.substring(1);
                for (int i=0;i<a.length();i++)r+=(a.toCharArray()[i]!=c.toCharArray()[i])?"1":"0";
                A = r;
                setFlags("A");
                return 1;
            case "79": // MOV A,C
                A = C;
                break;
            default:
                errors(1,"Code not identified or implemented : '"+entry+"' line "+hex(PC)+"H");
        }
        return 0;
    }

    public static void finalizeIt(){
        System.out.println("-------------");
        try { // Força a limpar Input.txt
            FileWriter file = new FileWriter("Input.txt");
            file.write("");
            file.write("");
            file.close();
        }catch (Exception e){
            errors(12,"Can't open Input.txt\nCheck if this file exists and has the extension '.txt'");
        }
        System.out.printf("System halted\nA : %sH (%s)\nB : %sH (%s)\nC : %sH (%s)\nOUT 03H : %sH (%s)\n",b2h(A),b2d(A),b2h(B),b2d(B),b2h(C),b2d(C),b2h(outs[0]),b2d(outs[0]));
        System.out.println("Executed in " + (float) (System.currentTimeMillis()-time)/1000 + " seconds");
        System.exit(0);
    }

    public static void setFlags(String acumulator){
        switch (acumulator){
            case "A":
                flagSignal = 1;
                flagZero = 1;
                if (b2d(A).toCharArray()[0] != '-') flagSignal = 0;
                if (!(b2d(A).equals("0"))) flagZero = 0;
                break;
            case "B":
                flagSignal = 1;
                flagZero = 1;
                if (b2d(B).toCharArray()[0] != '-') flagSignal = 0;
                if (!(b2d(B).equals("0"))) flagZero = 0;
                break;
            case "C":
                flagSignal = 1;
                flagZero = 1;
                if (b2d(C).toCharArray()[0] != '-') flagSignal = 0;
                if (!(b2d(C).equals("0"))) flagZero = 0;
                break;
        }
    }

    public static void setConfigurations(){
        File config = new File("Config.txt");
        Scanner read = null;
        try {
            read = new Scanner(config);
        } catch (Exception e){
            errors(4, "Can't open 'Config.txt'\nCheck if this file exists and has the extension '.txt'");
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
                case "delay_when_showAnd_useOutput":
                    delay_when_showAnd_useOutput = value;
                    break;
                case "show_when_useOutput":
                    show_when_useOutput = value;
                    break;
                case "showAllOutputs_when_useOutput":
                    showAllOutputs_when_useOutput = value;
                    break;
                case "noCut_when_stackOverflowHardware":
                    noCut_when_stackOverflowHardware = value;
                    break;
                default:
                    errors(7,"Occured a problem when reading a configuration (configuration): '"+line+"'");
            }
        }
        read.close();
    }

    public static void overflowHardware(){
        if (noCut_when_stackOverflowHardware) return;
        if (A.length() > 8){
            String last = A;
            A = A.substring(A.length()-8);
            System.out.println("A : Cut from '"+last+"' to '"+A+"'");
        } 
        if (B.length() > 8){
            String last = B;
            B = B.substring(B.length()-8);
            System.out.println("B : Cut from '"+last+"' to '"+B+"'");
        } 
        if (C.length() > 8){
            String last = C;
            C = C.substring(C.length()-8);
            System.out.println("C : Cut from '"+last+"' to '"+C+"'");
        } 
    }


    // Numberis



    public static void teste1(){
        String a,b,soma,sub;
        a = "0101";
        b = "100000101";
        soma = sumB(a,b);
        System.out.println("><");
        sub = subB(a,b);
        System.out.println("><");
        System.out.println(b2d(a)+"..."+b2d(b));
        System.out.println(soma+":"+b2d(soma));
        System.out.println((Integer.parseInt(b2d(a))+Integer.parseInt(b2d(b))));
        System.out.println(sub+":"+b2d(sub));
        System.out.println((Integer.parseInt(b2d(a))-Integer.parseInt(b2d(b))));
    }
    
    public static String b2h(String a){
        String[] hexs = "0123456789ABCDEF".split("");
        char[] parts = "....".toCharArray();
        String result = "",got = "";
        int j = 0,n;
        
        for (int i=a.length()-1;i>=0;i--){
            parts[3-j] = a.toCharArray()[i];
            if (j==3){
                n = Integer.parseInt(b2d("0"+toString(parts)));
                result = hexs[n]+result; 
                j=0;
            }else j++;
        }
        if (j != 0){
            got = a.substring(0,j);
            n = Integer.parseInt(b2d("0"+got));
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
        String[] hexs = "0123456789ABCDEF".split("");
        String b = "",result = "",got="";
        boolean found =  false;
        int p;

        for (int i=a.length()-1;i>=0;i--){
            p = 0;
            for (String str : hexs){
                if (str.equals(a.toCharArray()[i]+"")){
                    found = true;
                    break;
                }
                p++;
            }
            if (!(found)) errors(15,"Byte not identified : "+a);
            got = Integer.toString(p,2);
            while (got.length() < 4) got = "0"+got;
            b = got + b;
        }
        result = b;
        return result;
    }

    public static String d2b(String a){
        String value = "00000000";char sig = ' ';
        String add = "00000001";
        char[] al = a.toCharArray();
        String new_al = "";
        boolean read = false;

        if (al[0]=='-'){
            add = "10000001";
            al[0] = '0';
            sig = '-';
        }
        for (char c : al){
            if (c == '0' && !(read)) continue;
            read = true;
            new_al = new_al + c;
        }
        if (read) al = new_al.toCharArray();
        if (add.equals("10000001")) while (!(b2d(value).equals(sig+toString(al)))) value = sumB(value,add);   
        else while (!(b2d(value).equals(toString(al)))) value = sumB(value,add);
        return value;
    }

    public static String mult(String a,String b){
        String result = "",step2= "",cacheZeros = "", cacheZerosA = "";
        int cache = 0,step1=0;

        for (int i=1;i<=b.length();i++){
            step2 = "";
            cacheZerosA = "";
            for (int j=1;j<=a.length();j++){
                step1 = Integer.parseInt(""+b.toCharArray()[b.length()-i])*Integer.parseInt(""+a.toCharArray()[a.length()-j])+cache;
                cache = 0;
                if (step1 >= 10) cache = (step1-step1%10)/10;
                step2 = ""+sum(""+step1%10+cacheZerosA+cacheZeros,step2);
                cacheZerosA += "0";
            }
            result = sum(step2,result);
            if (cache != 0) result = sum(cache+cacheZeros+cacheZerosA,result);
            cacheZeros += "0";
        }
        return result;
    }

    public static String subB(String a,String b){ // A-B (bin)
        if (b.length() < 4) while (b.length() < 4) b = b.toCharArray()[0]+"0"+b.substring(1);
        String br = (b.toCharArray()[0] == '0') ? '1'+b.substring(1) : '0'+b.substring(1);
        return sumB(a,br);
    }

    public static String sumB(String a,String b){ // A+B (bin)
        String result = "",value="",cache = "",ls_cache = "";
        char sig=a.toCharArray()[0],mr;
        boolean cont;
        int soma;
        
        if (a.toCharArray()[0] == b.toCharArray()[0]){
            if (min(a.length(),b.length()) == b.length()) while (a.length() != b.length()) b = b.toCharArray()[0]+"0"+b.substring(1);
            else return sumB(b,a);
            if (!(AmajB_ModBin(a,b))) return sumB(b,a);
            for (int i=a.length()-1;i>=0;i--){
                value = "0";
                soma = 0;
                soma += (a.toCharArray()[i] == '1') ? 1 : 0;
                soma += (b.toCharArray()[i] == '1') ? 1 : 0;
                soma += (cache == "1") ? 1 : 0;
                ls_cache = cache;
                cache = "0";
                if (soma == 1 || soma == 3) value = "1";
                if (soma >= 2) cache = "1";
                result = value+result;
            }
        }else{
            mr = ' ';  // mini resultado
            cont = false; // continue
            if (!(AmajB_ModBin(a,b))) return sumB(b,a);
            while (a.length() > b.length()) b = b.toCharArray()[0]+"0"+b.substring(1);
            for (int i=1;i<=min(a.length(),b.length());i++){
                cont = false;
                if (a.toCharArray()[a.length()-i] == '1'){
                    if (b.toCharArray()[b.length()-i] == '1') mr = '0';
                    else mr = '1';
                }else{
                    if (b.toCharArray()[b.length()-i] == '0') mr = '0';
                    else{
                        int k = 1;
                        while (a.length()-(i+k)>=0){
                            char[] ar = a.toCharArray(),br = b.toCharArray();
                            ar[a.length()-(i+k-1)] = '1';
                            if (a.toCharArray()[a.length()-(i+k)] == '1'){
                                br[b.length()-i] = '0';
                                ar[a.length()-(i+k)] = '0';
                                b = toString(br);
                                a = toString(ar);
                                i--;
                                cont = true;
                                break;
                            }
                            a = toString(ar);
                            k++;
                        }
                    }
                }
                if (cont) continue;
                result = mr + result;
            }
        }
        if (ls_cache == "1") return sumB(a.toCharArray()[0] +"0000"+ a.substring(1),b.toCharArray()[0] +"0000"+ b.substring(1));
        result = sig + result.substring(1);
        return result;
    }

    public static String dou(String a){
        String result = "";
        int cache = 0,step1=0;
        for (int i=a.length()-1;i>=0;i--){
            step1 = Integer.parseInt(""+a.toCharArray()[i])*2+cache;
            cache = 0;
            if (step1 >= 10){
                cache = (step1-step1%10)/10;
            }
            result = ""+step1%10 +result;
        }
        return result;
    }
    
    public static String sum(String a,String b){
        String result = "";
        int cache = 0,step1=0;
        if (min(a.length(),b.length()) == b.length()){
            while (a.length() != b.length()){
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
        for ( char c : chars) word = word + c;
        return word;
    }

    public static boolean AmajB_ModBin(String a,String b){ // A>=B (em módulo binário)
        String br = b.substring(1),ar = a.substring(1);
        while (ar.length() > br.length()) br = "0"+br;
        while (ar.length() < br.length()) ar = "0"+ar;
        
        for (int i=0;i<ar.length();i++){
            if (ar.toCharArray()[i] == '1' && br.toCharArray()[i] == '0') return true;
            if (ar.toCharArray()[i] == '0' && br.toCharArray()[i] == '1') return false;
        }
        return true;
    }

}
// By SGL
// 24/05/2024
