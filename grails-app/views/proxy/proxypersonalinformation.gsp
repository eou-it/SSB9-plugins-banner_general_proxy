<%@ page import="net.hedtech.banner.general.system.State" %>
<%@ page import="net.hedtech.banner.general.system.Nation" %>
<%@ page import="net.hedtech.banner.general.system.ldm.Gender" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title></title>
    <link rel="stylesheet"
          href="${resource(plugin: 'bannerGeneralPersonalInformationUi', dir: 'css', file: 'main.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'generalSsbMain.css')}"/>
</head>

<body>
<br>
<b>Proxy Profile</b><br>
Please keep your Banner Web proxy information up-to-date.<br>

<g:if test='${flash.message}'>
    <span class="icon-error"></span>${flash.message}
</g:if>

<FORM ACTION="updateProxyProfile" METHOD="POST" id="proxyprofile" autocomplete="off">
    <TABLE CLASS="dataentrytable" SUMMARY="This table displays the proxy profile fields.">

        <g:if test="${proxyUiRules?.p_name_prefix?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_name_prefix><SPAN>Salutation</SPAN>
            <g:if test="${proxyUiRules?.p_name_prefix?.required}">
                        <span class="red-star"> *</span>
            </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_name_prefix" value="${proxyProfile?.p_name_prefix}" SIZE="22"
                           MAXLENGTH="20" id="p_name_prefix">
                </TD>
            </TR>
        </g:if>

        <TR>
            <TD CLASS="input-field-label">
                <LABEL for=p_first_name><SPAN>First Name</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field-proxy">
                <INPUT TYPE="text" NAME="p_first_name" value="${proxyProfile?.p_first_name}" SIZE="62" MAXLENGTH="60"
                       VALUE="Mike" id="p_first_name">
            </TD>
        </TR>

        <g:if test="${proxyUiRules?.p_mi?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_mi><SPAN>Middle Name</SPAN>
                        <g:if test="${proxyUiRules?.p_mi?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_mi" value="${proxyProfile?.p_mi}" SIZE="62" MAXLENGTH="60" id="p_mi">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_surname_prefix?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_surname_prefix><SPAN>Surname Prefix</SPAN>
                        <g:if test="${proxyUiRules?.p_surname_prefix?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_surname_prefix" value="${proxyProfile?.p_surname_prefix}" SIZE="62"
                           MAXLENGTH="60" id="p_surname_prefix">
                </TD>
            </TR>

        </g:if>

        <TR>
            <TD CLASS="input-field-label">
                <LABEL for=p_last_name><SPAN>Last Name</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field-proxy">
                <INPUT TYPE="text" NAME="p_last_name" value="${proxyProfile?.p_last_name}" SIZE="62" MAXLENGTH="60"
                       VALUE="Hitrik" id="p_last_name">
            </TD>
        </TR>

        <g:if test="${proxyUiRules?.p_name_suffix?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_name_suffix><SPAN>Name Suffix</SPAN>
                        <g:if test="${proxyUiRules?.p_name_suffix?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_name_suffix" value="${proxyProfile?.p_name_suffix}" SIZE="22"
                           MAXLENGTH="20" id="p_name_suffix">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_pref_first_name?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_pref_first_name><SPAN>Nickname</SPAN>
                        <g:if test="${proxyUiRules?.p_pref_first_name?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_pref_first_name" value="${proxyProfile?.p_pref_first_name}" SIZE="62"
                           MAXLENGTH="60" id="p_pref_first_name">
                </TD>
            </TR>
        </g:if>

        <TR>
            <TD CLASS="input-field-label">
                <LABEL for=p_email_address><SPAN
                        class="fieldlabeltext">Home E Mail Address &nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field-proxy">
                <INPUT TYPE="text" NAME="p_email_address" value="${proxyProfile?.p_email_address}" SIZE="75"
                       MAXLENGTH="128" VALUE="michael.hitrik@ellucian.com"
                       id="p_email_address">
            </TD>
        </TR>

        <g:if test="${proxyUiRules?.p_phone_area?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_phone_area><SPAN
                            class="fieldlabeltext">Permanent Phone Area Code&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_phone_area?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_phone_area" value="${proxyProfile?.p_phone_area}" SIZE="7" MAXLENGTH="6"
                           id="p_phone_area">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_phone_number?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_phone_number><SPAN
                            class="fieldlabeltext">Permanent Phone Number&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_phone_number?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_phone_number" value="${proxyProfile?.p_phone_number}" SIZE="14"
                           MAXLENGTH="12" id="p_phone_number">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_phone_ext?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_phone_ext><SPAN
                            class="fieldlabeltext">Permanent Phone Extension &nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_phone_ext?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_phone_ext" value="${proxyProfile?.p_phone_ext}" SIZE="11" MAXLENGTH="10"
                           id="p_phone_ext">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_street_line1?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_street_line1><SPAN
                            class="fieldlabeltext">Mailing Address Line 1 &nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_street_line1?.required}">
                            <span class="red-star"> *</span>
                        </g:if>

                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_street_line1" value="${proxyProfile?.p_street_line1}" SIZE="77"
                           MAXLENGTH="75" VALUE="4 Country View Rd"
                           id="p_street_line1">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_street_line2?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_street_line2><SPAN
                            class="fieldlabeltext">Mailing Address Line 2 &nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_street_line2?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_street_line2" value="${proxyProfile?.p_street_line2}" SIZE="77"
                           MAXLENGTH="75" id="p_street_line2">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_street_line3?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_street_line3><SPAN
                            class="fieldlabeltext">Mailing Address Line 3 &nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_street_line3?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_street_line3" value="${proxyProfile?.p_street_line3}" SIZE="77"
                           MAXLENGTH="75" id="p_street_line3">
                </TD>
            </TR>
        </g:if>
        <g:if test="${proxyUiRules?.p_street_line4?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_street_line4><SPAN
                            class="fieldlabeltext">Mailing Address Line 4 &nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_street_line4?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_street_line4" value="${proxyProfile?.p_street_line4}" SIZE="77"
                           MAXLENGTH="75" id="p_street_line4">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_city?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_city><SPAN class="fieldlabeltext">City&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_city?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_city" value="${proxyProfile?.p_city}" SIZE="52" MAXLENGTH="50"
                           VALUE="Malvern" id="p_city">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_stat_code?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_stat_code><SPAN class="fieldlabeltext">State&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_stat_code?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <g:select name="p_stat_code" optionKey="code" from="${State.list().sort { it.description }}"
                              optionValue="${{ it.description }}" value="${proxyProfile?.p_stat_code}"
                              noSelection="['null': 'Enter State']"></g:select>

                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_zip?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_zip><SPAN class="fieldlabeltext">Zipcode&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_zip?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_zip" value="${proxyProfile?.p_zip}" SIZE="33" MAXLENGTH="30" VALUE="19355"
                           id="p_zip">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_natn_code?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_natn_code><SPAN class="fieldlabeltext">Nation&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_natn_code?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <g:select name="p_natn_code" optionKey="code" from="${Nation.list().sort { it.nation }}"
                              optionValue="${{ it.nation }}" value="${proxyProfile?.p_natn_code}"
                              noSelection="['null': 'Enter Nation']"></g:select>

                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_sex?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_sex><SPAN class="fieldlabeltext">Gender&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_sex?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <g:select name="p_sex" from="${Gender.values().sort { it.bannerValue }}" optionKey="bannerValue"
                              value="${proxyProfile?.p_sex}"/>
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_birth_date?.visible}">

            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_birth_date><SPAN class="fieldlabeltext">Birthdate (DD/MM/YYYY)&nbsp;&nbsp;&nbsp;</SPAN>
                        <g:if test="${proxyUiRules?.p_birth_date?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="date" NAME="p_birth_date" value="${proxyProfile?.p_birth_date}" id="p_birth_date">
                </TD>
            </TR>
        </g:if>

        <g:if test="${proxyUiRules?.p_ssn?.visible}">
            <TR>
                <TD CLASS="input-field-label">
                    <LABEL for=p_ssn><SPAN class="fieldlabeltext">SSN/SIN/TIN</SPAN>
                        <g:if test="${proxyUiRules?.p_ssn?.required}">
                            <span class="red-star"> *</span>
                        </g:if>
                    </LABEL>
                </TD>
                <TD CLASS="text-input-field-proxy">
                    <INPUT TYPE="text" NAME="p_ssn" value="${proxyProfile?.p_ssn}" SIZE="10" MAXLENGTH="9" id="p_ssn"
                           autocomplete="off">
                </TD>
            </TR>
        </g:if>

        <tr>
            <td><INPUT TYPE="submit" NAME="p_save" VALUE="Save" id="p_save" class="signin-button"></td>
        </tr>
    </TABLE>
</FORM>

</body>
</html>