package es.mindef.bod.reader;


import java.text.Normalizer;
import java.util.regex.Pattern;



public class Utiles {



    public static String quitarAcentos(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }


}