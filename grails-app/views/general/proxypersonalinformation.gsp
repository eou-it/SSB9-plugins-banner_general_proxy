<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title></title>
    <link rel="stylesheet" href="${resource(plugin: 'bannerGeneralPersonalInformationUi', dir: 'css', file: 'main.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
</head>

<body>
THIS IS A PLACEHOLDER TO VIEW Proxy Personal Information

<button ng-click="goApp(tileData.url)" class="signin-button">

    <div>
        <div class="tile-title">
            Change PIN
        </div>

    </div>
</button>

<FORM ACTION="/GVUDB/bwgkpxya.P_PA_StoreProfile" METHOD="POST" id="proxyprofile" autocomplete="off">
    <INPUT TYPE="hidden" NAME="p_proxyIDM" VALUE="-99999700">
    <TABLE CLASS="dataentrytable" SUMMARY="This table displays the proxy profile fields.">
        <TR>
            <TD COLSPAN="2" CLASS="text-input-field">
                <SPAN class="requiredmsgtext"><SPAN class="fieldrequiredtext"><IMG
                        SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom" ALT="Required" CLASS="headerImg"
                        TITLE="Required" NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN> - indicates a required field.</SPAN>
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_name_prefix><SPAN>Salutation</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_name_prefix" SIZE="22" MAXLENGTH="20" id="p_name_prefix" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_first_name><SPAN >First Name</SPAN><SPAN
                        ><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_first_name" SIZE="62" MAXLENGTH="60" VALUE="Mike" id="p_first_name" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_mi><SPAN >Middle Name</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_mi" SIZE="62" MAXLENGTH="60" id="p_mi" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_surname_prefix><SPAN>Surname Prefix</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_surname_prefix" SIZE="62" MAXLENGTH="60" id="p_surname_prefix" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_last_name><SPAN>Last Name</SPAN><SPAN
                        class="fieldrequiredtext"><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_last_name" SIZE="62" MAXLENGTH="60" VALUE="Hitrik" id="p_last_name" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_name_suffix><SPAN>Name Suffix</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_name_suffix" SIZE="22" MAXLENGTH="20" id="p_name_suffix" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_pref_first_name><SPAN>Nickname</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_pref_first_name" SIZE="62" MAXLENGTH="60" id="p_pref_first_name" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_email_address><SPAN
                        class="fieldlabeltext">Home E Mail Address &nbsp;&nbsp;&nbsp;</SPAN><SPAN
                        class="fieldrequiredtext"><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_email_address" SIZE="75" MAXLENGTH="128" VALUE="michael.hitrik@ellucian.com"
                       id="p_email_address" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_phone_area><SPAN class="fieldlabeltext">Permanent Phone Area Code&nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_phone_area" SIZE="7" MAXLENGTH="6" id="p_phone_area" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_phone_number><SPAN class="fieldlabeltext">Permanent Phone Number&nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_phone_number" SIZE="14" MAXLENGTH="12" id="p_phone_number" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_phone_ext><SPAN class="fieldlabeltext">Permanent Phone Extension &nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_phone_ext" SIZE="11" MAXLENGTH="10" id="p_phone_ext" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_street_line1><SPAN
                        class="fieldlabeltext">Mailing Address Line 1 &nbsp;&nbsp;&nbsp;</SPAN><SPAN
                        class="fieldrequiredtext"><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_street_line1" SIZE="77" MAXLENGTH="75" VALUE="4 Country View Rd"
                       id="p_street_line1" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_street_line2><SPAN class="fieldlabeltext">Mailing Address Line 2 &nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_street_line2" SIZE="77" MAXLENGTH="75" id="p_street_line2" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_street_line3><SPAN class="fieldlabeltext">Mailing Address Line 3 &nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_street_line3" SIZE="77" MAXLENGTH="75" id="p_street_line3" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_city><SPAN class="fieldlabeltext">City&nbsp;&nbsp;&nbsp;</SPAN><SPAN
                        class="fieldrequiredtext"><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_city" SIZE="52" MAXLENGTH="50" VALUE="Malvern" id="p_city" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_stat_code><SPAN class="fieldlabeltext">State&nbsp;&nbsp;&nbsp;</SPAN><SPAN
                        class="fieldrequiredtext"><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <SELECT NAME="p_stat_code" SIZE="1" ID="p_stat_code">
                    <OPTION VALUE="">Not Applicable</OPTION>
                    <OPTION VALUE="123">1Test</OPTION>
                    <OPTION VALUE="AL">Alabama</OPTION>
                    <OPTION VALUE="AK">Alaska</OPTION>
                    <OPTION VALUE="AB">Alberta</OPTION>
                    <OPTION VALUE="AS">American Samoa</OPTION>
                    <OPTION VALUE="AZ">Arizona</OPTION>
                    <OPTION VALUE="AR">Arkansas</OPTION>
                    <OPTION VALUE="ACT">Australian Capital Territory</OPTION>
                    <OPTION VALUE="BC">British Columbia</OPTION>
                    <OPTION VALUE="CA">California</OPTION>
                    <OPTION VALUE="CN">Canada</OPTION>
                    <OPTION VALUE="CZ">Canal Zone</OPTION>
                    <OPTION VALUE="CO">Colorado</OPTION>
                    <OPTION VALUE="CT">Connecticut</OPTION>
                    <OPTION VALUE="DE">Delaware</OPTION>
                    <OPTION VALUE="DC">District of Columbia</OPTION>
                    <OPTION VALUE="FL">Florida</OPTION>
                    <OPTION VALUE="FC">Foreign Country</OPTION>
                    <OPTION VALUE="FR">Foreign State</OPTION>
                    <OPTION VALUE="GA">Georgia</OPTION>
                    <OPTION VALUE="GU">Guam</OPTION>
                    <OPTION VALUE="HI">Hawaii</OPTION>
                    <OPTION VALUE="ID">Idaho</OPTION>
                    <OPTION VALUE="IL">Illinois</OPTION>
                    <OPTION VALUE="IND">Indian Capital Territory</OPTION>
                    <OPTION VALUE="IN">Indiana</OPTION>
                    <OPTION VALUE="IA">Iowa</OPTION>
                    <OPTION VALUE="KS">Kansas</OPTION>
                    <OPTION VALUE="KY">Kentucky</OPTION>
                    <OPTION VALUE="LA">Louisiana</OPTION>
                    <OPTION VALUE="ME">Maine</OPTION>
                    <OPTION VALUE="MB">Manitoba</OPTION>
                    <OPTION VALUE="MD">Maryland</OPTION>
                    <OPTION VALUE="MA">Massachusetts</OPTION>
                    <OPTION VALUE="MI">Michigan</OPTION>
                    <OPTION VALUE="AA">Military - Americas</OPTION>
                    <OPTION VALUE="AE">Military - Europe</OPTION>
                    <OPTION VALUE="AP">Military - Pacific</OPTION>
                    <OPTION VALUE="MN">Minnesota</OPTION>
                    <OPTION VALUE="MS">Mississippi</OPTION>
                    <OPTION VALUE="MO">Missouri</OPTION>
                    <OPTION VALUE="MT">Montana</OPTION>
                    <OPTION VALUE="NE">Nebraska</OPTION>
                    <OPTION VALUE="NV">Nevada</OPTION>
                    <OPTION VALUE="NB">New Brunswick</OPTION>
                    <OPTION VALUE="NH">New Hampshire</OPTION>
                    <OPTION VALUE="NJ">New Jersey</OPTION>
                    <OPTION VALUE="NM">New Mexico</OPTION>
                    <OPTION VALUE="NSW">New South Wales</OPTION>
                    <OPTION VALUE="ZZ">New State</OPTION>
                    <OPTION VALUE="NY">New York</OPTION>
                    <OPTION VALUE="NF">Newfoundland</OPTION>
                    <OPTION VALUE="NC">North Carolina</OPTION>
                    <OPTION VALUE="ND">North Dakota</OPTION>
                    <OPTION VALUE="CM">Northern Mariana Islands</OPTION>
                    <OPTION VALUE="NT.">Northern Territory</OPTION>
                    <OPTION VALUE="NT">Northwest Territories</OPTION>
                    <OPTION VALUE="NS">Nova Scotia</OPTION>
                    <OPTION VALUE="OH">Ohio</OPTION>
                    <OPTION VALUE="OK">Oklahoma</OPTION>
                    <OPTION VALUE="ON">Ontario</OPTION>
                    <OPTION VALUE="OR">Oregon</OPTION>
                    <OPTION VALUE="PA" SELECTED>Pennsylvania</OPTION>
                    <OPTION VALUE="PE">Prince Edward Island</OPTION>
                    <OPTION VALUE="PQ">Provence of Quebec</OPTION>
                    <OPTION VALUE="PR">Puerto Rico</OPTION>
                    <OPTION VALUE="QLD">Queensland</OPTION>
                    <OPTION VALUE="RI">Rhode Island</OPTION>
                    <OPTION VALUE="AJ">SAAADMS_State</OPTION>
                    <OPTION VALUE="SK">Saskatchewan</OPTION>
                    <OPTION VALUE="SA">South Australia</OPTION>
                    <OPTION VALUE="SC">South Carolina</OPTION>
                    <OPTION VALUE="SD">South Dakota</OPTION>
                    <OPTION VALUE="TAS">Tasmania</OPTION>
                    <OPTION VALUE="TN">Tennessee</OPTION>
                    <OPTION VALUE="TX">Texas</OPTION>
                    <OPTION VALUE="UT">Utah</OPTION>
                    <OPTION VALUE="VT">Vermont</OPTION>
                    <OPTION VALUE="VIC">Victoria</OPTION>
                    <OPTION VALUE="VI">Virgin Islands</OPTION>
                    <OPTION VALUE="VA">Virginia</OPTION>
                    <OPTION VALUE="WA">Washington</OPTION>
                    <OPTION VALUE="WV">West Virginia</OPTION>
                    <OPTION VALUE="WA.">Western Australia</OPTION>
                    <OPTION VALUE="WI">Wisconsin</OPTION>
                    <OPTION VALUE="WY">Wyoming</OPTION>
                    <OPTION VALUE="YT">Yukon Territory</OPTION>
                </SELECT>
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_zip><SPAN class="fieldlabeltext">Zipcode&nbsp;&nbsp;&nbsp;</SPAN><SPAN
                        class="fieldrequiredtext"><IMG SRC="/wtlgifs/web_required_cascade.png" ALIGN="bottom"
                                                       ALT="Required" CLASS="headerImg" TITLE="Required"
                                                       NAME="web_required" HSPACE=0 VSPACE=0 BORDER=0 HEIGHT=9 WIDTH=10>
                </SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_zip" SIZE="33" MAXLENGTH="30" VALUE="19355" id="p_zip" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_natn_code><SPAN class="fieldlabeltext">Nation&nbsp;&nbsp;&nbsp;</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <SELECT NAME="p_natn_code" SIZE="1" ID="p_natn_code">
                    <OPTION VALUE="">Not Applicable</OPTION>
                    <OPTION VALUE="WWWWW">5 Character Test</OPTION>
                    <OPTION VALUE="AF">Afghanistan</OPTION>
                    <OPTION VALUE="1">Afghanistan</OPTION>
                    <OPTION VALUE="AX">Aland Islands</OPTION>
                    <OPTION VALUE="2">Albania</OPTION>
                    <OPTION VALUE="AL">Albania</OPTION>
                    <OPTION VALUE="3">Algeria</OPTION>
                    <OPTION VALUE="4">Andorra</OPTION>
                    <OPTION VALUE="5">Angola</OPTION>
                    <OPTION VALUE="6">Antigua and Barbuda and isless</OPTION>
                    <OPTION VALUE="7">Argentina</OPTION>
                    <OPTION VALUE="AR">Argentina</OPTION>
                    <OPTION VALUE="AA">Armenia</OPTION>
                    <OPTION VALUE="AU">Aruba</OPTION>
                    <OPTION VALUE="8">Australia</OPTION>
                    <OPTION VALUE="BH">Bahrain</OPTION>
                    <OPTION VALUE="11">Bahrain</OPTION>
                    <OPTION VALUE="12">Bangladesh</OPTION>
                    <OPTION VALUE="13">Barbados</OPTION>
                    <OPTION VALUE="14">Belgium</OPTION>
                    <OPTION VALUE="@1">Belgium</OPTION>
                    <OPTION VALUE="15">Belize</OPTION>
                    <OPTION VALUE="16">Benin</OPTION>
                    <OPTION VALUE="17">Bhutan</OPTION>
                    <OPTION VALUE="18">Bolivia</OPTION>
                    <OPTION VALUE="19">Botswana</OPTION>
                    <OPTION VALUE="20">Brazil</OPTION>
                    <OPTION VALUE="21">Brunei</OPTION>
                    <OPTION VALUE="22">Bulgaria</OPTION>
                    <OPTION VALUE="23">Burma</OPTION>
                    <OPTION VALUE="24">Burundi</OPTION>
                    <OPTION VALUE="KH">Cambodia</OPTION>
                    <OPTION VALUE="25">Cambodia</OPTION>
                    <OPTION VALUE="26">Cameroon</OPTION>
                    <OPTION VALUE="800">Canada</OPTION>
                    <OPTION VALUE="CA">Canada</OPTION>
                    <OPTION VALUE="27">Canada</OPTION>
                    <OPTION VALUE="CV">Cape Verde</OPTION>
                    <OPTION VALUE="28">Cape Verde</OPTION>
                    <OPTION VALUE="29">Central African Republic</OPTION>
                    <OPTION VALUE="30">Chad</OPTION>
                    <OPTION VALUE="31">Chile</OPTION>
                    <OPTION VALUE="32">China</OPTION>
                    <OPTION VALUE="CN">China</OPTION>
                    <OPTION VALUE="@2">China</OPTION>
                    <OPTION VALUE="33">China (Taiwan)</OPTION>
                    <OPTION VALUE="34">Columbia</OPTION>
                    <OPTION VALUE="35">Comoros</OPTION>
                    <OPTION VALUE="36">Congo</OPTION>
                    <OPTION VALUE="37">Costa Rica</OPTION>
                    <OPTION VALUE="38">Cuba</OPTION>
                    <OPTION VALUE="201">Curacao</OPTION>
                    <OPTION VALUE="39">Cyprus</OPTION>
                    <OPTION VALUE="40">Czech Republic</OPTION>
                    <OPTION VALUE="41">Danemark</OPTION>
                    <OPTION VALUE="42">Djibouti</OPTION>
                    <OPTION VALUE="43">Dominica</OPTION>
                    <OPTION VALUE="44">Dominican Republic</OPTION>
                    <OPTION VALUE="55">East Germany</OPTION>
                    <OPTION VALUE="45">Ecuador</OPTION>
                    <OPTION VALUE="46">Egypt</OPTION>
                    <OPTION VALUE="47">El Salvador</OPTION>
                    <OPTION VALUE="48">Equatorial Guinea</OPTION>
                    <OPTION VALUE="EA">Estonia</OPTION>
                    <OPTION VALUE="49">Ethiopia</OPTION>
                    <OPTION VALUE="50">Fiji</OPTION>
                    <OPTION VALUE="51">Finland</OPTION>
                    <OPTION VALUE="52">France</OPTION>
                    <OPTION VALUE="FR">France</OPTION>
                    <OPTION VALUE="53">Gabon</OPTION>
                    <OPTION VALUE="54">Gambia</OPTION>
                    <OPTION VALUE="57">Ghana</OPTION>
                    <OPTION VALUE="58">Greece</OPTION>
                    <OPTION VALUE="59">Grenada</OPTION>
                    <OPTION VALUE="60">Guam</OPTION>
                    <OPTION VALUE="61">Guatemala</OPTION>
                    <OPTION VALUE="63">Guyana</OPTION>
                    <OPTION VALUE="64">Haiti</OPTION>
                    <OPTION VALUE="65">Honduras</OPTION>
                    <OPTION VALUE="66">Hungary</OPTION>
                    <OPTION VALUE="PLIAT">IAT Test of a max length cntry</OPTION>
                    <OPTION VALUE="67">Iceland</OPTION>
                    <OPTION VALUE="91">India</OPTION>
                    <OPTION VALUE="68">India</OPTION>
                    <OPTION VALUE="69">Indonesia</OPTION>
                    <OPTION VALUE="70">Iran</OPTION>
                    <OPTION VALUE="71">Iraq</OPTION>
                    <OPTION VALUE="72">Ireland</OPTION>
                    <OPTION VALUE="73">Israel</OPTION>
                    <OPTION VALUE="74">Italy</OPTION>
                    <OPTION VALUE="75">Ivory Coast</OPTION>
                    <OPTION VALUE="76">Jamaica</OPTION>
                    <OPTION VALUE="77">Japan</OPTION>
                    <OPTION VALUE="JE">Jersey</OPTION>
                    <OPTION VALUE="78">Jordan</OPTION>
                    <OPTION VALUE="79">Kenya</OPTION>
                    <OPTION VALUE="80">Kiribati</OPTION>
                    <OPTION VALUE="83">Kuwait</OPTION>
                    <OPTION VALUE="84">Laos</OPTION>
                    <OPTION VALUE="85">Lebanon</OPTION>
                    <OPTION VALUE="86">Lesotho</OPTION>
                    <OPTION VALUE="87">Liberia</OPTION>
                    <OPTION VALUE="88">Libya</OPTION>
                    <OPTION VALUE="89">Liechtenstein</OPTION>
                    <OPTION VALUE="90">Luxembourg</OPTION>
                    <OPTION VALUE="MG">Madagascar</OPTION>
                    <OPTION VALUE="92">Malawi</OPTION>
                    <OPTION VALUE="93">Malaysia</OPTION>
                    <OPTION VALUE="94">Maldives</OPTION>
                    <OPTION VALUE="95">Mali</OPTION>
                    <OPTION VALUE="96">Malta</OPTION>
                    <OPTION VALUE="97">Mauritania</OPTION>
                    <OPTION VALUE="98">Mauritius</OPTION>
                    <OPTION VALUE="99">Mexico</OPTION>
                    <OPTION VALUE="100">Monaco</OPTION>
                    <OPTION VALUE="101">Mongolia</OPTION>
                    <OPTION VALUE="102">Morocco</OPTION>
                    <OPTION VALUE="103">Mozambique</OPTION>
                    <OPTION VALUE="104">Nauru</OPTION>
                    <OPTION VALUE="105">Nepal</OPTION>
                    <OPTION VALUE="106">Netherlands</OPTION>
                    <OPTION VALUE="107">New Zealand</OPTION>
                    <OPTION VALUE="108">Nicaragua</OPTION>
                    <OPTION VALUE="109">Niger</OPTION>
                    <OPTION VALUE="110">Nigeria</OPTION>
                    <OPTION VALUE="81">North Korea</OPTION>
                    <OPTION VALUE="164">North Yemen</OPTION>
                    <OPTION VALUE="111">Norway</OPTION>
                    <OPTION VALUE="112">Oman</OPTION>
                    <OPTION VALUE="113">Pakistan</OPTION>
                    <OPTION VALUE="PS">Palestinian Territory, Occupie</OPTION>
                    <OPTION VALUE="114">Panama</OPTION>
                    <OPTION VALUE="115">Papua New Guinea</OPTION>
                    <OPTION VALUE="116">Paraguay</OPTION>
                    <OPTION VALUE="117">Peru</OPTION>
                    <OPTION VALUE="118">Philippines</OPTION>
                    <OPTION VALUE="119">Poland</OPTION>
                    <OPTION VALUE="120">Portugal</OPTION>
                    <OPTION VALUE="121">Qatar</OPTION>
                    <OPTION VALUE="122">Romania</OPTION>
                    <OPTION VALUE="154">Russian Federation</OPTION>
                    <OPTION VALUE="123">Rwanda</OPTION>
                    <OPTION VALUE="AJ">SAAADMS_CODE</OPTION>
                    <OPTION VALUE="SH">Saint Helena, Ascension, and T</OPTION>
                    <OPTION VALUE="125">Saint Lucia</OPTION>
                    <OPTION VALUE="127">San Marino</OPTION>
                    <OPTION VALUE="128">Sao Tome and Principe</OPTION>
                    <OPTION VALUE="129">Saudi Arabia</OPTION>
                    <OPTION VALUE="130">Senegal</OPTION>
                    <OPTION VALUE="131">Seychelles</OPTION>
                    <OPTION VALUE="132">Sierra Leone</OPTION>
                    <OPTION VALUE="133">Singapore</OPTION>
                    <OPTION VALUE="134">Solomon Islands</OPTION>
                    <OPTION VALUE="135">Somalia</OPTION>
                    <OPTION VALUE="136">South Africa</OPTION>
                    <OPTION VALUE="82">South Korea</OPTION>
                    <OPTION VALUE="165">South Yemen</OPTION>
                    <OPTION VALUE="137">Spain</OPTION>
                    <OPTION VALUE="138">Sri Lanka</OPTION>
                    <OPTION VALUE="124">St. Kitts and Nevis</OPTION>
                    <OPTION VALUE="126">St. Vincent and Grenadines</OPTION>
                    <OPTION VALUE="139">Sudan</OPTION>
                    <OPTION VALUE="140">Suriname</OPTION>
                    <OPTION VALUE="141">Swaziland</OPTION>
                    <OPTION VALUE="142">Sweden</OPTION>
                    <OPTION VALUE="143">Switzerland</OPTION>
                    <OPTION VALUE="144">Syria</OPTION>
                    <OPTION VALUE="145">Tanzania</OPTION>
                    <OPTION VALUE="146">Thailand</OPTION>
                    <OPTION VALUE="10">The Bahamas</OPTION>
                    <OPTION VALUE="147">Togo</OPTION>
                    <OPTION VALUE="148">Tongo</OPTION>
                    <OPTION VALUE="149">Trinidad and Tobago</OPTION>
                    <OPTION VALUE="150">Tunisia</OPTION>
                    <OPTION VALUE="151">Turkey</OPTION>
                    <OPTION VALUE="152">Tuvalu</OPTION>
                    <OPTION VALUE="153">Uganda</OPTION>
                    <OPTION VALUE="155">United Arab Emirates</OPTION>
                    <OPTION VALUE="156">United Kingdom</OPTION>
                    <OPTION VALUE="US">United States</OPTION>
                    <OPTION VALUE="157">United States of America</OPTION>
                    <OPTION VALUE="12345">Unknown</OPTION>
                    <OPTION VALUE="158">Upper Volta</OPTION>
                    <OPTION VALUE="159">Uruguay</OPTION>
                    <OPTION VALUE="160">Vanuatu</OPTION>
                    <OPTION VALUE="161">Venezuela</OPTION>
                    <OPTION VALUE="162">Vietnam</OPTION>
                    <OPTION VALUE="56">West Germany</OPTION>
                    <OPTION VALUE="163">Western Samoa</OPTION>
                    <OPTION VALUE="166">Yugoslavia</OPTION>
                    <OPTION VALUE="167">Zaire</OPTION>
                    <OPTION VALUE="168">Zambia</OPTION>
                    <OPTION VALUE="169">Zimbabwe</OPTION>
                    <OPTION VALUE="BERMU">dp test code</OPTION>
                    <OPTION VALUE="9">Österreich</OPTION>
                </SELECT>
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_sex><SPAN class="fieldlabeltext">Gender&nbsp;&nbsp;&nbsp;</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <SELECT NAME="p_sex" id="p_sex">
                    <OPTION SELECTED value="">Select Gender
                    <OPTION value="F">Female
                    <OPTION value="M">Male
                </SELECT>
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_birth_date><SPAN class="fieldlabeltext">Birthdate (DD/MM/YYYY)&nbsp;&nbsp;&nbsp;</SPAN>
                </LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_birth_date" id="p_birth_date" CLASS="text-input-field">
            </TD>
        </TR>
        <TR>
            <TD CLASS="text-input-field">
                <LABEL for=p_ssn><SPAN class="fieldlabeltext">SSN/SIN/TIN&nbsp;&nbsp;&nbsp;</SPAN></LABEL>
            </TD>
            <TD CLASS="text-input-field">
                <INPUT TYPE="text" NAME="p_ssn" SIZE="10" MAXLENGTH="9" id="p_ssn" autocomplete="off" CLASS="text-input-field">
            </TD>
        </TR>
    </TABLE>
    <INPUT TYPE="submit" NAME="p_save" VALUE="Save" id="p_save" class="signin-button">
</FORM>

</body>
</html>