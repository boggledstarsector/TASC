package data.campaign.econ.industries;

import java.util.LinkedHashMap;

interface BoggledCommonIndustryInterface {
    // Token replacements should be private but Java 7
    LinkedHashMap<String, String> getTokenReplacements();
}
