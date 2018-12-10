/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import net.hedtech.banner.general.person.PersonUtility

class GeneralSsbProxyServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalSsbProxyService
    def dataSource
    def conn

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        createProxy_0()
        createProxy_1()
    }

    @After
    public void tearDown() {
        deleteProxy_0()
        deleteProxy_1()
        deleteProxyPageWithRole()
        super.tearDown()
    }


    @Test
    void testTokenError() {
        def result = generalSsbProxyService.setProxy("QUFBVjNnQUFJQUF")

        assertNotNull result.error
    }


    @Test
    void testStudentList() {

        def result = generalSsbProxyService.getStudentListForProxy(-1)

        assertNotNull result.students
        assertEquals PersonUtility.getPerson("GDP000005").pidm, result.students[0].pidm
        assertEquals PersonUtility.getPerson("HOS00001").pidm, result.students[1].pidm
    }


    @Test
    void testStudentListNoAccess() {
        deleteProxy_0()
        createProxyNoAccess()
        def result = generalSsbProxyService.getStudentListForProxy(-1)
        assertTrue result?.students?.active.size() == 0
    }

    @Test
    void testSetProxyExpiredActionLink() {
        // expired letter SSS_REGD_USER2	SS_PINRESET2    AAAgFJAAFAAAspFAAB
        // set encodedRowId to that letter's row id encoded as base64
        String encodedRowId = 'QUFBZ0ZKQUFGQUFBc3BGQUFC'
        def result = generalSsbProxyService.setProxy(encodedRowId)

        assertNull result.gidm
        assertTrue result.login
        assertEquals 'token-expire', result.message
        assertFalse result.verify
        assertFalse result.doPin
        assertFalse result.error
    }


    @Test
    void testGetProxyPages() {
        addProxyPageWithRole()
        updateProxyRETP()
        def result = generalSsbProxyService.getProxyPages(-1, PersonUtility.getPerson("GDP000005").pidm )

        def page = result.pages.find { it -> it.url.equals("/ssb/proxy/mypage") }

        assertNotNull page
        deleteProxyPageWithRole()
    }


    def createProxy_0() {

        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
declare

   lv_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE := 0;
   lv_pinhash     gpbprxy.gpbprxy_pin%TYPE;
   lv_salt        gpbprxy.gpbprxy_salt%TYPE;
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
   lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
   lv_proxy_url   VARCHAR2(300);

begin

  gp_gpbprxy.P_Create (
      p_proxy_idm        => -1,
      p_email_address    => 'z',
      p_last_name        => 'a',
      p_first_name       => 'b',
      p_proxy_pidm       => ${pidm},
      p_pin              => null,
      p_pin_disabled_ind => 'C',
      p_salt             => null,
      p_entity_cde       => null,
      p_id               => null,
      p_email_ver_date   => NULL,
      p_pin_exp_date     => NULL,
      p_create_user      => goksels.f_get_ssb_id_context,
      p_create_date      => SYSDATE,
      p_user_id          => goksels.f_get_ssb_id_context,
      p_opt_out_adv_date => NULL,
      p_rowid_out        => lv_hold_rowid
      );


        gp_gprxref.P_Create (
         p_proxy_idm   => -1,
         p_person_pidm => ${pidm},
         p_retp_code   => 'AAA',
         p_proxy_desc  => NULL,
         p_start_date  => TRUNC(SYSDATE-1),
         p_stop_date   => TRUNC(SYSDATE + 1),
         p_create_user => goksels.f_get_ssb_id_context,
         p_create_date => SYSDATE,
         p_user_id     => goksels.f_get_ssb_id_context,
         p_passphrase  => NULL,
         p_rowid_out   => lv_hold_rowid
         );

insert into TWGRROLE (TWGRROLE_PIDM,TWGRROLE_ROLE,TWGRROLE_ACTIVITY_DATE) values(${pidm},'WTAILORPROXYMGMT',sysdate);
insert into TWGRROLE (TWGRROLE_PIDM,TWGRROLE_ROLE,TWGRROLE_ACTIVITY_DATE) values(${pidm},'WTAILORPROXYACCESS',sysdate);

end;
            """)
    }


    def createProxy_1() {

        def pidm = PersonUtility.getPerson("HOS00001").pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
declare

   lv_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE := 0;
   lv_pinhash     gpbprxy.gpbprxy_pin%TYPE;
   lv_salt        gpbprxy.gpbprxy_salt%TYPE;
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
   lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
   lv_proxy_url   VARCHAR2(300);

begin


        gp_gprxref.P_Create (
         p_proxy_idm   => -1,
         p_person_pidm => ${pidm},
         p_retp_code   => 'AAA',
         p_proxy_desc  => NULL,
         p_start_date  => TRUNC(SYSDATE-1),
         p_stop_date   => TRUNC(SYSDATE + 1),
         p_create_user => goksels.f_get_ssb_id_context,
         p_create_date => SYSDATE,
         p_user_id     => goksels.f_get_ssb_id_context,
         p_passphrase  => NULL,
         p_rowid_out   => lv_hold_rowid
         );

insert into TWGRROLE (TWGRROLE_PIDM,TWGRROLE_ROLE,TWGRROLE_ACTIVITY_DATE) values(${pidm},'WTAILORPROXYMGMT',sysdate);
insert into TWGRROLE (TWGRROLE_PIDM,TWGRROLE_ROLE,TWGRROLE_ACTIVITY_DATE) values(${pidm},'WTAILORPROXYACCESS',sysdate);

end;
            """)

    }


    def createProxyNoAccess() {

        //p_start_date --> SYSDATE+10)

        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
declare

   lv_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE := 0;
   lv_pinhash     gpbprxy.gpbprxy_pin%TYPE;
   lv_salt        gpbprxy.gpbprxy_salt%TYPE;
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
   lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
   lv_proxy_url   VARCHAR2(300);

begin

  gp_gpbprxy.P_Create (
      p_proxy_idm        => -1,
      p_email_address    => 'z',
      p_last_name        => 'a',
      p_first_name       => 'b',
      p_proxy_pidm       => ${pidm},
      p_pin              => null,
      p_pin_disabled_ind => 'C',
      p_salt             => null,
      p_entity_cde       => null,
      p_id               => null,
      p_email_ver_date   => NULL,
      p_pin_exp_date     => NULL,
      p_create_user      => goksels.f_get_ssb_id_context,
      p_create_date      => SYSDATE,
      p_user_id          => goksels.f_get_ssb_id_context,
      p_opt_out_adv_date => NULL,
      p_rowid_out        => lv_hold_rowid
      );


        gp_gprxref.P_Create (
         p_proxy_idm   => -1,
         p_person_pidm => ${pidm},
         p_retp_code   => 'AAA',
         p_proxy_desc  => NULL,
         p_start_date  => TRUNC(SYSDATE+10),
         p_stop_date   => TRUNC(SYSDATE + 11),
         p_create_user => goksels.f_get_ssb_id_context,
         p_create_date => SYSDATE,
         p_user_id     => goksels.f_get_ssb_id_context,
         p_passphrase  => NULL,
         p_rowid_out   => lv_hold_rowid
         );

insert into TWGRROLE (TWGRROLE_PIDM,TWGRROLE_ROLE,TWGRROLE_ACTIVITY_DATE) values(${pidm},'WTAILORPROXYMGMT',sysdate);
insert into TWGRROLE (TWGRROLE_PIDM,TWGRROLE_ROLE,TWGRROLE_ACTIVITY_DATE) values(${pidm},'WTAILORPROXYACCESS',sysdate);

end;
            """)
    }


    def deleteProxy_0() {

        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
        begin
        DELETE
        FROM gprxref
        WHERE gprxref_proxy_idm   = -1;

        DELETE
        FROM gpbprxy
        WHERE gpbprxy_proxy_idm = -1;

        DELETE FROM TWGRROLE WHERE TWGRROLE_PIDM = ${pidm};

        commit;
        end;
            """)

    }


    def deleteProxy_1() {

        def pidm = PersonUtility.getPerson("HOS00001").pidm

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
        begin
        DELETE
        FROM gprxref
        WHERE gprxref_proxy_idm   = -1;

        DELETE
        FROM gpbprxy
        WHERE gpbprxy_proxy_idm = -1;

        DELETE FROM TWGRROLE WHERE TWGRROLE_PIDM = ${pidm};

        commit;

        end;
            """)
    }


    // adds a new proxy page
    private def addProxyPageWithRole() {
        conn = dataSource.getConnection()

        Sql sqlObj = new Sql( conn )
        try {
            sqlObj.executeInsert("INSERT INTO twgbwmnu (TWGBWMNU_NAME,TWGBWMNU_DESC,TWGBWMNU_PAGE_TITLE,TWGBWMNU_HEADER,TWGBWMNU_BACK_MENU_IND,TWGBWMNU_MODULE,TWGBWMNU_ENABLED_IND," +
                    "TWGBWMNU_INSECURE_ALLOWED_IND,TWGBWMNU_ACTIVITY_DATE,TWGBWMNU_CACHE_OVERRIDE,TWGBWMNU_SOURCE_IND,TWGBWMNU_ADM_ACCESS_IND) VALUES " +
                    "('/ssb/proxy/mypage','My Proxy Page','My Proxy Page','My Proxy Page','N','WTL','Y','N'," +
                    "TO_TIMESTAMP('13-AUG-02','DD-MON-RR HH.MI.SSXFF AM'),'S','L','N')")

            sqlObj.executeInsert("insert into TWGRMENU (TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL,TWGRMENU_DB_LINK_IND,TWGRMENU_SUBMENU_IND,TWGRMENU_ACTIVITY_DATE,TWGRMENU_SOURCE_IND,TWGRMENU_ENABLED)\n" +
                    "values ('PROXY_ACCESS_PARENT',99,'My Proxy Page','/ssb/proxy/mypage','Y','N',sysdate,'L','Y')")

            sqlObj.executeInsert("insert into twgrwmrl (twgrwmrl_name, twgrwmrl_role,twgrwmrl_activity_date,twgrwmrl_source_ind)" +
                    "values ('/ssb/proxy/mypage','PROXY_ACCESS_SUPPORT',TO_TIMESTAMP('13-AUG-02','DD-MON-RR HH.MI.SSXFF AM'),'L')")

            sqlObj.commit();
        } finally {
            sqlObj?.close()
        }

    }


    private def deleteProxyPageWithRole() {
        conn = dataSource.getConnection()
        Sql  sqlObj = new Sql( conn )
        try {
            sqlObj.execute("delete from  twgrmenu where TWGRMENU_SEQUENCE=99 and TWGRMENU_NAME = 'PROXY_ACCESS_PARENT'");
            sqlObj.execute("delete from twgbwmnu where TWGBWMNU_NAME='/ssb/proxy/mypage' and TWGBWMNU_SOURCE_IND='L'");
            sqlObj.execute("delete from twgrwmrl where twgrwmrl_name='/ssb/proxy/mypage' and twgrwmrl_SOURCE_IND='L'");
            sqlObj.commit();
        } finally {
            sqlObj?.close()
        }
    }



    def updateProxyRETP() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""
declare

   lv_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE := 0;
   lv_pinhash     gpbprxy.gpbprxy_pin%TYPE;
   lv_salt        gpbprxy.gpbprxy_salt%TYPE;
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
   lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
   lv_proxy_url   VARCHAR2(300);

begin

gp_gprxref.P_Update (
      p_proxy_idm   => -1,
      p_person_pidm => ${pidm},
      p_retp_code   => 'PARENT',
      p_start_date  => TRUNC(SYSDATE - 1),
      p_stop_date   => TRUNC(SYSDATE + 10),
      p_user_id     => goksels.f_get_ssb_id_context
      );
   gb_common.P_Commit;

end;
            """)
    }
}
