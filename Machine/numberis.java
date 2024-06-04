import java.util.ArrayList;
import java.util.Scanner;

public class numberis {
    public static void sleep(float timeSleep){
        try { 
            Thread.sleep((int) (timeSleep*1000));
        } catch (Exception e) {
        }
    }
    public static void main (String[] args){
        String a,b,soma,sub;
        a = "0";
        System.out.println(d2b(a));
    }
    
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
        String b = "",result = "",got="";
        String[] hexs = "0123456789ABCDEF".split("");
        int p;

        for (int i=a.length()-1;i>=0;i--){
            p = 0;
            for (String str : hexs){
                if (str.equals(a.toCharArray()[i]+"")) break;
                p++;
            }
            got = Integer.toString(p,2);
            while (got.length() < 4) got = "0"+got;
            b = got + b;
        }
        result = b;
        return result;
    }

    public static String d2b(String a){
        String value = "0000";char sig = ' ';
        String add = "0001";
        char[] al = a.toCharArray();
        String new_al = "";
        boolean read = false;

        if (al[0]=='-'){
            add = "1001";
            al[0] = '0';
            sig = '-';
        }
        for (char c : al){
            if (c == '0' && !(read)) continue;
            read = true;
            new_al = new_al + c;
        }
        if (read) al = new_al.toCharArray();
        System.out.println(toString(al));
        if (add.equals("1001")) while (!(b2d(value).equals(sig+toString(al)))) value = sumB(value,add);   
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
