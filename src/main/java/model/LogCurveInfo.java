package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogCurveInfo {
    @JacksonXmlProperty(isAttribute = true)
    public String uid;
    public String mnemonic;
    public String unit;
    public String curveDescription;
    public String dataSource;
    public String typeLogData;
    public String minDateTimeIndex;
    public String maxDateTimeIndex;
}
