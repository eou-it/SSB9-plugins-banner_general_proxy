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
    void testViewHolds() {
        def pidm = PersonUtility.getPerson("STUAFR320").pidm
        def result = generalSsbProxyService.viewHolds(pidm)

        println result

        assertNotNull result.message
        assertTrue result.rows.size() > 0

        assertTrue result.rows[4].hold_for.contains('evaluation')
        assertEquals '$111.22', result.rows[4].r_amount_owed.trim()
        assertEquals 'Jun 11, 2014', result.rows[4].r_from_date
        assertEquals 'This one has them all', result.rows[4].r_reason
        assertEquals 'Dec 31, 2025', result.rows[4].r_to_date
        assertEquals 'Compliance Hold', result.rows[4].stvhold_desc
        assertEquals 'Hold Originator', result.rows[4].stvorig_desc
    }

    @Test
    void testViewHoldsNoHolds() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def result = generalSsbProxyService.viewHolds(pidm)

        println result

        assertEquals 'noHoldsExist', result.message
        assertTrue result.rows.size() == 0
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
}
