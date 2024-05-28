import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

// Assembler asd CPU of the SAP 2 _ ast
public class acst{

    // errors 5
    public static void errors(int error,String saida){
        System.out.printf("\nACST ERROR %03d : %s\n",error,saida);
        System.exit(-1);
    }
    public static boolean[] onPresets = {true,true,true};
    public static void main(String[] args) {
        setConfigurations();
        ProcessBuilder processBuilder = new ProcessBuilder();
        String[] presets = "javac acst.java;javac ast.java;javac cst.java".split(";");
        String exec_ast = "java ast ";
        String exec_cst = "java cst ";
        
        switch (args.length){
            case 2:
                exec_ast += args[0]+" "+args[1];
                exec_cst += args[0];
                break;
            case 3:
                exec_ast += args[0]+" "+args[1]+" "+args[2];
                exec_cst += args[1];
                break;
            default:
                errors(1,"This format of input is not available");
        }

        try {
            // Preset
            Process process;
            BufferedReader reader;
            String line;
            int i=-1;
            for (String preset : presets){
                i++;
                if (!(onPresets[i])) continue;
                System.out.println("Executing : "+preset);
                processBuilder.command("cmd.exe","/c",preset);
                process = processBuilder.start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) System.out.println(line);
            }

            // AST
            processBuilder.command("cmd.exe","/c",exec_ast);
            process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) System.out.println(line);
            
            // CST
            processBuilder.command("cmd.exe","/c",exec_cst);
            process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) System.out.println(line);


        } catch (Exception e){

        }
    }

    public static void setConfigurations(){
        File config = new File("config.txt");
        Scanner read = null;
        try {
            read = new Scanner(config);
        } catch (Exception e){
            errors(5, "Can't open 'config.txt'\nCheck if this file exists and has the extension '.txt'");
        }
        ArrayList<String> lines = new ArrayList<String>();
        while (read.hasNextLine()){
            lines.add(read.nextLine());
        }
        
        for (String line : lines){
            if (!(line.split(" ")[0].equals("ACST"))) continue;
            String configurationStr = line.split(" ")[1];
            String valueStr = "";
            try{
                valueStr = line.split(" ")[2];
            } catch (Exception e){
                errors(2,"Occured a problem when reading a configuration (value): '"+line+"'");
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
                    errors(3,"Occured a problem identifying a configuration (value): '"+line+"'");
            }
            switch (configurationStr){
                case "compileACST":
                    onPresets[0] = value;
                    break;
                case "compileAST":
                    onPresets[1] = value;
                    break;
                case "compileCST":
                    onPresets[2] = value;
                    break;
                default:
                    errors(4,"Occured a problem when reading a configuration (configuration): '"+line+"'");

            }
        }

    }
}
 
// Create by SGL
// 26/05/2024