package org.hsweb.web.crawler.extracter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultHtmlContentExtractor implements HtmlContentExtractor {
    private static String clearLabel(String html) {
        html = html.replaceAll("(?is)<!DOCTYPE.*?>", "");
        html = html.replaceAll("(?is)<!--.*?-->", "");                // remove html comment
        html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
        html = html.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
        html = html.replaceAll("&.{2,5};|&#.{2,5};", " ");            // remove special char
        html = html.replaceAll("(?is)<.*?>", "");
        return html;
    }
    private int defaultThreshold=35;
    public DefaultHtmlContentExtractor(){}
    public DefaultHtmlContentExtractor(int threshold){
        this.defaultThreshold=threshold;
    }
    @Override
    public String parse(String html) {
        html = clearLabel(html);
        final int blocksWidth = 3;
        int start, end, threshold = defaultThreshold;
        StringBuilder text = new StringBuilder();
        List<Integer> indexDistribution = new ArrayList<>();
        List<String> lines = Arrays.asList(html.split("\n"));
        indexDistribution.clear();
        for (int i = 0; i < lines.size() - blocksWidth; i++) {
            int wordsNum = 0;
            for (int j = i; j < i + blocksWidth; j++) {
                lines.set(j, lines.get(j).replaceAll("\\s+", ""));
                wordsNum += lines.get(j).length();
            }
            indexDistribution.add(wordsNum);
        }
        start = -1;
        end = -1;
        boolean boolStart = false, boolEnd = false;
        text.setLength(0);
        for (int i = 0, len = indexDistribution.size(); i < len - 1; i++) {
            if (indexDistribution.get(i) > threshold && !boolStart) {
                if (indexDistribution.get(i + 1).intValue() != 0
                        || indexDistribution.get(i + 2).intValue() != 0
                        || indexDistribution.get(i + 3).intValue() != 0) {
                    boolStart = true;
                    start = i;
                    continue;
                }
            }
            if (boolStart) {
                if (indexDistribution.get(i).intValue() == 0
                        || indexDistribution.get(i + 1).intValue() == 0) {
                    end = i;
                    boolEnd = true;
                }
            }
            if (boolEnd) {
                StringBuilder tmp = new StringBuilder();
                for (int ii = start; ii <= end; ii++) {
                    if (lines.get(ii).length() < 5) continue;
                    tmp.append(lines.get(ii) + "\n");
                }
                String str = tmp.toString();
                if (str.toLowerCase().contains("copyright") || str.contains("版权所有")) continue;
                text.append(str);
                boolStart = boolEnd = false;
            }
        }
        return text.toString();
    }
}
