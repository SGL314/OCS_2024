public class numberis {
    public static void main (String[] args){
        String a,b;
        a = "00010011";
        b = "10000101";
        System.out.println(sumB(a,b));
        
    }
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

    public static String sumB(String a,String b){
        

        char sig=' ';
        String result = "",value="";
        String cache = "",ls_cache = "";
        sig = a.toCharArray()[0];
        if (a.toCharArray()[0] == b.toCharArray()[0]){
            if (min(a.length(),b.length()) == b.length()){
                while (a.length() != b.length()&& a.length() != b.length()){
                    b = "0"+b;
                }
            }else return sumB(b,a);
            for (int i=a.length()-1;i>=0;i--){
                value = "0";
                int soma = 0;
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
            char mr = ' ';
            boolean cont = false;
            for (int i=1;i<=min(a.length(),b.length());i++){
                cont = false;
                //System.out.println(a.toCharArray()[a.length()-(i)]+" "+(i));
                if (a.toCharArray()[a.length()-i] == '1'){
                    if (b.toCharArray()[b.length()-i] == '1') mr = '0';
                    else mr = '1';
                }else{
                    if (b.toCharArray()[b.length()-i] == '0') mr = '0';
                    else{
                        int k = 1;
                        while (a.length()-(i+k)>=0){
                            //System.out.println(a.toCharArray()[a.length()-(i+k)]+" "+(i));
                            char[] ar = a.toCharArray(),br = b.toCharArray();
                            ar[a.length()-(i+k-1)] = '1';
                            if (a.toCharArray()[a.length()-(i+k)] == '1'){
                                
                                br[b.length()-i] = '0';
                                ar[a.length()-(i+k)] = '0';
                                //ar[a.length()-(i)] = '1';
                                b = toString(br);
                                a = toString(ar);
                                i--;
                                cont = true;
                                //System.out.println(i);
                                break;
                            }
                            a = toString(ar);
                            k++;
                        }
                    }
                }
                if (cont) continue;
                result = mr + result;
                //System.out.println(result);
            }
        }
        if (ls_cache == "1") return sumB(a.toCharArray()[0] +"000"+ a,b.toCharArray()[0] +"000"+ b);
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
