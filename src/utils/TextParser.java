package utils;

import dock.DockData;

import java.util.HashMap;
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

    private static HashMap<String, Integer> matcherToHashMap (Matcher toFind) {
        HashMap<String, Integer> output = new HashMap<>();

        while (toFind.find()) {
            String[] str = toFind.group().split("\\s*->\\s*");
            output.put(str[0], convertHourToMin (str[1]));
        }

        return output;
    }

    private static int convertHourToMin (String time) {
        String[] timeSp = time.split("h");
        return Integer.parseInt(timeSp[0]) * 60 + (timeSp.length >= 2? Integer.parseInt(timeSp[1]) : 0);
    }

    private static HashMap<String, Integer> matcherToHashMapInt (Matcher toFind) {
        HashMap<String, Integer> output = new HashMap<>();

        while (toFind.find()) {
            String[] str = toFind.group().split("\\s*->\\s*");
            output.put(str[0], Integer.parseInt(str[1]));
        }

        return output;
    }

    private static HashMap<String, Integer> matcherTempArr (Matcher toFind) {
        HashMap<String, Integer>  output = new HashMap<>();

        if (toFind.find())
            output = matcherToHashMap(Pattern.compile("\\w+\\s*->\\s*\\d+h\\d*").matcher(toFind.group()));

        return output;
    }

    private static HashMap<String, HashMap<String, Integer>> matcherToDict (Matcher toFind) {
        HashMap<String, HashMap<String, Integer>> output = new HashMap<>();

        while (toFind.find()) {
            String[] str = toFind.group().split("\\s*:\\s*",2);
            output.put(str[0], matcherToHashMapInt(Pattern.compile("\\w+\\s*->\\s*\\d+").matcher(str[1])));
        }

        return output;
    }

    public static DockData convertTextToDoc (String text) {
        DockData.DockBuilder dock = new DockData.DockBuilder();

        Pattern nvrPattern = Pattern.compile("nombre de navires\\s*[=:]\\s*\\d+", Pattern.CASE_INSENSITIVE);
        Pattern pstPattern = Pattern.compile("nombre de postes d'amarrage\\s*[=:]\\s*\\d+", Pattern.CASE_INSENSITIVE);
        Pattern arrPattern = Pattern.compile("(\\w+\\s*->\\s*\\d+h\\d*\\s*)+", Pattern.CASE_INSENSITIVE);
        Pattern durPattern = Pattern.compile("(Poste\\d+\\s*:\\s*" +
                                                    "(\\w*\\s*->\\s*\\d+\\s*)+)", Pattern.CASE_INSENSITIVE);

        return dock.setNbrNvr(matcherToInt(nvrPattern.matcher(text)))
                .setNbrPst(matcherToInt(pstPattern.matcher(text)))
                .setNvrTimeMap(matcherTempArr(arrPattern.matcher(text)))
                .setTmpService(matcherToDict(durPattern.matcher(text)))
                .build();
    }
}
