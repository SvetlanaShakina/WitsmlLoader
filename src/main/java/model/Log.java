package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import model.LogCurveInfo;
import model.LogData;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Log {

    // Атрибуты XML
    @JacksonXmlProperty(isAttribute = true, localName = "uid")
    public String uid;  // logs.uid

    @JacksonXmlProperty(isAttribute = true, localName = "uidWell")
    public String well_uid; // wellbores.well_uid

    @JacksonXmlProperty(localName = "nameWell")
    public String nameWell; // для wells.name

    @JacksonXmlProperty(isAttribute = true, localName = "uidWellbore")
    public String wellbore_uid; // wellbores.uid

    // Теги XML
    @JacksonXmlProperty(localName = "name")
    public String name; // logs.name

    @JacksonXmlProperty(localName = "nameWellbore")
    public String nameWellbore; // wellbores.name

    @JacksonXmlProperty(localName = "serviceCompany")
    public String service_company; // logs.service_company

    @JacksonXmlProperty(localName = "startDateTimeIndex")
    public String start_time; // logs.start_time

    @JacksonXmlProperty(localName = "endDateTimeIndex")
    public String end_time; // logs.end_time

    @JacksonXmlProperty(localName = "indexType")
    public String index_type; // logs.index_type

    @JacksonXmlProperty(localName = "creationDate")
    public String creation_date; // logs.creation_date

    @JacksonXmlProperty(localName = "indexCurve")
    public String index_curve; // logs.index_curve

    @JacksonXmlProperty(localName = "direction")
    public String direction; // logs.direction

    // Вложенные элементы (оставляем как было)
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "logCurveInfo")
    public List<LogCurveInfo> logCurveInfo;

    @JacksonXmlProperty(localName = "logData")
    public LogData logData;

}
