// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

/**
 * This class was generated by EMF JET. <b>DO NOT MODIFY IT!</b>
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
// The entire class was regenerated
public class WsdlGenerator {

    protected static String nl;

    public static synchronized WsdlGenerator create(String lineSeparator) {
        nl = lineSeparator;
        WsdlGenerator result = new WsdlGenerator();
        nl = null;
        return result;
    }

    public final String pNL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;

    protected final String text1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + pNL + "<wsdl:definitions " + pNL + "\tname=\"";

    protected final String text2 = "Service\"" + pNL + "\ttargetNamespace=\"http://petals.ow2.org/talend/";

    protected final String text3 = "/\" " + pNL + "\txmlns:tns=\"http://petals.ow2.org/talend/";

    protected final String text4 = "/\""
            + pNL
            + "\txmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
            + pNL
            + "\txmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\""
            + pNL
            + "\txmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">"
            + pNL
            + "\t"
            + pNL
            + "\t<wsdl:types>"
            + pNL
            + "\t\t<xs:schema "
            + pNL
            + "\t\t\txmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + pNL
            + "\t\t\ttargetNamespace=\"http://jaxb.dev.java.net/array\" "
            + pNL
            + "\t\t\tattributeFormDefault=\"unqualified\" "
            + pNL
            + "\t\t\telementFormDefault=\"qualified\""
            + pNL
            + "\t\t\tversion=\"1.0\">"
            + pNL
            + "\t\t\t"
            + pNL
            + "\t\t\t<xs:complexType final=\"#all\" name=\"stringArray\">"
            + pNL
            + "\t\t\t\t<xs:sequence>"
            + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" nillable=\"true\" type=\"xs:string\" />"
            + pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t</xs:schema>" + pNL + "\t" + pNL
            + "\t\t<xs:schema " + pNL + "\t\t\txmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + pNL
            + "\t\t\txmlns:ns1=\"http://jaxb.dev.java.net/array\" " + pNL + "\t\t\txmlns:tns=\"http://petals.ow2.org/talend/";

    protected final String text5 = "/\"" + pNL + "\t\t\tattributeFormDefault=\"unqualified\" " + pNL
            + "\t\t\telementFormDefault=\"qualified\"" + pNL + "\t\t\ttargetNamespace=\"http://petals.ow2.org/talend/";

    protected final String text6 = "/\">" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:import namespace=\"http://jaxb.dev.java.net/array\" />" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:complexType name=\"attachment\">" + pNL + "\t\t\t\t<xs:sequence>" + pNL + "\t\t\t\t\t<xs:element " + pNL
            + "\t\t\t\t\t\txmlns:ns2=\"http://www.w3.org/2005/05/xmlmime\"" + pNL
            + "\t\t\t\t\t\tminOccurs=\"0\" name=\"fileContent\" " + pNL
            + "\t\t\t\t\t\tns2:expectedContentTypes=\"application/octet-stream\"" + pNL
            + "\t\t\t\t\t\ttype=\"xs:base64Binary\" />" + pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL
            + "\t\t\t" + pNL + "\t\t\t" + pNL + "\t\t\t<!-- The input message -->" + pNL
            + "\t\t\t<xs:element name=\"executeJob\" type=\"tns:executeJob\" />" + pNL
            + "\t\t\t<xs:complexType name=\"executeJob\">" + pNL + "\t\t\t\t<xs:sequence>" + pNL
            + "\t\t\t\t\t<xs:element minOccurs=\"0\" name=\"contexts\" type=\"tns:talendContexts\" />" + pNL
            + "\t\t\t\t\t<xs:element minOccurs=\"0\" name=\"in-attachments\" type=\"tns:inAttachments\" />" + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"in-data-bean\" type=\"tns:inRow\" />" + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"talend-option\" type=\"xs:string\" />" + pNL
            + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:complexType name=\"talendContexts\">" + pNL + "\t\t\t\t<xs:sequence>";

    protected final String text7 = pNL + "\t\t\t\t\t\t<xs:element name=\"";

    protected final String text8 = "\" type=\"";

    protected final String text9 = "\" ";

    protected final String text10 = "minOccurs=\"0\" default=\"";

    protected final String text11 = "\"";

    protected final String text12 = " />";

    protected final String text13 = pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:complexType name=\"inAttachments\">" + pNL + "\t\t\t\t<xs:sequence>";

    protected final String text14 = pNL + "\t\t\t\t\t\t<xs:element name=\"";

    protected final String text15 = "\" nillable=\"true\" type=\"tns:attachment\" />";

    protected final String text16 = pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:complexType name=\"inRow\">" + pNL + "\t\t\t\t<xs:sequence>";

    protected final String text17 = pNL + "\t\t\t\t\t<xs:element name=\"";

    protected final String text18 = "\" type=\"";

    protected final String text19 = "\" ";

    protected final String text20 = "default=\"";

    protected final String text21 = "\"";

    protected final String text22 = "nillable=\"true\" ";

    protected final String text23 = " />";

    protected final String text24 = pNL
            + "\t\t\t\t</xs:sequence>"
            + pNL
            + "\t\t\t</xs:complexType>"
            + pNL
            + "\t\t\t"
            + pNL
            + "\t\t\t"
            + pNL
            + "\t\t\t<!-- The output message -->"
            + pNL
            + "\t\t\t<xs:element name=\"executeJobResponse\" type=\"tns:executeJobResponse\" />"
            + pNL
            + "\t\t\t<xs:complexType name=\"executeJobResponse\">"
            + pNL
            + "\t\t\t\t<xs:sequence>"
            + pNL
            + "\t\t\t\t\t<xs:element minOccurs=\"0\" name=\"talend-job-output\" type=\"tns:talendJobOutput\" />"
            + pNL
            + "\t\t\t\t</xs:sequence>"
            + pNL
            + "\t\t\t</xs:complexType>"
            + pNL
            + "\t\t\t"
            + pNL
            + "\t\t\t<xs:complexType name=\"talendJobOutput\">"
            + pNL
            + "\t\t\t\t<xs:sequence>"
            + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"executionResult\" nillable=\"true\" type=\"ns1:stringArray\" />"
            + pNL
            + "\t\t\t\t\t<xs:element minOccurs=\"0\" name=\"outAttachment\" type=\"tns:outAttachments\" />"
            + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"outDataBean\" nillable=\"true\" type=\"tns:outRow\" />"
            + pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:complexType name=\"outAttachments\">" + pNL + "\t\t\t\t<xs:sequence>";

    protected final String text25 = pNL + "\t\t\t\t\t\t<xs:element name=\"";

    protected final String text26 = "\" nillable=\"true\" type=\"tns:attachment\" />";

    protected final String text27 = pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:complexType name=\"outRow\">" + pNL + "\t\t\t\t<xs:sequence>";

    protected final String text28 = pNL + "\t\t\t\t\t<xs:element name=\"";

    protected final String text29 = "\" type=\"";

    protected final String text30 = "\" ";

    protected final String text31 = " default=\"";

    protected final String text32 = "\" ";

    protected final String text33 = "nillable=\"true\" ";

    protected final String text34 = "minOccurs=\"";

    protected final String text35 = "\" ";

    protected final String text36 = "maxOccurs=\"";

    protected final String text37 = "\"";

    protected final String text38 = " />";

    protected final String text39 = pNL + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t" + pNL + "\t\t\t<!-- The possible faults -->" + pNL
            + "\t\t\t<xs:element name=\"TalendBusinessException\" type=\"tns:TalendBusinessException\" />" + pNL
            + "\t\t\t<xs:complexType name=\"TalendBusinessException\">" + pNL + "\t\t\t\t<xs:sequence>" + pNL
            + "\t\t\t\t\t<xs:element name=\"message\" type=\"xs:string\" />" + pNL + "\t\t\t\t</xs:sequence>" + pNL
            + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:element name=\"TalendTechnicalException\" type=\"tns:TalendTechnicalException\" />" + pNL
            + "\t\t\t<xs:complexType name=\"TalendTechnicalException\">" + pNL + "\t\t\t\t<xs:sequence>" + pNL
            + "\t\t\t\t\t<xs:element name=\"message\" type=\"xs:string\" />" + pNL + "\t\t\t\t</xs:sequence>" + pNL
            + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL + "\t\t\t" + pNL
            + "\t\t\t<!-- Types for the operation with an empty result -->" + pNL
            + "\t\t\t<xs:element name=\"executeJobOnly\" type=\"tns:executeJobOnly\" />" + pNL
            + "\t\t\t<xs:complexType name=\"executeJobOnly\">" + pNL + "\t\t\t\t<xs:sequence>" + pNL
            + "\t\t\t\t\t<xs:element minOccurs=\"0\" name=\"contexts\" type=\"tns:talendContexts\" />" + pNL
            + "\t\t\t\t\t<xs:element minOccurs=\"0\" name=\"in-attachments\" type=\"tns:inAttachments\" />" + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"in-data-bean\" type=\"tns:inRow\" />" + pNL
            + "\t\t\t\t\t<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"talend-option\" type=\"xs:string\" />" + pNL
            + "\t\t\t\t</xs:sequence>" + pNL + "\t\t\t</xs:complexType>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<xs:element name=\"executeJobOnlyResponse\" type=\"tns:executeJobOnlyResponse\" />" + pNL
            + "\t\t\t<xs:complexType name=\"executeJobOnlyResponse\">" + pNL + "\t\t\t\t<xs:sequence />" + pNL
            + "\t\t\t</xs:complexType>" + pNL + "\t\t</xs:schema>" + pNL + "\t</wsdl:types>" + pNL + "\t" + pNL
            + "\t<wsdl:message name=\"executeJob\">" + pNL + "\t\t<wsdl:part name=\"parameters\" element=\"tns:executeJob\" />"
            + pNL + "\t</wsdl:message>" + pNL + "\t" + pNL + "\t<wsdl:message name=\"TalendBusinessException\">" + pNL
            + "\t\t<wsdl:part name=\"TalendBusinessException\" element=\"tns:TalendBusinessException\" />" + pNL
            + "\t</wsdl:message>" + pNL + "\t" + pNL + "\t<wsdl:message name=\"TalendTechnicalException\">" + pNL
            + "\t\t<wsdl:part name=\"TalendTechnicalException\" element=\"tns:TalendTechnicalException\" />" + pNL
            + "\t</wsdl:message>" + pNL + "\t" + pNL + "\t<wsdl:message name=\"executeJobResponse\">" + pNL
            + "\t\t<wsdl:part name=\"parameters\" element=\"tns:executeJobResponse\" />" + pNL + "\t</wsdl:message>" + pNL + "\t"
            + pNL + "\t<wsdl:message name=\"executeJobOnlyResponse\">" + pNL
            + "\t\t<wsdl:part name=\"parameters\" element=\"tns:executeJobOnlyResponse\" />" + pNL + "\t</wsdl:message>" + pNL
            + "\t" + pNL + "\t<wsdl:message name=\"executeJobOnly\">" + pNL
            + "\t\t<wsdl:part name=\"parameters\" element=\"tns:executeJobOnly\" />" + pNL + "\t</wsdl:message>" + pNL + "\t"
            + pNL + "\t<wsdl:portType name=\"";

    protected final String text40 = "ServicePortType\">" + pNL + "\t\t<wsdl:operation name=\"executeJob\">" + pNL
            + "\t\t\t<wsdl:input name=\"executeJob\" message=\"tns:executeJob\" />" + pNL
            + "\t\t\t<wsdl:output name=\"executeJobResponse\" message=\"tns:executeJobResponse\" />" + pNL
            + "\t\t\t<wsdl:fault name=\"TalendBusinessException\" message=\"tns:TalendBusinessException\" />" + pNL
            + "\t\t\t<wsdl:fault name=\"TalendTechnicalException\" message=\"tns:TalendTechnicalException\" />" + pNL
            + "\t\t</wsdl:operation>" + pNL + "\t\t" + pNL + "\t\t<wsdl:operation name=\"executeJobOnly\">" + pNL
            + "\t\t\t<wsdl:input name=\"executeJobOnly\" message=\"tns:executeJobOnly\" />" + pNL
            + "\t\t\t<wsdl:output name=\"executeJobOnlyResponse\" message=\"tns:executeJobOnlyResponse\" />" + pNL
            + "\t\t\t<wsdl:fault name=\"TalendTechnicalException\" message=\"tns:TalendTechnicalException\" />" + pNL
            + "\t\t\t<wsdl:fault name=\"TalendBusinessException\" message=\"tns:TalendBusinessException\" />" + pNL
            + "\t\t</wsdl:operation>" + pNL + "\t</wsdl:portType>" + pNL + "\t" + pNL + "\t<wsdl:binding name=\"";

    protected final String text41 = "ServiceSoapBinding\" type=\"tns:";

    protected final String text42 = "ServicePortType\">" + pNL
            + "\t\t<soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\" />" + pNL + "\t\t" + pNL
            + "\t\t<!-- ExecuteJob (with result) -->" + pNL + "\t\t<wsdl:operation name=\"executeJob\">" + pNL
            + "\t\t\t<soap:operation soapAction=\"\" style=\"document\" />" + pNL + "\t\t\t" + pNL
            + "\t\t\t<wsdl:input name=\"executeJob\">" + pNL + "\t\t\t\t<soap:body use=\"literal\" />" + pNL
            + "\t\t\t</wsdl:input>" + pNL + "\t\t\t" + pNL + "\t\t\t<wsdl:output name=\"executeJobResponse\">" + pNL
            + "\t\t\t\t<soap:body use=\"literal\" />" + pNL + "\t\t\t</wsdl:output>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<wsdl:fault name=\"TalendBusinessException\">" + pNL
            + "\t\t\t\t<soap:fault name=\"TalendBusinessException\" use=\"literal\" />" + pNL + "\t\t\t</wsdl:fault>" + pNL
            + "\t\t\t" + pNL + "\t\t\t<wsdl:fault name=\"TalendTechnicalException\">" + pNL
            + "\t\t\t\t<soap:fault name=\"TalendTechnicalException\" use=\"literal\" />" + pNL + "\t\t\t</wsdl:fault>\t\t\t"
            + pNL + "\t\t</wsdl:operation>" + pNL + "\t\t" + pNL + "\t\t<!-- ExecuteJob (without any result) -->" + pNL
            + "\t\t<wsdl:operation name=\"executeJobOnly\">" + pNL
            + "\t\t\t<soap:operation soapAction=\"\" style=\"document\" />" + pNL + "\t\t\t" + pNL
            + "\t\t\t<wsdl:input name=\"executeJobOnly\">" + pNL + "\t\t\t\t<soap:body use=\"literal\" />" + pNL
            + "\t\t\t</wsdl:input>" + pNL + "\t\t\t" + pNL + "\t\t\t<wsdl:output name=\"executeJobOnlyResponse\">" + pNL
            + "\t\t\t\t<soap:body use=\"literal\" />" + pNL + "\t\t\t</wsdl:output>" + pNL + "\t\t\t" + pNL
            + "\t\t\t<wsdl:fault name=\"TalendTechnicalException\">" + pNL
            + "\t\t\t\t<soap:fault name=\"TalendTechnicalException\" use=\"literal\" />" + pNL + "\t\t\t</wsdl:fault>" + pNL
            + "\t\t\t" + pNL + "\t\t\t<wsdl:fault name=\"TalendBusinessException\">" + pNL
            + "\t\t\t\t<soap:fault name=\"TalendBusinessException\" use=\"literal\" />" + pNL + "\t\t\t</wsdl:fault>" + pNL
            + "\t\t</wsdl:operation>" + pNL + "\t</wsdl:binding>" + pNL + "\t" + pNL + "\t<wsdl:service name=\"";

    protected final String text43 = "Service_";

    protected final String text44 = "\">" + pNL + "\t\t<wsdl:port name=\"";

    protected final String text45 = "\" binding=\"tns:";

    protected final String text46 = "ServiceSoapBinding\">" + pNL
            + "\t\t\t<soap:address location=\"http://localhost:9090/petals.does.not.use.it\" />" + pNL + "\t\t</wsdl:port>" + pNL
            + "\t</wsdl:service>" + pNL + "</wsdl:definitions>";

    protected final String text47 = pNL;

    public String generate(Object argument) {
        final StringBuffer stringBuffer = new StringBuffer();

        PetalsWsdlBean bean = (PetalsWsdlBean) argument;

        stringBuffer.append(text1);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text2);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text3);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text4);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text5);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text6);

        for (ContextTypeDefinition def : bean.contextDefinitions) {
            if (def.exportType == ContextExportType.PARAMETER || def.exportType == ContextExportType.PARAMETER_AND_OUT_ATTACHMENT) {

                stringBuffer.append(text7);
                stringBuffer.append(def.definition.name);
                stringBuffer.append(text8);
                stringBuffer.append(def.definition.type);
                stringBuffer.append(text9);

                if (def.definition.defaultValue != null && def.definition.defaultValue.trim().length() > 0) {

                    stringBuffer.append(text10);
                    stringBuffer.append(def.definition.defaultValue);
                    stringBuffer.append(text11);

                }

                stringBuffer.append(text12);

            }
        }

        stringBuffer.append(text13);

        for (ContextTypeDefinition def : bean.contextDefinitions) {
            if (def.exportType == ContextExportType.IN_ATTACHMENT) {

                stringBuffer.append(text14);
                stringBuffer.append(def.definition.name);
                stringBuffer.append(text15);

            }
        }

        stringBuffer.append(text16);

        for (ElementTypeDefinition def : bean.tPetalsInputSchema) {

            stringBuffer.append(text17);
            stringBuffer.append(def.name);
            stringBuffer.append(text18);
            stringBuffer.append(def.type);
            stringBuffer.append(text19);

            if (def.defaultValue != null) {
                stringBuffer.append(text20);
                stringBuffer.append(def.defaultValue);
                stringBuffer.append(text21);
            } else if (def.nillable) {
                stringBuffer.append(text22);
            }
            stringBuffer.append(text23);

        } // End of "for" loop

        stringBuffer.append(text24);

        for (ContextTypeDefinition def : bean.contextDefinitions) {
            if (def.exportType == ContextExportType.OUT_ATTACHMENT
                    || def.exportType == ContextExportType.PARAMETER_AND_OUT_ATTACHMENT) {

                stringBuffer.append(text25);
                stringBuffer.append(def.definition.name);
                stringBuffer.append(text26);

            }
        }

        stringBuffer.append(text27);

        for (ElementTypeDefinition def : bean.tPetalsOutputSchema) {

            stringBuffer.append(text28);
            stringBuffer.append(def.name);
            stringBuffer.append(text29);
            stringBuffer.append(def.type);
            stringBuffer.append(text30);

            if (def.defaultValue != null) {
                stringBuffer.append(text31);
                stringBuffer.append(def.defaultValue);
                stringBuffer.append(text32);
            } else if (def.nillable) {
                stringBuffer.append(text33);
            } else {
                stringBuffer.append(text34);
                stringBuffer.append(def.minOccurs);
                stringBuffer.append(text35);
            }
            if (def.maxOccurs > 1) {
                stringBuffer.append(text36);
                stringBuffer.append(def.maxOccurs);
                stringBuffer.append(text37);
            }
            stringBuffer.append(text38);

        } // end of "for" loop

        stringBuffer.append(text39);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text40);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text41);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text42);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text43);
        stringBuffer.append(bean.jobVersion);
        stringBuffer.append(text44);
        stringBuffer.append(bean.autogenerate ? "autogenerate" : (bean.jobName + "_" + bean.jobVersion + "_Endpoint"));
        stringBuffer.append(text45);
        stringBuffer.append(bean.jobName);
        stringBuffer.append(text46);
        stringBuffer.append(text47);
        return stringBuffer.toString();
    }
}
