/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/



/**
 * Specifies all of the URL mappings supported by the application.
 */
class UrlMappings {

    static mappings = {

        "/ssb/menu" {
            controller = "selfServiceMenu"
            action = [GET: "data", POST: "create"]
        }

        "/ssb/i18n/$name*.properties"(controller: "i18n", action: "index" )


        "/ssb/resource/$controller" {
            action = [ GET: "list", POST: "create" ]
        }

        "/ssb/resource/$controller/batch" {
            action = [ POST: "processBatch" ]
        }


        "/ssb/resource/$controller/$id?" {
            action = [ GET: "show", PUT: "update", DELETE: "destroy" ]
            constraints {
                id(matches:/[0-9]+/)
            }
        }

        "/ssb/resource/$controller/$type" {
            action = "list"
            constraints {
                type(matches:/[^0-9]+/)
            }
        }

        "/ssb/resource/$controller/$type/batch" {
            action = [ POST: "processBatch" ]
            constraints {
                type(matches:/[^0-9]+/)
            }
        }

        "/ssb/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }

        "/login/auth" {
            controller = "login"
            action = "auth"
        }

        "/login/denied" {
            controller = "login"
            action = "denied"
        }

        "/login/ajaxDenied" {
            controller = "hrDashboard"
            action = "denied403"
        }

        "/login/authAjax" {
            controller = "login"
            action = "authAjax"
        }

        "/login/authfail" {
            controller = "login"
            action = "authfail"
        }

        "/logout" {
            controller = "logout"
            action = "index"
        }

        "/logout/timeout" {
            controller = "logout"
            action = "timeout"
        }

        "/ssb/$controller/logout" {
            controller = "logout"
            action = "index"
        }

        "/ssb/$controller/logout/timeout" {
            controller = "logout"
            action = "timeout"
        }

        "/"(view:"/index")
        "/index.gsp"(view:"/index")
        "500"(controller: "error", action: "internalServerError")
        "403"(controller: "error", action: "accessForbidden")

        "/login/resetPassword" {
            controller = "login"
            action = "forgotpassword"
        }


        "/resetPassword/validateans" {
            controller = "resetPassword"
            action = "validateAnswer"
        }


        "/resetPassword/resetpin" {
            controller = "resetPassword"
            action = "resetPin"
        }


        "/resetPassword/auth" {
            controller = "login"
            action = "auth"
        }


        "/ssb/resetPassword/auth" {
            controller = "login"
            action = "auth"
        }


        "/resetPassword/recovery" {
            controller = "resetPassword"
            action = "recovery"
        }


        "/resetPassword/validateCode" {
            controller = "resetPassword"
            action = "validateCode"
        }


        "/resetPassword/login/auth" {
            controller = "login"
            action = "auth"
        }


        "/resetPassword/logout/timeout" {
            controller = "logout"
            action = "timeout"
        }
    }
}