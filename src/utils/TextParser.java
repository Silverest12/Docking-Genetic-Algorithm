package utils;

import dock.DockData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    private static int matcherToInt (Matcher toFind) {
        String output = "0";

        if (toFind.find()) {
            output = toFind.group().split("\\s*[=:]\\s*")[1];
        }

        return Integer.parseInt(output);
    }

    private static LinkedHashMap<String, Integer> matcherToHashMap (Matcher toFind) {
        LinkedHashMap<String, Integer> output = new LinkedHashMap<>();

        while (toFind.find()) {
            String[] str = toFind.group().split("\\s*->\\s*");
            output.put(str[0].trim(), convertHourToMin (str[1]));
        }

        return output;
    }

    private static int convertHourToMin (String time) {
        String[] timeSp = time.split("h");
        return Integer.parseInt(timeSp[0]) * 60 + (timeSp.length >= 2? Integer.parseInt(timeSp[1]) : 0);
    }

    private static LinkedHashMap<String, Integer> matcherToHashMapInt (Matcher toFind) {
        LinkedHashMap<String, Integer> output = new LinkedHashMap<>();

        while (toFind.find()) {
            String[] str = toFind.group().split("\\s*->\\s*");
            output.put(str[0].trim(), Integer.parseInt(str[1]));
        }

        return output;
    }

    private static LinkedHashMap<String, Integer> matcherTempArr (Matcher toFind) {
        LinkedHashMap<String, Integer>  output = new LinkedHashMap<>();

        if (toFind.find())
            output = matcherToHashMap(Pattern.compile("\\w+\\s*->\\s*\\d+h\\d*").matcher(toFind.group()));

        return output;
    }

    private static LinkedHashMap<String, HashMap<String, Integer>> matcherToDict (Matcher toFind) {
        LinkedHashMap<String, HashMap<String, Integer>> output = new LinkedHashMap<>();
        // Poste1 -> Map < ID Navire -> duree Serv>
        while (toFind.find()) {
            String[] str = toFind.group().split("\\s*:\\s*",2);
            output.put(str[0].trim(), matcherToHashMapInt(Pattern.compile("\\w+\\s*->\\s*\\d+").matcher(str[1])));
        }

        return output;
    }

    public static DockData convertTextToDoc (String text) {
        Pattern nvrPattern = Pattern.compile("nombre de navires\\s*[=:]\\s*\\d+", Pattern.CASE_INSENSITIVE);
        Pattern pstPattern = Pattern.compile("nombre de postes d'amarrage\\s*[=:]\\s*\\d+", Pattern.CASE_INSENSITIVE);
        Pattern arrPattern = Pattern.compile("(\\w+\\s*->\\s*\\d+h\\d*\\s*)+", Pattern.CASE_INSENSITIVE);
        Pattern durPattern = Pattern.compile("(Poste\\d+\\s*:\\s*" +
                                                    "(\\w*\\s*->\\s*\\d+\\s*)+)", Pattern.CASE_INSENSITIVE);

        return new DockData.Builder()
                .setNbrNvr(matcherToInt(nvrPattern.matcher(text)))
                .setNbrPst(matcherToInt(pstPattern.matcher(text)))
                .setNvrTimeMap(matcherTempArr(arrPattern.matcher(text)))
                .setTmpService(matcherToDict(durPattern.matcher(text)))
                .build();
    }
}
