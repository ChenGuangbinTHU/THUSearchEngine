import java.io.*;

/**
 * Created by chen on 17-5-23.
 */
public class Tools {
    public static void writeContent(String s,String filename) {
        try {

            File file = new File(filename);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringAfterMirror(String s) {
        return s.substring(s.indexOf("mirror")+7);
    }

    public static String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }

    /*
    handle string so that can be analyze by xml
     */
    public static String processString(String s) {
        if(s==null)
            return "";
        else {
            s = s.replace( "&", "&amp;" );
            s = s.replace( "<", "&lt;" );
            s = s.replace( ">", "&gt;" );
            s = s.replace( "\"", "&quot;" );
            s = s.replace( "\'", "&apos;" );
            s = s.replaceAll("[\\000]+", "");
            StringBuffer out = new StringBuffer(); // Used to hold the output.
            char current; // Used to reference the current character.

            if (s == null || ("".equals(s))) return ""; // vacancy test.
            for (int i = 0; i < s.length(); i++) {
                current = s.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
                if ((current == 0x9) ||
                        (current == 0xA) ||
                        (current == 0xD) ||
                        ((current >= 0x20) && (current <= 0xD7FF)) ||
                        ((current >= 0xE000) && (current <= 0xFFFD)) ||
                        ((current >= 0x10000) && (current <= 0x10FFFF)))
                    out.append(current);
            }
            return out.toString();
        }



    }


    public static void main(String[] args) {
        String s ="<<First <Previous Next> Last>> ";
        System.out.println(Tools.processString(s));
    }


}
